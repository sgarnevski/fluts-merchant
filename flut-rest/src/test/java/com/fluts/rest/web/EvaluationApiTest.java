package com.fluts.rest.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/** The evaluation API end to end, through the full Spring context of the stateless app. */
@SpringBootTest(properties = {"flut.limits.max-body-size=2KB", "spring.servlet.multipart.max-file-size=2KB"})
@AutoConfigureMockMvc
class EvaluationApiTest {

    private static final Path SAMPLE_TEXT = Path.of("../demo/input.txt");
    private static final Path SAMPLE_JSON = Path.of("../demo/input.json");
    private static final Path SAMPLE_OUTPUT = Path.of("../demo/output.txt");

    private static final String EXPECTED_JSON = """
            {"results": [
              {"scenario": "Example 1", "maxProfit": 8, "flutCounts": [4]},
              {"scenario": "Example 2", "maxProfit": 40, "flutCounts": [6, 7, 8, 9, 10, 12, 13]}
            ]}""";

    private final MockMvcTester mvc;

    EvaluationApiTest(@Autowired final MockMvcTester mvc) {
        this.mvc = mvc;
    }

    @Test
    void evaluatesJsonBody() throws IOException {
        assertThat(mvc.post().uri("/api/evaluations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Files.readString(SAMPLE_JSON)))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson().isStrictlyEqualTo(EXPECTED_JSON);
    }

    @Test
    void answersInTheOriginalTextFormatOnRequest() throws IOException {
        assertThat(mvc.post().uri("/api/evaluations")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_PLAIN)
                .content(Files.readString(SAMPLE_JSON)))
                .hasStatusOk()
                .hasContentTypeCompatibleWith(MediaType.TEXT_PLAIN)
                .hasBodyTextEqualTo(Files.readString(SAMPLE_OUTPUT));
    }

    @Test
    void evaluatesUploadedTextFile() throws IOException {
        assertThat(mvc.post().uri("/api/evaluations")
                .multipart()
                .file(file("input.txt", SAMPLE_TEXT))
                .accept(MediaType.TEXT_PLAIN))
                .hasStatusOk()
                .hasBodyTextEqualTo(Files.readString(SAMPLE_OUTPUT));
    }

    @Test
    void evaluatesUploadedJsonFile() throws IOException {
        assertThat(mvc.post().uri("/api/evaluations")
                .multipart()
                .file(file("input.json", SAMPLE_JSON)))
                .hasStatusOk()
                .bodyJson().isStrictlyEqualTo(EXPECTED_JSON);
    }

    @Test
    void reportsInvalidTextWithLineNumber() {
        assertThat(mvc.post().uri("/api/evaluations")
                .multipart()
                .file(new MockMultipartFile("file", "bad.txt", "text/plain", "1\n3 1 2\n".getBytes(StandardCharsets.UTF_8))))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson().isLenientlyEqualTo("""
                        {"title": "Invalid input", "status": 400,
                         "detail": "Schuur declares 3 boxes but lists 2 prices", "line": 2}""");
    }

    @Test
    void reportsInvalidJsonContentWithoutLineNumber() {
        assertThat(mvc.post().uri("/api/evaluations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"scenarios\": []}"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().isLenientlyEqualTo("{\"detail\": \"No scenarios given\"}")
                .doesNotHavePath("$.line");
    }

    @Test
    void rejectsUnsupportedFileTypes() {
        assertThat(mvc.post().uri("/api/evaluations")
                .multipart()
                .file(new MockMultipartFile("file", "input.csv", "text/csv", "1".getBytes(StandardCharsets.UTF_8))))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.detail").isEqualTo("Unsupported file 'input.csv': upload a .txt or .json file");
    }

    @Test
    void rejectsUploadsWithoutFileName() {
        assertThat(mvc.post().uri("/api/evaluations")
                .multipart()
                .file(new MockMultipartFile("file", null, "text/plain", "0".getBytes(StandardCharsets.UTF_8))))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.detail").isEqualTo("Unsupported file '': upload a .txt or .json file");
    }

    @Test
    void rejectsMissingFilePart() {
        assertThat(mvc.post().uri("/api/evaluations").multipart())
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void rejectsTooLargeJsonBody() {
        assertThat(mvc.post().uri("/api/evaluations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(" ".repeat(3000) + "{}"))
                .hasStatus(HttpStatus.CONTENT_TOO_LARGE)
                .bodyJson().extractingPath("$.detail").isEqualTo("Request body is larger than 2048B");
    }

    @Test
    void rejectsTextBodies() {
        assertThat(mvc.post().uri("/api/evaluations")
                .contentType(MediaType.TEXT_PLAIN)
                .content("1\n1 5\n0\n"))
                .hasStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    void publishesOpenApiWithRequiredFields() {
        assertThat(mvc.get().uri("/v3/api-docs"))
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.paths['/api/evaluations'].post.requestBody.content['application/json'].example",
                        value -> assertThat(value).asString().contains("Example 2"))
                .hasPathSatisfying("$.components.schemas.EvaluationResult.required",
                        value -> assertThat(value).asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.LIST)
                                .containsExactlyInAnyOrder("scenario", "maxProfit", "flutCounts"))
                .hasPathSatisfying("$.components.schemas.ScenarioInput.required",
                        value -> assertThat(value).asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.LIST)
                                .containsExactly("schuurs"));
    }

    @Test
    void servesSwaggerUi() {
        assertThat(mvc.get().uri("/swagger-ui/index.html")).hasStatusOk();
    }

    private static MockMultipartFile file(final String name, final Path content) throws IOException {
        return new MockMultipartFile("file", name, MediaType.APPLICATION_OCTET_STREAM_VALUE, Files.readAllBytes(content));
    }
}
