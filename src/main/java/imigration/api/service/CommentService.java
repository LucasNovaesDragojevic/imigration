package imigration.api.service;

import org.springframework.stereotype.Service;

import imigration.api.model.entity.Comment;
import imigration.api.model.entity.Process;
import imigration.api.model.entity.User;
import imigration.api.model.request.CommentRequest;
import imigration.api.repository.CommentRepository;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Comment create(final Process process, final User owner, final CommentRequest commentRequest) {
        final var comment = new Comment();
        comment.setOwner(owner);
        comment.setProcess(process);
        comment.setContent(commentRequest.content());
        return commentRepository.save(comment);
    }

}
