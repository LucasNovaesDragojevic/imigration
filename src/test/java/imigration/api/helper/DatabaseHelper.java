package imigration.api.helper;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import imigration.api.model.entity.Attachment;
import imigration.api.model.entity.Authority;
import imigration.api.model.entity.Comment;
import imigration.api.model.entity.Process;
import imigration.api.model.entity.User;
import imigration.api.repository.AttachmentRepository;
import imigration.api.repository.AuthorityRepository;
import imigration.api.repository.CommentRepository;
import imigration.api.repository.PasswordResetTokenRepository;
import imigration.api.repository.ProcessRepository;
import imigration.api.repository.UserRepository;
import imigration.api.repository.VerificationTokenRepository;

@Component
public class DatabaseHelper {

    private final AttachmentRepository attachmentRepository;
    private final CommentRepository commentRepository;
    private final ProcessRepository processRepository;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    public DatabaseHelper(final AttachmentRepository attachmentRepository,
                          final CommentRepository commentRepository,
                          final ProcessRepository processRepository,
                          final UserRepository userRepository,
                          final AuthorityRepository authorityRepository,
                          final PasswordResetTokenRepository passwordResetTokenRepository,
                          final VerificationTokenRepository verificationTokenRepository) {
        this.attachmentRepository = attachmentRepository;
        this.commentRepository = commentRepository;
        this.processRepository = processRepository;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    public void clearDatabase() {
        attachmentRepository.deleteAll();
        commentRepository.deleteAll();
        processRepository.deleteAll();
        userRepository.deleteAll();
        authorityRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        verificationTokenRepository.deleteAll();
    }

    public Set<Authority> saveAll(final Set<Authority> authorities) {
        return authorityRepository.saveAll(authorities).stream().collect(Collectors.toSet());
    }

    public User save(final User user) {
        return this.userRepository.save(user);
    }

    public Process save(final Process process) {
        return this.processRepository.save(process);
    }

    public Comment save(final Comment comment) {
        return this.commentRepository.save(comment);
    }

    public Attachment save(final Attachment attachment) {
        return this.attachmentRepository.save(attachment);
    }
}
