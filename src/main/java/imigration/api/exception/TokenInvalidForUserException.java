package imigration.api.exception;

import org.springframework.http.HttpStatus;

import imigration.api.model.enums.Error;

public class TokenInvalidForUserException extends ApiException {

    public TokenInvalidForUserException() {
        super(Error.E1011, HttpStatus.BAD_REQUEST, "It is not possible to reset the password of other user.");
    }
}
