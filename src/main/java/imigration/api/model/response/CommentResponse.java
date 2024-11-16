package imigration.api.model.response;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import imigration.api.model.entity.Attachment;
import imigration.api.model.entity.Comment;

public record CommentResponse(
    LocalDateTime createAt, 
    Integer owner, 
    String content, 
    List<Integer> attachments
) {

    public CommentResponse(final Comment comment) {
        this(LocalDateTime.ofInstant(comment.getCreatedAt(), ZoneId.systemDefault()), 
                comment.getOwner().getId(), 
                comment.getContent(), 
                comment.getAttachments().stream().map(Attachment::getId).toList());
    }
}
