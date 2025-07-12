package imigration.api.model.request;

import java.time.LocalDate;

import imigration.api.model.enums.Country;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProcessRequest(
    @NotNull Country nationality,
    @NotNull LocalDate dateBirth,
    @NotBlank String passport, 
    String govId, 
    String driverLicense,
    @NotNull
    CommentRequest comment
) {}
