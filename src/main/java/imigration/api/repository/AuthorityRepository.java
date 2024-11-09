package imigration.api.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import imigration.api.model.entity.Authority;
import imigration.api.model.enums.AuthorityName;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Integer> {

    Set<Authority> findByNameIn(final Set<AuthorityName> authorities);

}
