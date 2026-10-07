package com.efca.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EfcaApiIntegrationTest {

    private static final String ALLOWED_ORIGIN = "http://localhost:5500";

    private final HttpClient client = HttpClient.newHttpClient();

    @Value("${local.server.port}")
    private int port;

    private String completeAnswers(int value) {
        return IntStream.rangeClosed(1, 16)
            .mapToObj(i -> "\"q" + i + "\":" + value)
            .collect(Collectors.joining(",", "{\"answers\":{", "}}"));
    }

    private HttpResponse<String> send(HttpRequest.Builder builder) throws Exception {
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
    }

    private HttpResponse<String> postJson(String body) throws Exception {
        return send(request("/api/responses")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body)));
    }

    private void assertError(HttpResponse<String> response, int status, String code) {
        assertEquals(status, response.statusCode(), response.body());
        assertTrue(response.body().contains("\"error\":\"" + code + "\""), response.body());
    }

    @Test
    void listsSixteenQuestionsWithoutExposingReverseFlag() throws Exception {
        HttpResponse<String> response = send(request("/api/questions").GET());

        assertEquals(200, response.statusCode());
        assertEquals(16, response.body().split("\"id\":\"q").length - 1);
        assertFalse(response.body().contains("reverse"));
    }

    @Test
    void scoresACompleteSubmission() throws Exception {
        HttpResponse<String> response = postJson(completeAnswers(3));

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"domains\""));
        assertTrue(response.body().contains("\"average\":3.0"));
    }

    @Test
    void rejectsInvalidContentWithAllProblemsListed() throws Exception {
        HttpResponse<String> response = postJson("{\"answers\":{\"q1\":9,\"q99\":3}}");

        assertError(response, 400, "respostas_invalidas");
        assertTrue(response.body().contains("q99"));
        assertTrue(response.body().contains("q1"));
    }

    @Test
    void rejectsMissingAnswersField() throws Exception {
        assertError(postJson("{}"), 400, "requisicao_invalida");
    }

    @Test
    void rejectsMoreEntriesThanQuestions() throws Exception {
        String body = IntStream.rangeClosed(1, 17)
            .mapToObj(i -> "\"q" + i + "\":3")
            .collect(Collectors.joining(",", "{\"answers\":{", "}}"));

        assertError(postJson(body), 400, "requisicao_invalida");
    }

    @Test
    void rejectsUnknownTopLevelField() throws Exception {
        String body = completeAnswers(3).replaceFirst("\\}$", ",\"nome\":\"Maria\"}");

        HttpResponse<String> response = postJson(body);

        assertError(response, 400, "requisicao_invalida");
        assertFalse(response.body().contains("Maria"));
    }

    @Test
    void rejectsMalformedJsonAsClientError() throws Exception {
        assertError(postJson("{\"answers\":"), 400, "requisicao_invalida");
    }

    @Test
    void rejectsNonNumericValueAsClientError() throws Exception {
        HttpResponse<String> response = postJson("{\"answers\":{\"q1\":\"peso 80kg\"}}");

        assertError(response, 400, "requisicao_invalida");
        assertFalse(response.body().contains("80kg"));
    }

    @Test
    void rejectsWrongContentType() throws Exception {
        HttpResponse<String> response = send(request("/api/responses")
            .header("Content-Type", "text/plain")
            .POST(HttpRequest.BodyPublishers.ofString(completeAnswers(3))));

        assertError(response, 415, "tipo_de_conteudo_nao_suportado");
    }

    @Test
    void rejectsUnsupportedMethod() throws Exception {
        assertError(send(request("/api/responses").DELETE()), 405, "metodo_nao_permitido");
    }

    @Test
    void unknownRouteReturnsNotFound() throws Exception {
        assertError(send(request("/api/admin").GET()), 404, "recurso_nao_encontrado");
    }

    @Test
    void healthEndpointIsUp() throws Exception {
        HttpResponse<String> response = send(request("/actuator/health").GET());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("UP"));
    }

    @Test
    void sensitiveActuatorEndpointsAreNotExposed() throws Exception {
        for (String path : new String[] {"/actuator/env", "/actuator/beans", "/actuator/heapdump"}) {
            assertNotEquals(200, send(request(path).GET()).statusCode(), path);
        }
    }

    @Test
    void corsAllowsConfiguredOrigin() throws Exception {
        HttpResponse<String> response = send(request("/api/responses")
            .header("Origin", ALLOWED_ORIGIN)
            .header("Access-Control-Request-Method", "POST")
            .header("Access-Control-Request-Headers", "Content-Type")
            .method("OPTIONS", HttpRequest.BodyPublishers.noBody()));

        assertEquals(200, response.statusCode());
        assertEquals(ALLOWED_ORIGIN, response.headers().firstValue("Access-Control-Allow-Origin").orElse(null));
    }

    @Test
    void corsBlocksUnknownOrigin() throws Exception {
        HttpResponse<String> response = send(request("/api/responses")
            .header("Origin", "https://site-malicioso.example")
            .header("Access-Control-Request-Method", "POST")
            .method("OPTIONS", HttpRequest.BodyPublishers.noBody()));

        assertEquals(403, response.statusCode());
        assertTrue(response.headers().firstValue("Access-Control-Allow-Origin").isEmpty());
    }
}
