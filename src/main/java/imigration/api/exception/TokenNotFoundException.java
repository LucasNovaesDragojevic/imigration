package imigration.api.exception;

import org.springframework.http.HttpStatus;

import imigration.api.model.enums.Error;

public class TokenNotFoundException extends ApiException {

    public TokenNotFoundException() {
        super(Error.E1007, HttpStatus.NOT_FOUND, Error.E1007.getTitle());
    }
}
