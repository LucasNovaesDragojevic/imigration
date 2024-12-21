package imigration.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import imigration.api.builder.UserBuilder;
import imigration.api.constant.Url;
import imigration.api.exception.TokenNotFoundException;
import imigration.api.exception.UserNotFoundException;
import imigration.api.model.entity.PasswordResetToken;
import imigration.api.model.entity.User;
import imigration.api.model.entity.VerificationToken;
import imigration.api.model.enums.Error;
import imigration.api.model.request.EmailRequest;
import imigration.api.model.request.PasswordRequest;
import imigration.api.model.request.SignRequest;
import imigration.api.repository.PasswordResetTokenRepository;
import imigration.api.repository.UserRepository;
import imigration.api.repository.VerificationTokenRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class SignControllerTest {

    private static final String TOKEN = "token";
    private static final String USERNAME = "root@email.com";
    private static final String PASSWORD = "Aa!12345";
    private static final String CODE_PATH = "$.code";
    private static final String INVALID_USERNAME = "root";
    private static final String INVALID_PASSWORD = "1234";
    private static final String INVALID_TOKEN = "abc";
    private final MockMvc mockMvc;
    private final ObjectMapper om;
    private final UserBuilder ub;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    
    @Autowired
    SignControllerTest(
            final MockMvc mockMvc,
            final ObjectMapper om,
            final UserBuilder ub,
            final UserRepository userRepository,
            final PasswordEncoder passwordEncoder,
            final PasswordResetTokenRepository passwordResetTokenRepository,
            final VerificationTokenRepository verificationTokenRepository
    ) {
        this.mockMvc = mockMvc;
        this.om = om;
        this.ub = ub;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.verificationTokenRepository = verificationTokenRepository;
    }

    @AfterEach
    void afterEach() {
        passwordResetTokenRepository.deleteAll();
        verificationTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testSignup() throws Exception {
        performDefaultSignup();
    }

    @Test
    void testSignupInvalidUsername() throws Exception {
        mockMvc.perform(post(Url.SIGNUP).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new SignRequest(INVALID_USERNAME, PASSWORD))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath(CODE_PATH).value(Error.E1000.name()));
    }

    @Test
    void testSignupInvalidPassword() throws Exception {
        mockMvc.perform(post(Url.SIGNUP).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new SignRequest(USERNAME, INVALID_PASSWORD))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath(CODE_PATH).value(Error.E1000.name()));
    }

    @Test
    void testSignupSameUser() throws Exception {
        performDefaultSignup();
        mockMvc.perform(post(Url.SIGNUP)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new SignRequest(USERNAME, PASSWORD))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath(CODE_PATH).value(Error.E1001.name()));
    }

    @Test
    void testEmailVerification() throws Exception {
        performDefaultSignup();
        final var user = userRepository.findByUsername(USERNAME).orElseThrow(UserNotFoundException::new);
        final var exampleMatcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnorePaths(TOKEN);
        final var example = Example.of(new VerificationToken(user), exampleMatcher);
        final var verificationToken = verificationTokenRepository.findBy(example, query -> query.one())
                .orElseThrow(TokenNotFoundException::new);
        mockMvc.perform(get(Url.EMAIL_VERIFICATIONS, verificationToken.getToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testEmailVerificationAlreadyValidated() throws Exception {
        performDefaultSignup();
        final var user = userRepository.findByUsername(USERNAME).orElseThrow(UserNotFoundException::new);
        final var exampleMatcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnorePaths(TOKEN);
        final var example = Example.of(new VerificationToken(user), exampleMatcher);
        final var verificationToken = verificationTokenRepository.findBy(example, query -> query.one())
                .orElseThrow(TokenNotFoundException::new);
        mockMvc.perform(get(Url.EMAIL_VERIFICATIONS, verificationToken.getToken()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get(Url.EMAIL_VERIFICATIONS, verificationToken.getToken()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath(CODE_PATH).value(Error.E1008.name()));
    }

    @Test
    void testEmailVerificationWithNotExistentToken() throws Exception {
        performDefaultSignup();
        mockMvc.perform(get(Url.EMAIL_VERIFICATIONS, INVALID_TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPasswordRecovery() throws Exception {
        performDefaultSignup();
        mockMvc.perform(post(Url.PASSWORDS_RECOVERY).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new EmailRequest(USERNAME))))
                .andExpect(status().isNoContent());
    }

    @Test
    void testPasswordRecoveryWithTwoRequestsAtSameTime() throws Exception {
        performDefaultSignup();
        mockMvc.perform(post(Url.PASSWORDS_RECOVERY).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new EmailRequest(USERNAME))))
                .andExpect(status().isNoContent());
        mockMvc.perform(post(Url.PASSWORDS_RECOVERY).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new EmailRequest(USERNAME))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath(CODE_PATH).value(Error.E1010.name()));
    }

    @Test
    void testPasswordReset() throws Exception {
        performDefaultSignup();
        mockMvc.perform(post(Url.PASSWORDS_RECOVERY).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new EmailRequest(USERNAME))))
                .andExpect(status().isNoContent());
        final var user = userRepository.findByUsername(USERNAME).orElseThrow(UserNotFoundException::new);
        final var exampleMatcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnorePaths(TOKEN);
        final var example = Example.of(new PasswordResetToken(user), exampleMatcher);
        final var passwordResetToken = passwordResetTokenRepository.findBy(example, query -> query.one())
                .orElseThrow(TokenNotFoundException::new);
        mockMvc.perform(post(Url.PASSWORDS_RESET, passwordResetToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new PasswordRequest(PASSWORD))))
                .andExpect(status().isNoContent());
    }

    @Test
    void testPasswordResetAlreadyValidated() throws Exception {
        performDefaultSignup();
        mockMvc.perform(post(Url.PASSWORDS_RECOVERY).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new EmailRequest(USERNAME))))
                .andExpect(status().isNoContent());
        final var user = userRepository.findByUsername(USERNAME).orElseThrow(UserNotFoundException::new);
        final var exampleMatcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnorePaths(TOKEN);
        final var example = Example.of(new PasswordResetToken(user), exampleMatcher);
        final var passwordResetToken = passwordResetTokenRepository.findBy(example, query -> query.one())
                .orElseThrow(TokenNotFoundException::new);
        mockMvc.perform(post(Url.PASSWORDS_RESET, passwordResetToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new PasswordRequest(PASSWORD))))
                .andExpect(status().isNoContent());
        mockMvc.perform(post(Url.PASSWORDS_RESET, passwordResetToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new PasswordRequest(PASSWORD))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath(CODE_PATH).value(Error.E1008.name()));
    }

    @Test
    void testPasswordResetWithInvalidToken() throws Exception {
        mockMvc.perform(post(Url.PASSWORDS_RESET, INVALID_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new PasswordRequest(PASSWORD))))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath(CODE_PATH).value(Error.E1007.name()));
    }

    @Test
    void testPasswordResetWithInvalidPassword() throws Exception {
        performDefaultSignup();
        mockMvc.perform(post(Url.PASSWORDS_RECOVERY).contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new EmailRequest(USERNAME))))
                .andExpect(status().isNoContent());
        final var user = userRepository.findByUsername(USERNAME).orElseThrow(UserNotFoundException::new);
        final var exampleMatcher = ExampleMatcher.matching().withIgnoreNullValues().withIgnorePaths(TOKEN);
        final var example = Example.of(new PasswordResetToken(user), exampleMatcher);
        final var passwordResetToken = passwordResetTokenRepository.findBy(example, query -> query.one())
                .orElseThrow(TokenNotFoundException::new);
        mockMvc.perform(post(Url.PASSWORDS_RESET, passwordResetToken.getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new SignRequest(USERNAME, INVALID_PASSWORD))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath(CODE_PATH).value(Error.E1000.name()));
    }

    @Test
    void testSignin() throws Exception {
        createDefaultUser();
        mockMvc.perform(post(Url.SIGNIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new SignRequest(USERNAME, PASSWORD))))
                .andExpect(status().isOk());
    }

    @Test
    void testSigninWithWrongUsername() throws Exception {
        mockMvc.perform(post(Url.SIGNIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new SignRequest("example@email.com", PASSWORD))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(CODE_PATH).value(Error.E1006.name()));
    }

    @Test
    void testSigninWithInvalidUsername() throws Exception {
        mockMvc.perform(post(Url.SIGNIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new SignRequest(INVALID_USERNAME, PASSWORD))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(CODE_PATH).value(Error.E1000.name()));
    }

    @Test
    void testSigninWithWrongPassword() throws Exception {
        mockMvc.perform(post(Url.SIGNIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new SignRequest(USERNAME, "Bb!12345"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(CODE_PATH).value(Error.E1006.name()));
    }

    @Test
    void testSigninWithInvalidPassword() throws Exception {
        createDefaultUser();
        mockMvc.perform(post(Url.SIGNIN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(new SignRequest(USERNAME, INVALID_PASSWORD))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(CODE_PATH).value(Error.E1000.name()));
    }

    @Test
    void testSigninWithAccountExpiredUser() throws Exception {
        final var user = ub.user()
            .withUsername(USERNAME)
            .withPassword(passwordEncoder.encode(PASSWORD))
            .withEnabled(Boolean.TRUE)
            .withAccountNonExpired(Boolean.FALSE)
            .build();
        userRepository.save(user);
        mockMvc.perform(post(Url.SIGNIN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(new SignRequest(USERNAME, PASSWORD))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(CODE_PATH).value(Error.E1004.name()));
    }

    @Test
    void testSigninWithLockedUser() throws Exception {
        final var user = ub.user()
            .withUsername(USERNAME)
            .withPassword(passwordEncoder.encode(PASSWORD))
            .withEnabled(Boolean.TRUE)
            .withAccountNonLocked(Boolean.FALSE)
            .build();
        userRepository.save(user);
        mockMvc.perform(post(Url.SIGNIN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(new SignRequest(USERNAME, PASSWORD))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(CODE_PATH).value(Error.E1002.name()));
    }

    @Test
    void testSigninWithCredentialsExpiredUser() throws Exception {
        final var user = ub.user()
            .withUsername(USERNAME)
            .withPassword(passwordEncoder.encode(PASSWORD))
            .withEnabled(Boolean.TRUE)
            .withCredentialsNonExpired(Boolean.FALSE)
            .build();
        userRepository.save(user);
        mockMvc.perform(post(Url.SIGNIN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(new SignRequest(USERNAME, PASSWORD))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(CODE_PATH).value(Error.E1003.name()));
    }

    @Test
    void testSigninWithDisabledUser() throws Exception {
        final var user = ub.user()
            .withUsername(USERNAME)
            .withPassword(passwordEncoder.encode(PASSWORD))
            .build();
        userRepository.save(user);
        mockMvc.perform(post(Url.SIGNIN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(new SignRequest(USERNAME, PASSWORD))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(CODE_PATH).value(Error.E1005.name()));
    }

    private User createDefaultUser() {
        final var user = new User(USERNAME, passwordEncoder.encode(PASSWORD), new HashSet<>());
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private void performDefaultSignup() throws Exception {
        mockMvc.perform(post(Url.SIGNUP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new SignRequest(USERNAME, PASSWORD))))
                .andExpect(status().isCreated());
    }
}
