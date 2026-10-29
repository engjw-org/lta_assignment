package sg.lta.vrls2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import sg.lta.vrls2.service.VehicleService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Real HTTP/error dispatch: standalone MockMvc would miss Spring Boot's /error response. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class ApiErrorSecurityTest {
    private static final String UUID = "00000000-0000-4000-8000-000000000001";
    private static final String INTERNAL = "issue7-diagnostic /private/server/config.csv java.lang.IllegalStateException Framework/9.9.9";
    @Autowired TestRestTemplate client;
    @Autowired ObjectMapper json;
    @MockBean VehicleService service;

    @BeforeEach
    void emptyData() {
        when(service.all()).thenReturn(List.of());
        when(service.findByUuid(any())).thenReturn(Optional.empty());
    }

    record ErrorCase(HttpMethod method, String path, String body, int status, String code) { }

    static Stream<ErrorCase> invalidRequests() {
        return Stream.of(
                new ErrorCase(HttpMethod.POST, "/api/vehicles/search", "{broken-json", 400, "BAD_REQUEST"),
                new ErrorCase(HttpMethod.POST, "/api/vehicles/search", "{\"query\":[]}", 400, "BAD_REQUEST"),
                new ErrorCase(HttpMethod.POST, "/api/vehicles/search", "{\"startDate\":\"2026-02-30\"}", 400, "BAD_REQUEST"),
                new ErrorCase(HttpMethod.GET, "/api/vehicles?uuid=invalid-issue7-diagnostic", null, 400, "BAD_REQUEST"),
                new ErrorCase(HttpMethod.GET, "/api/vehicles?page=not-a-number", null, 400, "BAD_REQUEST"),
                new ErrorCase(HttpMethod.GET, "/api/vehicles?page=-1", null, 400, "BAD_REQUEST"),
                new ErrorCase(HttpMethod.GET, "/api/vehicles/" + UUID, null, 404, "NOT_FOUND"),
                new ErrorCase(HttpMethod.GET, "/api/unknown-issue7-diagnostic", null, 404, "NOT_FOUND"),
                new ErrorCase(HttpMethod.DELETE, "/api/vehicles/" + UUID, null, 405, "METHOD_NOT_ALLOWED"));
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void clientErrorsUseAControlledEnvelopeWithoutInternalDetails(ErrorCase request) throws Exception {
        assertEnvelope(send(request.method(), request.path(), request.body()), request.status(), request.code());
    }

    @ParameterizedTest
    @ValueSource(strings = {"list", "single", "search"})
    void unexpectedFailuresAreSafeAcrossServingPaths(String route) throws Exception {
        when(service.all()).thenThrow(new IllegalStateException(INTERNAL));
        when(service.findByUuid(any())).thenThrow(new IllegalStateException(INTERNAL));
        when(service.search(any())).thenThrow(new IllegalStateException(INTERNAL));
        ResponseEntity<String> response = switch (route) {
            case "search" -> send(HttpMethod.POST, "/api/vehicles/search", "{}");
            case "single" -> send(HttpMethod.GET, "/api/vehicles/" + UUID, null);
            default -> send(HttpMethod.GET, "/api/vehicles", null);
        };
        assertEnvelope(response, 500, "INTERNAL_ERROR");
    }

    private ResponseEntity<String> send(HttpMethod method, String path, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return client.exchange(path, method, new HttpEntity<>(body, headers), String.class);
    }

    private void assertEnvelope(ResponseEntity<String> response, int status, String code) throws Exception {
        assertThat(response.getStatusCode().value()).isEqualTo(status);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue();
        String body = response.getBody();
        assertThat(body).isNotBlank().doesNotContain("issue7-diagnostic", "/private/", "java.lang.",
                "Framework/9.9.9", "org.springframework", "org.apache", "stackTrace");
        JsonNode envelope = json.readTree(body);
        List<String> fields = new ArrayList<>();
        envelope.fieldNames().forEachRemaining(fields::add);
        assertThat(fields).as("R-6.6 deliberate public error contract, no default diagnostic fields")
                .containsExactlyInAnyOrder("code", "message");
        assertThat(envelope.path("code").asText()).isEqualTo(code);
        assertThat(envelope.path("message").asText()).isNotBlank();
        assertThat(response.getHeaders().getFirst("Server")).isNull();
        assertThat(response.getHeaders().getFirst("X-Powered-By")).isNull();
    }
}
