package imigration.api.exception;

import org.springframework.http.HttpStatus;

import imigration.api.model.enums.Error;

public class ProcessNotFoundException extends ApiException {

    public ProcessNotFoundException() {
        super(Error.E1015, HttpStatus.NOT_FOUND, Error.E1015.getTitle());
    }
}
