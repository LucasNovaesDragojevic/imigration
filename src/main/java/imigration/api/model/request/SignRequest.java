package imigration.api.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignRequest(@Email String username, @NotBlank String password) {}
