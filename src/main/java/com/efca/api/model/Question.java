package com.efca.api.model;

public record Question(String id, Domain domain, String text, boolean reverseScored) {

    public Question(String id, Domain domain, String text) {
        this(id, domain, text, false);
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 1cbb7d1d45233d4757ce4dcf49b93b1ca8a135d4
