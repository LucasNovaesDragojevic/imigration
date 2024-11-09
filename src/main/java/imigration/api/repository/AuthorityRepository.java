package imigration.api.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import imigration.api.model.Authority;
import imigration.api.model.AuthorityName;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Integer> {

    Set<Authority> findByNameIn(final Set<AuthorityName> authorities);

}
