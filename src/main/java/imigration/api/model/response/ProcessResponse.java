package imigration.api.model.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import imigration.api.model.enums.Country;
import imigration.api.model.enums.Step;
import imigration.api.model.entity.Comment;
import imigration.api.model.entity.Process;

public record ProcessResponse(
    Integer id, 
    Instant createAt, 
    Step step, 
    Country nationality,
    LocalDate dateBirth,
    String passport,
    String govId,
    String driverLicense,
    List<Integer> comments
) {
    public ProcessResponse(final Process process) {
        this(process.getId(), 
                process.getCreatedAt(), 
                process.getStep(), 
                process.getNationality(), 
                process.getDateBirth(), 
                process.getPassport(),
                process.getGovId(), 
                process.getDriverLicense(),
                process.getComments().stream().map(Comment::getId).toList());
    }
}
