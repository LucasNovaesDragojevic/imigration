package imigration.api.service;

import java.time.Instant;
import java.util.Optional;

import org.springframework.http.HttpStatusCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import imigration.api.model.entity.PasswordResetToken;
import imigration.api.model.entity.User;
import imigration.api.model.entity.VerificationToken;
import imigration.api.repository.PasswordResetTokenRepository;
import imigration.api.repository.UserRepository;
import imigration.api.repository.VerificationTokenRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public UserService(
        final UserRepository userRepository,
        final VerificationTokenRepository verificationTokenRepository,
        final PasswordResetTokenRepository passwordResetTokenRepository
    ) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    public User save(final User user) {
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(final String username) {
        return userRepository.findByUsername(username);
    }

    public User findById(final Integer id) {
        return userRepository.findById(id).get();
    }

    public String generateEmailValidationToken(final User user) {
        return verificationTokenRepository.save(new VerificationToken(user)).getToken();
    }

    @Transactional
    public void verifyEmailValidationToken(final String uuid) {
        final var token = verificationTokenRepository.findByToken(uuid).orElseThrow(() -> new ResponseStatusException(HttpStatusCode.valueOf(404)));
        if (token.getValidated() || token.getCreatedAt().isBefore(Instant.now().minusSeconds(360)))
            throw new ResponseStatusException(400, "Invalid token", null);
        token.getOwner().setIsEnabled(Boolean.TRUE);
        token.setValidated(true);
    }

    public String generatePasswordRecoveryToken(final User user) {
        return passwordResetTokenRepository.save(new PasswordResetToken(user)).getToken();
    }

    public Boolean hasPasswordResetToken(final User user) {
        final var optional = passwordResetTokenRepository.findByOwner(user);
        if (optional.isPresent()) {
            final var token = optional.get();
            if (token.getValidated() || token.getCreatedAt().isBefore(Instant.now().minusSeconds(360))) {
                passwordResetTokenRepository.delete(token);
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    @Transactional
    public void resetPassword(
        final String uuid,
        final String username,
        final String password,
        final PasswordEncoder passwordEncoder
    ) {
        final PasswordResetToken passwordResetToken = 
            passwordResetTokenRepository
            .findByToken(uuid)
            .orElseThrow(() -> new ResponseStatusException(404, "Not found token.", null));

        if (passwordResetToken.getValidated() || passwordResetToken.getCreatedAt().isBefore(Instant.now().minusSeconds(360)))
            throw new ResponseStatusException(400, "Invalid token.", null);

        final var owner = passwordResetToken.getOwner();
        if (!owner.getUsername().equals(username))
            throw new ResponseStatusException(400, "Invalid token for user.", null);

        owner.setPassword(passwordEncoder.encode(password));
    }
}
