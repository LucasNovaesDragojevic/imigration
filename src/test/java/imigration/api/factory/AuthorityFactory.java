package imigration.api.factory;

import java.util.Set;

import org.springframework.stereotype.Component;

import imigration.api.model.entity.Authority;
import imigration.api.model.enums.AuthorityName;

@Component
public class AuthorityFactory {

    public Set<Authority> fabricAll() {
        return Set.of(
            new Authority(AuthorityName.PROCESS_CREATE),
            new Authority(AuthorityName.PROCESS_READ),
            new Authority(AuthorityName.PROCESS_UPDATE),
            new Authority(AuthorityName.PROCESS_DELETE),
            new Authority(AuthorityName.PROCESS_REVIEWER),
            new Authority(AuthorityName.COMMENT_CREATE),
            new Authority(AuthorityName.COMMENT_READ),
            new Authority(AuthorityName.COMMENT_UPDATE),
            new Authority(AuthorityName.COMMENT_DELETE),
            new Authority(AuthorityName.USER_READ),
            new Authority(AuthorityName.USER_UPDATE)
        );
    }
}
