package imigration.api.builder;

import java.util.UUID;

import org.springframework.stereotype.Component;

import imigration.api.model.entity.Comment;
import imigration.api.model.entity.Process;
import imigration.api.model.entity.User;

@Component
public class CommentBuilder {

    private Comment comment;

    public CommentBuilder comment() {
        this.comment = new Comment();
        return this;
    }

    public CommentBuilder withOwner(final User owner) {
        this.comment.setOwner(owner);
        return this;
    }

    public CommentBuilder withContent(final String content) {
        this.comment.setContent(content);
        return this;
    }

    public CommentBuilder withProcess(final Process process) {
        this.comment.setProcess(process);
        return this;
    }

    public Comment build() {
        return this.comment;
    }

    public Comment buildRandomComment(final User owner, final Process process) {
        return this.comment()
            .withOwner(owner)
            .withContent(UUID.randomUUID().toString())
            .withProcess(process)
            .build();
    }
}
