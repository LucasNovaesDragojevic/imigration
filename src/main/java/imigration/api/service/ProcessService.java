package imigration.api.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import imigration.api.model.entity.Process;
import imigration.api.model.entity.User;
import imigration.api.model.enums.Step;
import imigration.api.model.request.ProcessRequest;
import imigration.api.model.response.CommentResponse;
import imigration.api.model.response.ProcessMinimalResponse;
import imigration.api.model.response.ProcessResponse;
import imigration.api.repository.AttachmentRepository;
import imigration.api.repository.CommentRepository;
import imigration.api.repository.ProcessRepository;

//TODO Remake persistence and access of comments and attachments values.

@Service
public class ProcessService {

    private final ProcessRepository processRepository;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    
    public ProcessService(final ProcessRepository processRepository,
                            final CommentRepository commentRepository,
                            final AttachmentRepository attachmentRepository) {
        this.processRepository = processRepository;
        this.commentRepository = commentRepository;
        this.attachmentRepository = attachmentRepository;
    }

    public Process create(final ProcessRequest processRequest, final User owner) {
        final var process = new Process();
        process.setOwner(owner);
        process.setStep(Step.SEND_DOCUMENTS);
        process.setNationality(processRequest.nationality());
        process.setDateBirth(processRequest.dateBirth());
        process.setPassport(processRequest.passport());
        process.setGovId(processRequest.govId());
        process.setDriverLicense(processRequest.driverLicense());
        // final var comment = new Comment();
        // comment.setOwner(owner);
        // comment.setProcess(process);
        // comment.setContent(processRequest.comment().content());
        // final var attachments = processRequest.comment().attachments().stream().map(Attachment::new).toList();
        // attachments.forEach(a -> a.setComment(comment));
        processRepository.save(process);
        // commentRepository.save(comment);
        // attachmentRepository.saveAll(attachments);
        // return new ProcessResponse(process, comment);
        return process;
    }
    
    public Page<ProcessMinimalResponse> findAllByOwner(final Pageable pageable, final User owner) {
        return processRepository.findAllByOwner(pageable, owner).map(ProcessMinimalResponse::new);
    }

    public ProcessResponse findByIdAndOwner(final Integer id, final User owner) {
        return new ProcessResponse(processRepository.findByIdAndOwner(id, owner).get(), commentRepository.findAllByProcessId(id));
    }

    public Page<CommentResponse> findAllByProcessId(final Integer id, final Pageable pageable) {
        final var comments = commentRepository.findAllByProcessId(id, pageable);
        return comments.map(c -> {
            final var attachments = attachmentRepository.findAllByCommentId(c.getId());
            return new CommentResponse(c, attachments);
        });
    }

    public Process findById(final Integer id) {
        return processRepository.findById(id).get();
    }
}
