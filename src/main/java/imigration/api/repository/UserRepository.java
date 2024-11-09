package imigration.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import imigration.api.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    User findByUsername(final String username);

}
