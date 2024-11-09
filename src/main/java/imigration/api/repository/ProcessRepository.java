package imigration.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import imigration.api.model.entity.Process;

@Repository
public interface ProcessRepository extends JpaRepository<Process, Integer> {

}
