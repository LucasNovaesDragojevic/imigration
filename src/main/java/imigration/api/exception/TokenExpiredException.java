package imigration.api.exception;

import org.springframework.http.HttpStatus;

import imigration.api.model.enums.Error;

public class TokenExpiredException extends ApiException {

    public TokenExpiredException() {
        super(Error.E1009, HttpStatus.BAD_REQUEST, Error.E1009.getTitle());
    }
}
