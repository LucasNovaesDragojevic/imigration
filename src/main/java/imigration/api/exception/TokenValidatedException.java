package imigration.api.exception;

import org.springframework.http.HttpStatus;

import imigration.api.model.enums.Error;

public class TokenValidatedException extends ApiException {

    public TokenValidatedException() {
        super(Error.E1008, HttpStatus.BAD_REQUEST, Error.E1008.getTitle());
    }
}
