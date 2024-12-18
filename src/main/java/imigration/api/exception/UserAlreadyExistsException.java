package imigration.api.exception;

import org.springframework.http.HttpStatus;

import imigration.api.model.enums.Error;

public class UserAlreadyExistsException extends ApiException {

    public UserAlreadyExistsException() {
        super(Error.E1001, HttpStatus.BAD_REQUEST, Error.E1001.getTitle());
    }
}
