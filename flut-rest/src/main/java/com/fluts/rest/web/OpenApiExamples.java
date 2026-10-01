package com.fluts.rest.web;

/** Examples shown in Swagger UI: the two scenarios of the specification. */
final class OpenApiExamples {

    static final String JSON_INPUT = """
            {
              "scenarios": [
                { "name": "Example 1", "schuurs": [ { "boxPrices": [12, 3, 10, 7, 16, 5] } ] },
                { "name": "Example 2", "schuurs": [
                    { "boxPrices": [7, 3, 11, 9, 10] },
                    { "boxPrices": [1, 2, 3, 4, 10, 16, 10, 4, 16] } ] }
              ]
            }""";

    static final String TEXT_OUTPUT = """
            schuurs 1
            Maximum profit is 8.
            Number of fluts to buy: 4

            schuurs 2
            Maximum profit is 40.
            Number of fluts to buy: 6 7 8 9 10 12 13
            """;

    static final String PROBLEM = """
            { "type": "about:blank", "title": "Invalid input", "status": 400,
              "detail": "Schuur declares 3 boxes but lists 2 prices", "line": 2 }""";

    private OpenApiExamples() {
    }
}
