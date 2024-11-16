package imigration.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import imigration.api.model.entity.Process;
import imigration.api.model.entity.User;

@Repository
public interface ProcessRepository extends JpaRepository<Process, Integer> {

    Page<Process> findAllByOwner(final Pageable pageable, final User owner);

    Optional<Process> findByIdAndOwner(final Integer id, final User owner);

}
