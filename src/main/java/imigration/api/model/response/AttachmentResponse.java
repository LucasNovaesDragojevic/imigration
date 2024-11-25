package imigration.api.model.response;

import imigration.api.model.entity.Attachment;

public record AttachmentResponse(String content) {

    public AttachmentResponse(final Attachment attachment) {
        this(attachment.getContent());
    }
}
