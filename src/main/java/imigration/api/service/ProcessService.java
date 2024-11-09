package imigration.api.service;

import org.springframework.stereotype.Service;

import imigration.api.model.entity.Attachment;
import imigration.api.model.entity.Comment;
import imigration.api.model.entity.Process;
import imigration.api.model.enums.Step;
import imigration.api.model.request.ProcessRequest;
import imigration.api.model.response.ProcessResponse;
import imigration.api.repository.ProcessRepository;

@Service
public class ProcessService {

    private final ProcessRepository processRepository;
    
    public ProcessService(final ProcessRepository processRepository) {
        this.processRepository = processRepository;
    }

    public ProcessResponse post(final ProcessRequest processRequest) {
        final var process = new Process();
        process.setStep(Step.SEND_DOCUMENTS);
        process.setNationality(processRequest.nationality());
        process.setDateBirth(processRequest.dateBirth());
        process.setPassport(processRequest.passport());
        process.setGovId(processRequest.govId());
        process.setDriverLicense(processRequest.driverLicense());
        final var comment = new Comment();
        comment.setContent(processRequest.comment().content());
        final var attachments = processRequest.comment().attachments().stream().map(Attachment::new).toList();
        comment.addAttachment(attachments);
        process.addComment(comment);
        processRepository.save(process);
        return new ProcessResponse(process);
    }
}
