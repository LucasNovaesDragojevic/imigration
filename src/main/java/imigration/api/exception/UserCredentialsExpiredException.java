package imigration.api.exception;

import org.springframework.http.HttpStatus;

import imigration.api.model.enums.Error;

public class UserCredentialsExpiredException extends ApiException {

    public UserCredentialsExpiredException() {
        super(Error.E1003, HttpStatus.BAD_REQUEST, Error.E1003.getTitle());
    }
}
