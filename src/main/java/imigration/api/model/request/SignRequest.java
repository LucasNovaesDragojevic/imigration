package imigration.api.model.request;

import imigration.api.validator.ValidPassword;
import jakarta.validation.constraints.Email;

public record SignRequest(
    @Email String username,
    @ValidPassword String password
) {}
