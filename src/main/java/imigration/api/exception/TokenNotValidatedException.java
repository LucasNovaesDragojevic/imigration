package imigration.api.exception;

import org.springframework.http.HttpStatus;

import imigration.api.model.enums.Error;

public class TokenNotValidatedException extends ApiException {

    public TokenNotValidatedException() {
        super(Error.E1010, HttpStatus.BAD_REQUEST, "The user already have requested a recovery password token.");
    }
}
