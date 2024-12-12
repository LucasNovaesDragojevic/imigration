package imigration.api.service;

import java.time.Instant;

import org.springframework.http.HttpStatusCode;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import imigration.api.model.entity.User;
import imigration.api.model.entity.VerificationToken;
import imigration.api.repository.VerificationTokenRepository;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final VerificationTokenRepository verificationTokenRepository;

    public EmailService(
        final JavaMailSender javaMailSender, 
        final VerificationTokenRepository verificationTokenRepository
    ) {
        this.javaMailSender = javaMailSender;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @Async
    public void sendVerification(final String email, final String token) {
        final var message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Verify your e-mail");
        message.setText("http://localhost:8080/email-verification/" + token);
        javaMailSender.send(message);
    }

    @Async
    public void sendRecovery(final String email, final String token) {
        final var message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Recovery password");
        message.setText("http://localhost:8080/passwords/reset/" + token);
        javaMailSender.send(message);
    }
}
