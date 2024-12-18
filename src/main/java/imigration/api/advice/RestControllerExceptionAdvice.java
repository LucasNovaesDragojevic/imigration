package imigration.api.advice;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import imigration.api.exception.ApiException;
import imigration.api.model.enums.Error;
import imigration.api.model.response.InvalidFieldResponse;

@RestControllerAdvice
public class RestControllerExceptionAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestControllerExceptionAdvice.class);

    @ExceptionHandler(ApiException.class)
    public ErrorResponse processException(final ApiException exception) {
        LOGGER.error(exception.getMessage());
        return exception;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse processException(final MethodArgumentNotValidException exception) {
        exception.getBody()
        .setProperties(Map.of("code", Error.E1000, "invalid-fields", 
            exception.getBindingResult()
            .getAllErrors()
            .stream()
            .map(FieldError.class::cast)
            .map(InvalidFieldResponse::new)
            .toList()));
        return exception;
    }
}
