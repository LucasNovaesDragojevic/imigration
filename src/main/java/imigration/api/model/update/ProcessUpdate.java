package imigration.api.model.update;

import imigration.api.model.enums.Step;

public record ProcessUpdate(
    Integer owner,
    Step step) {}
