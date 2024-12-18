package imigration.api.exception;

import org.springframework.http.HttpStatus;

import imigration.api.model.enums.Error;

public class UserAccountExpiredException extends ApiException {

    public UserAccountExpiredException() {
        super(Error.E1004, HttpStatus.BAD_REQUEST, Error.E1004.getTitle());
    }
}
