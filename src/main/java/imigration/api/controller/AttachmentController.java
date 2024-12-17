package imigration.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import imigration.api.constant.Url;
import imigration.api.model.response.AttachmentResponse;
import imigration.api.service.AttachmentService;

@RestController
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(
        final AttachmentService attachmentService
    ) {
        this.attachmentService = attachmentService;
    }

    @GetMapping(Url.ATTACHMENTS_BY_COMMENT)
    public List<AttachmentResponse> findAllByCommentId(
        @PathVariable final Integer id
    ) {
        return attachmentService.findAllByCommentId(id).stream().map(AttachmentResponse::new).toList();
    }
}
