package imigration.api.model.update;

import java.util.Set;

public record UserUpdate(
    Boolean isEnabled,
    Set<String> authorities
) {}
