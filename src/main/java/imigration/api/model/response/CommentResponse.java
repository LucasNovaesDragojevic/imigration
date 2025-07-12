package imigration.api.model.response;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import imigration.api.model.entity.Attachment;
import imigration.api.model.entity.Comment;

public record CommentResponse(
    Integer id,
    LocalDateTime createdAt, 
    Integer owner, 
    String content, 
    List<Integer> attachments
) {

    public CommentResponse(final Comment comment, final List<Attachment> attachments) {
        this(comment.getId(),
                LocalDateTime.ofInstant(comment.getCreatedAt(), ZoneId.systemDefault()), 
                comment.getOwner().getId(), 
                comment.getContent(),
                attachments.stream().map(Attachment::getId).toList());
    }

    public CommentResponse(final Comment comment) {
        this(comment, Collections.emptyList());
    }
}
