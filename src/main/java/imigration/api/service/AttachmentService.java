package imigration.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import imigration.api.model.entity.Attachment;
import imigration.api.model.entity.Comment;
import imigration.api.model.entity.User;
import imigration.api.repository.AttachmentRepository;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    public AttachmentService(final AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    public List<Attachment> create(final User owner, final Comment comment, final List<String> attachmentsContents) {
        return attachmentRepository.saveAll(attachmentsContents.stream().map(content -> new Attachment(content, comment)).toList());
    }

    public List<Attachment> findAllByCommentId(final Integer commentId) {
        return attachmentRepository.findAllByCommentId(commentId);
    }
}
