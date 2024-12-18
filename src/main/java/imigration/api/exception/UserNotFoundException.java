package imigration.api.exception;

import org.springframework.http.HttpStatus;

import imigration.api.model.enums.Error;

public class UserNotFoundException extends ApiException {

    public UserNotFoundException() {
        super(Error.E1012, HttpStatus.NOT_FOUND, Error.E1012.getTitle());
    }
}
