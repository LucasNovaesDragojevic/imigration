package imigration.api.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import imigration.api.model.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    Page<Comment> findAllByProcessIdAndProcessOwnerId(final Integer processId, final Integer processOwnerId, final Pageable pageable);
    
    List<Comment> findAllByProcessId(final Integer processId);
}
