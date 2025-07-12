package imigration.api.builder;

import java.util.UUID;

import org.springframework.stereotype.Component;

import imigration.api.model.entity.Attachment;
import imigration.api.model.entity.Comment;

@Component
public class AttachmentBuilder {

    private Attachment attachment;

    public AttachmentBuilder attachment() {
        this.attachment = new Attachment();
        return this;
    }

    public AttachmentBuilder withContent(final String content) {
        this.attachment.setContent(content);
        return this;
    }

    public AttachmentBuilder withComment(final Comment comment) {
        this.attachment.setComment(comment);
        return this;
    }

    public Attachment build() {
        return this.attachment;
    }

    public Attachment buildRandomAttachment(final Comment comment) {
        return this.attachment()
            .withComment(comment)
            .withContent(UUID.randomUUID().toString())
            .build();
    }
}
