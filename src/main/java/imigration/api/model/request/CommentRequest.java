package imigration.api.model.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record CommentRequest(
    @NotBlank String content,
    List<String> attachments
) {}
