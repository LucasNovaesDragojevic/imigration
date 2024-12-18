package imigration.api.model.response;

import org.springframework.validation.FieldError;

public record InvalidFieldResponse(
    String name,
    String reason
) {
    public InvalidFieldResponse(final FieldError fieldError) {
        this(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
