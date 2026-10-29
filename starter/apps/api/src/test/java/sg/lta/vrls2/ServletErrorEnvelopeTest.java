package sg.lta.vrls2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import sg.lta.vrls2.controller.ApiErrorController;

import static org.assertj.core.api.Assertions.assertThat;

/** Complements the HTTP tests: servlet errors can reach /error without controller advice. */
class ServletErrorEnvelopeTest {
    @ParameterizedTest
    @CsvSource({"400,400,BAD_REQUEST", "404,404,NOT_FOUND", "500,500,INTERNAL_ERROR", ",500,INTERNAL_ERROR"})
    void servletDispatchAndDirectErrorRequestsExposeOnlySafeFields(Integer dispatchedStatus, int expectedStatus,
                                                                  String expectedCode) throws Exception {
        var request = new MockHttpServletRequest();
        if (dispatchedStatus != null) request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, dispatchedStatus);
        request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, new IllegalStateException("/private/config.csv"));
        request.setAttribute(RequestDispatcher.ERROR_MESSAGE, "internal framework diagnostic");
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/private/endpoint");
        var response = new ApiErrorController().error(request);
        assertThat(response.getStatusCode().value()).isEqualTo(expectedStatus);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        String body = new ObjectMapper().writeValueAsString(response.getBody());
        var fields = new ObjectMapper().readTree(body);
        assertThat(fields.size()).isEqualTo(2);
        assertThat(fields.path("code").asText()).isEqualTo(expectedCode);
        assertThat(fields.path("message").asText()).isNotBlank();
        assertThat(body).doesNotContain("/private", "framework", "IllegalStateException");
    }
}
