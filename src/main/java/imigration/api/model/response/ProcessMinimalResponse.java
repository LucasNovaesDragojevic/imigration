package imigration.api.model.response;

import java.time.LocalDateTime;
import java.time.ZoneId;

import imigration.api.model.entity.Process;
import imigration.api.model.enums.Step;

public record ProcessMinimalResponse(
    Integer id,
    LocalDateTime createdAt,
    Step step
) {
    public ProcessMinimalResponse(final Process process) {
        this(process.getId(), LocalDateTime.ofInstant(process.getCreatedAt(), ZoneId.systemDefault()), process.getStep());
    }
}
