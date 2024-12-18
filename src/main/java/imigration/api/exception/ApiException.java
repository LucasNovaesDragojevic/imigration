package imigration.api.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

import imigration.api.model.enums.Error;

public class ApiException extends ResponseStatusException {

    public ApiException(final Error error, final HttpStatusCode httpStatus, final String detail) {
        super(httpStatus, detail);
        super.setTitle(error.getTitle());
        super.getBody().setProperty("code", error);
    }
}
