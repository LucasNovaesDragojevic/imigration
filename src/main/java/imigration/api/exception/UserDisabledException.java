package imigration.api.exception;

import org.springframework.http.HttpStatus;

import imigration.api.model.enums.Error;

public class UserDisabledException extends ApiException {

    public UserDisabledException() {
        super(Error.E1005, HttpStatus.BAD_REQUEST, Error.E1005.getTitle());
    }
}
