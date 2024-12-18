package imigration.api.exception;

import org.springframework.http.HttpStatus;

import imigration.api.model.enums.Error;

public class UserLockedException extends ApiException {

    public UserLockedException() {
        super(Error.E1002, HttpStatus.BAD_REQUEST, Error.E1002.getTitle());
    }
}
