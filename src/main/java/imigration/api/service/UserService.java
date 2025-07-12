package imigration.api.service;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import imigration.api.exception.TokenExpiredException;
import imigration.api.exception.TokenNotFoundException;
import imigration.api.exception.TokenValidatedException;
import imigration.api.exception.UserAlreadyExistsException;
import imigration.api.model.entity.Authority;
import imigration.api.model.entity.PasswordResetToken;
import imigration.api.model.entity.User;
import imigration.api.model.entity.VerificationToken;
import imigration.api.model.update.UserUpdate;
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
        if (userRepository.existsByUsername(user.getUsername()))
            throw new UserAlreadyExistsException();
        return userRepository.save(user);
    }

    public Page<User> findAll(final Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> findByUsername(final String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(final Integer id) {
        return userRepository.findById(id);
    }

    public String generateEmailValidationToken(final User user) {
        return verificationTokenRepository.save(new VerificationToken(user)).getToken();
    }

    @Transactional
    public void verifyEmailValidationToken(final String uuid) {
        final var token = verificationTokenRepository.findByToken(uuid).orElseThrow(TokenNotFoundException::new);
        if (token.getValidated())
            throw new TokenValidatedException();
        if (token.getCreatedAt().isBefore(Instant.now().minusSeconds(360)))
            throw new TokenExpiredException();
        token.getOwner().setEnabled(Boolean.TRUE);
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
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Transactional
    public void resetPassword(
        final String uuid,
        final String password,
        final PasswordEncoder passwordEncoder
    ) {
        final var passwordResetToken = passwordResetTokenRepository.findByToken(uuid).orElseThrow(TokenNotFoundException::new);

        if (passwordResetToken.getValidated())
            throw new TokenValidatedException();
        
        if (passwordResetToken.getCreatedAt().isBefore(Instant.now().minusSeconds(360)))
            throw new TokenExpiredException();

        final var owner = passwordResetToken.getOwner();

        owner.setPassword(passwordEncoder.encode(password));
        passwordResetToken.setValidated(Boolean.TRUE);
    }

    @Transactional
    public User update(final UserUpdate userUpdate, final Set<Authority> authorities, final User user) {
        Optional.ofNullable(userUpdate.isEnabled()).ifPresent(user::setEnabled);
        user.clearAuthorities();
        user.addAllAuthorities(authorities);
        return user;
    }
}
