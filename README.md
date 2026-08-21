# efca-api

API REST stateless em Java 21 e Spring Boot que aplica e pontua a EFCA (Escala de Fenótipo de Comportamento Alimentar), um questionário de autoavaliação de 16 itens distribuídos em cinco domínios de comportamento alimentar.

A API não persiste nada. Cada requisição de pontuação é independente e nenhuma resposta é gravada em banco ou em disco.

**Stack:** Java 21, Spring Boot 4.1.0, Maven, JUnit 5, Docker
**Front-end de referência:** [efca-front](https://github.com/Zero-Kng/efca-front)

## Demonstração

Ambiente publicado: `https://efca-api.onrender.com`

```
GET https://efca-api.onrender.com/api/questions
```

A instância roda no plano gratuito do Render, que hiberna o serviço após um período sem tráfego. A primeira requisição depois da hibernação pode levar cerca de um minuto até responder. As seguintes respondem normalmente.

## Como funciona a pontuação

O respondente atribui a cada item uma nota de 1 a 5 em escala Likert, onde 1 é discordo totalmente e 5 é concordo totalmente. Os 16 itens se distribuem assim:

| Domínio | Código | Itens | Pontuação máxima |
|---|---|---|---|
| Hedônico | `HEDONICO` | 2 | 10 |
| Hiperfágico | `HIPERFAGICO` | 4 | 20 |
| Emocional | `EMOCIONAL` | 3 | 15 |
| Compulsivo | `COMPULSIVO` | 3 | 15 |
| Desorganizado | `DESORGANIZADO` | 4 | 20 |

**Itens reversos.** O item `q9` é pontuado de forma invertida, porque a afirmação é redigida no sentido oposto ao do domínio que mede: concordar com ela indica comportamento mais organizado, não menos. Para esses itens a API aplica `valor_efetivo = 6 - valor_informado`, de modo que 1 vira 5 e 5 vira 1. O respondente não precisa saber disso, e o front-end também não: a inversão acontece no servidor.

**Métricas devolvidas.** Para cada domínio a API retorna a soma dos valores efetivos, a pontuação máxima possível daquele domínio e a média por item arredondada a uma casa decimal. As somas não são comparáveis entre domínios, porque o número de itens varia de 2 a 4. Para comparar domínios entre si, use a média.

## Endpoints

### GET /api/questions

Devolve o banco de perguntas. O front-end monta o formulário a partir desta resposta, sem replicar o conteúdo do questionário.

```json
[
  {
    "id": "q1",
    "domain": "HIPERFAGICO",
    "domainLabel": "Hiperfágico",
    "text": "Geralmente como até me sentir cheio(a), estufado(a)."
  }
]
```

O campo `domain` é o código estável, próprio para lógica de programa. O campo `domainLabel` é o rótulo em português, próprio para exibição. Nenhum item é marcado como reverso na resposta, já que a inversão é responsabilidade do servidor.

### POST /api/responses

Recebe o conjunto completo de respostas e devolve a pontuação por domínio.

```json
{
  "answers": {
    "q1": 4, "q2": 2, "q3": 3, "q4": 5,
    "q5": 1, "q6": 3, "q7": 2, "q8": 4,
    "q9": 5, "q10": 2, "q11": 1, "q12": 4,
    "q13": 3, "q14": 5, "q15": 2, "q16": 1
  }
}
```

Resposta:

```json
{
  "domains": [
    {
      "domain": "HEDONICO",
      "domainLabel": "Hedônico",
      "sum": 8,
      "maxPossible": 10,
      "average": 4.0
    }
  ]
}
```

As 16 respostas são obrigatórias. Um envio parcial é rejeitado.

### GET /actuator/health

Verificação de disponibilidade. É o único endpoint do Actuator exposto.

## Contrato de erro

Toda falha devolve o mesmo formato, com `details` sempre em lista:

```json
{
  "timestamp": "2026-08-20T18:42:11.203Z",
  "error": "respostas_invalidas",
  "details": [
    "id de pergunta desconhecido: 'q99'",
    "nota inválida para 'q3': deve ser um inteiro entre 1 e 5",
    "2 pergunta(s) não foram respondidas"
  ]
}
```

| Status | `error` | Quando ocorre |
|---|---|---|
| 400 | `requisicao_invalida` | O corpo não passa na validação estrutural, por exemplo `answers` ausente ou vazio |
| 400 | `respostas_invalidas` | O corpo é estruturalmente válido, mas o conteúdo não é: id inexistente, nota fora de 1 a 5, ou questionário incompleto |
| 404 | `recurso_nao_encontrado` | Rota inexistente. A resposta lista os endpoints disponíveis |
| 500 | `erro_interno` | Falha não prevista. A causa é registrada no log do servidor e não é devolvida ao cliente |

A validação de conteúdo acumula todos os problemas encontrados antes de responder, em vez de interromper no primeiro. Um cliente que envia um formulário inteiro errado recebe a lista completa em uma única viagem.

## Como rodar

### Com Docker

```bash
docker build -t efca-api .
docker run -p 8080:8080 efca-api
```

Para liberar um front-end local:

```bash
docker run -p 8080:8080 -e EFCA_ALLOWED_ORIGINS=http://localhost:5500 efca-api
```

### Com Maven

Requer JDK 21.

```bash
mvn spring-boot:run
```

Testes:

```bash
mvn test
```

## Configuração

| Variável | Padrão | Função |
|---|---|---|
| `PORT` | `8080` | Porta de escuta. O Render injeta esta variável automaticamente |
| `EFCA_ALLOWED_ORIGINS` | `http://localhost:5500,http://127.0.0.1:5500` | Lista de origens autorizadas no CORS, separadas por vírgula |

`EFCA_ALLOWED_ORIGINS` precisa ser definida no ambiente de produção. Sem ela a API só aceita chamadas das origens locais de desenvolvimento e o front-end publicado é bloqueado pelo navegador.

## Decisões de projeto

**Sem banco de dados.** O cálculo depende apenas do corpo da requisição, então não existe estado a guardar entre chamadas. A ausência de persistência elimina a necessidade de tratar armazenamento de dado sensível, e permite escalar horizontalmente sem coordenação entre instâncias.

**Dado de identificação não trafega.** O front-end coleta identificação do respondente para compor o relatório, mas envia apenas o mapa de respostas. Nome e demais dados permanecem no navegador e nunca chegam ao servidor.

**Banco de perguntas no código, não em arquivo externo.** O questionário é um instrumento fechado de 16 itens. Mantê-lo em `QuestionBank` como lista imutável torna a alteração deliberada e rastreável no histórico do repositório, em vez de silenciosa em um arquivo de configuração.

**Camadas separadas.** Controllers apenas recebem e devolvem, a regra de pontuação vive em `ScoringService`, e os DTOs isolam o formato público do modelo interno. `ScoringService` não conhece Spring MVC, o que permite testá-lo com JUnit puro, sem subir contexto de aplicação.

**Records para DTO e modelo.** Imutabilidade por construção, sem boilerplate de getter e sem risco de um objeto de resposta ser alterado depois de montado.

## Segurança

| Medida | Implementação |
|---|---|
| CORS restrito | Origens vindas de variável de ambiente, métodos limitados a `GET` e `POST`, cabeçalhos limitados a `Content-Type` |
| Rejeição de campo desconhecido | `fail-on-unknown-properties: true` no Jackson, para que um corpo com campos extras seja recusado em vez de ignorado |
| Limite de payload | `max-swallow-size` e `max-http-form-post-size` em 64 KB, reduzindo o custo de uma requisição abusiva |
| Erro sem vazamento | O handler genérico registra a exceção no log e devolve mensagem neutra, sem stack trace nem detalhe de implementação |
| Superfície mínima do Actuator | Apenas `health` exposto |
| Container sem privilégio | A imagem final roda sob usuário `efca`, criado sem shell e sem root |
| Imagem enxuta | Build multi-stage: o Maven e o JDK ficam no estágio de compilação e não vão para a imagem final, que carrega apenas o JRE Alpine e o JAR |

## Testes

`ScoringServiceTest` cobre o cálculo e os três modos de rejeição de entrada:

- média correta quando todos os itens são respondidos
- rejeição de id de pergunta inexistente
- rejeição de nota fora do intervalo de 1 a 5
- rejeição de questionário incompleto

## Limitações conhecidas

- O endpoint público não tem rate limiting. Aceitável na escala atual, mas seria o primeiro item a resolver antes de qualquer divulgação ampla.
- O limite de `16` entradas em `AnswerRequest` está fixo na anotação de validação, enquanto o número real de perguntas é definido em `QuestionBank`. Se o instrumento mudar de tamanho, os dois pontos precisam ser alterados juntos.
- Não há pipeline de integração contínua. Os testes existem mas dependem de execução manual.
- A pontuação é descritiva. A API devolve soma e média por domínio, e não classifica o resultado em faixas nem emite qualquer interpretação clínica.

## Aviso

Este projeto é um exercício acadêmico de implementação de um instrumento de autoavaliação. O resultado não é diagnóstico e não substitui avaliação por profissional de saúde qualificado.
