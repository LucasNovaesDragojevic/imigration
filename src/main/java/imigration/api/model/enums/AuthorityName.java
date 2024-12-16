package imigration.api.model.enums;

import java.util.HashSet;
import java.util.Set;

public enum AuthorityName {

    PROCESS_CREATE,
    PROCESS_READ,
    PROCESS_UPDATE,
    PROCESS_DELETE,
    PROCESS_REVIEWER,
    COMMENT_CREATE,
    COMMENT_READ,
    COMMENT_UPDATE,
    COMMENT_DELETE,
    USER_READ,
    USER_UPDATE;

    public static Set<AuthorityName> findByNameIn(final Set<String> authorities) {
        final var authorityNames = new HashSet<AuthorityName>();
        for (final var authority : authorities) {
            try {
                authorityNames.add(AuthorityName.valueOf(authority));
            } catch (final IllegalArgumentException e) {
                continue;
            }
        }
        return authorityNames;
    }
}
