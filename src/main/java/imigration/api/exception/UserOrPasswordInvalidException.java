package imigration.api.exception;

import org.springframework.http.HttpStatus;

import imigration.api.model.enums.Error;

public class UserOrPasswordInvalidException extends ApiException {

    public UserOrPasswordInvalidException() {
        super(Error.E1006, HttpStatus.BAD_REQUEST, Error.E1006.getTitle());
    }
}
