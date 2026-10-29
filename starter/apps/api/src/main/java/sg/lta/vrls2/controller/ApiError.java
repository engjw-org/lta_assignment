package sg.lta.vrls2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/** Only fixed public text is returned; exception messages and request values stay out of responses. */
public record ApiError(String code, String message) {
    static ApiError forStatus(HttpStatusCode status) {
        if (status.is5xxServerError()) {
            return new ApiError("INTERNAL_ERROR", "Unable to process request.");
        }
        HttpStatus known = HttpStatus.resolve(status.value());
        return known == null ? new ApiError("REQUEST_ERROR", "Unable to process request.")
                : new ApiError(known.name(), known.getReasonPhrase() + ".");
    }
}
