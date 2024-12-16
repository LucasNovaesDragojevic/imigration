package imigration.api.service;

import java.util.Set;

import org.springframework.stereotype.Service;

import imigration.api.model.entity.Authority;
import imigration.api.model.enums.AuthorityName;
import imigration.api.repository.AuthorityRepository;

@Service
public class AuthorityService {

    private final AuthorityRepository authorityRepository;

    public AuthorityService(final AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    public Set<Authority> getDefaultSignupAuthorities() {
        return authorityRepository.findByNameIn(
            Set.of(AuthorityName.PROCESS_CREATE, 
                    AuthorityName.PROCESS_READ,
                    AuthorityName.PROCESS_UPDATE,
                    AuthorityName.PROCESS_DELETE)
        );
    }

    public Set<Authority> findByNameIn(final Set<AuthorityName> authorities) {
        return authorityRepository.findByNameIn(authorities);
    }

}
