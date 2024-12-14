package imigration.api.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    
    public EmailService(
        final JavaMailSender javaMailSender
    ) {
        this.javaMailSender = javaMailSender;
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
