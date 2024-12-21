package imigration.api.model.request;

import imigration.api.validator.ValidPassword;

public record PasswordRequest(@ValidPassword String password) {}