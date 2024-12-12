package imigration.api.model.request;

import jakarta.validation.constraints.Email;

public record EmailRequest(@Email String email) {}
