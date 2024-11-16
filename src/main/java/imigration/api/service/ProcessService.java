package imigration.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import imigration.api.model.entity.Attachment;
import imigration.api.model.entity.Comment;
import imigration.api.model.entity.Process;
import imigration.api.model.entity.User;
import imigration.api.model.enums.Step;
import imigration.api.model.request.CommentRequest;
import imigration.api.model.request.ProcessRequest;
import imigration.api.model.response.ProcessMinimalResponse;
import imigration.api.model.response.ProcessResponse;
import imigration.api.repository.ProcessRepository;

@Service
public class ProcessService {

    private final ProcessRepository processRepository;
    
    public ProcessService(final ProcessRepository processRepository) {
        this.processRepository = processRepository;
    }

    public ProcessResponse post(final ProcessRequest processRequest, final User owner) {
        final var process = new Process();
        process.setOwner(owner);
        process.setStep(Step.SEND_DOCUMENTS);
        process.setNationality(processRequest.nationality());
        process.setDateBirth(processRequest.dateBirth());
        process.setPassport(processRequest.passport());
        process.setGovId(processRequest.govId());
        process.setDriverLicense(processRequest.driverLicense());
        final var comment = new Comment();
        comment.setOwner(owner);
        comment.setContent(processRequest.comment().content());
        final var attachments = processRequest.comment().attachments().stream().map(Attachment::new).toList();
        comment.addAttachments(attachments);
        process.addComment(comment);
        processRepository.save(process);
        return new ProcessResponse(process);
    }
    
    public Page<ProcessMinimalResponse> findAllByOwner(final Pageable pageable, final User owner) {
        return processRepository.findAllByOwner(pageable, owner).map(ProcessMinimalResponse::new);
    }

    public ProcessResponse findByIdAndOwner(final Integer id, final User owner) {
        return processRepository.findByIdAndOwner(id, owner).map(ProcessResponse::new).get();
    }

    @Transactional
    public Comment createComment(final Integer id, final User owner, final CommentRequest commentRequest) {
        final var comment = new Comment();
        comment.setOwner(owner);
        comment.setContent(commentRequest.content());
        final var attachments = commentRequest.attachments();
        if (attachments != null && !attachments.isEmpty())
            comment.addAttachments(attachments.stream().map(Attachment::new).toList());
        processRepository.findById(id).get().addComment(comment);
        return comment;
    }
}
