package imigration.api.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import imigration.api.builder.UserBuilder;
import imigration.api.constant.Url;
import imigration.api.model.entity.Authority;
import imigration.api.model.entity.User;
import imigration.api.model.enums.AuthorityName;
import imigration.api.model.request.SignRequest;
import imigration.api.model.update.UserUpdate;
import imigration.api.repository.AuthorityRepository;
import imigration.api.repository.PasswordResetTokenRepository;
import imigration.api.repository.UserRepository;
import imigration.api.repository.VerificationTokenRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private String jwt;
    private static final String TOKEN = "token";
    private static final String USERNAME = "root@email.com";
    private static final String PASSWORD = "Aa!12345";
    private final MockMvc mockMvc;
    private final ObjectMapper om;
    private final UserBuilder ub;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final AuthorityRepository authorityRepository;

    @Autowired
    UserControllerTest(
            final MockMvc mockMvc,
            final ObjectMapper om,
            final UserBuilder ub,
            final UserRepository userRepository,
            final PasswordEncoder passwordEncoder,
            final AuthorityRepository authorityRepository,
            final VerificationTokenRepository verificationTokenRepository,
            final PasswordResetTokenRepository passwordResetTokenRepository
    ) {
        this.mockMvc = mockMvc;
        this.om = om;
        this.ub = ub;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @BeforeEach
    void beforeEach() throws Exception {
        final var rootUser = this.createRootUser();
        this.jwt = this.signin(rootUser);
    }

    @AfterEach
    void afterEach() {
        passwordResetTokenRepository.deleteAll();
        verificationTokenRepository.deleteAll();
        userRepository.deleteAll();
        authorityRepository.deleteAll();
    }

    @Test
    void testGetAllUsers() throws Exception {
        this.createRandomUser();
        mockMvc.perform(get(Url.USERS)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
    }

    @Test
    void testGetAllUsersWithRandomUserReturnsForbidden() throws Exception {
        final var randomUser = this.createRandomUser();
        final var jwtRandomUser = this.signin(randomUser);
        mockMvc.perform(get(Url.USERS)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtRandomUser))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetUser() throws Exception {
        final var randomUser = this.createRandomUser();
        mockMvc.perform(get(Url.USER, randomUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(randomUser.getId()))
                .andExpect(jsonPath("$.username").value(randomUser.getUsername()));
    }

    @Test
    void testGetUserWithInvalidIdReturnsNotFound() throws Exception {
        mockMvc.perform(get(Url.USER, (int) System.currentTimeMillis())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetUserWithRandomUserReturnsForbidden() throws Exception {
        final var randomUser = this.createRandomUser();
        final var jwtRandomUser = this.signin(randomUser);
        mockMvc.perform(get(Url.USER, randomUser.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtRandomUser))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void testUpdateUser() throws Exception {
        final var randomUser = this.createRandomUser();
        mockMvc.perform(patch(Url.USER, randomUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new UserUpdate(false, Set.of(AuthorityName.USER_READ.name())))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(randomUser.getId()))
                .andExpect(jsonPath("$.isEnabled").value(false))
                .andExpect(jsonPath("$.authorities", hasSize(1)))
                .andExpect(jsonPath("$.authorities[0]").value(AuthorityName.USER_READ.name()));
    }

    @Test
    void testUpdateWithoutEnabledUser() throws Exception {
        final var randomUser = this.createRandomUser();
        mockMvc.perform(patch(Url.USER, randomUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new UserUpdate(null, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(randomUser.getId()))
                .andExpect(jsonPath("$.isEnabled").value(randomUser.isEnabled()));
    }

    @Test
    void testUpdateUserWithoutAuthorities() throws Exception {
        final var randomUser = this.createRandomUser();
        mockMvc.perform(patch(Url.USER, randomUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new UserUpdate(false, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(randomUser.getId()))
                .andExpect(jsonPath("$.isEnabled").value(false))
                .andExpect(jsonPath("$.authorities", hasSize(0)));
    }

    @Test
    void testUpdateUserWithInvalidIdReturnsNotFound() throws Exception {
        mockMvc.perform(patch(Url.USER, (int) System.currentTimeMillis())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new UserUpdate(null, null))))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateUserWithRandomUserReturnsForbidden() throws Exception {
        final var randomUser = this.createRandomUser();
        final var jwtRandomUser = this.signin(randomUser);
        mockMvc.perform(patch(Url.USER, randomUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtRandomUser))
                        .andExpect(status().isForbidden());
    }

    private User createRootUser() {
        final var authorityUserRead = authorityRepository.save(new Authority(AuthorityName.USER_READ));
        final var authorityUserUpdate = authorityRepository.save(new Authority(AuthorityName.USER_UPDATE));
        final var user = ub.user()
                           .withUsername(USERNAME)
                           .withPassword(passwordEncoder.encode(PASSWORD))
                           .withEnabled(true)
                           .withAuthorities(Set.of(authorityUserRead, authorityUserUpdate))
                           .build();
        return userRepository.save(user);
    }

    private User createRandomUser() {
        final var user = ub.user()
                           .withUsername("user" + System.currentTimeMillis() + "@email.com")
                           .withPassword(passwordEncoder.encode(PASSWORD))
                           .withEnabled(true)
                           .withAuthorities(new HashSet<>())
                           .build();
        return userRepository.save(user);
    }

    private String signin(final User user) throws Exception {
        var result = mockMvc.perform(post(Url.SIGNIN)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(om.writeValueAsString(new SignRequest(user.getUsername(), PASSWORD))))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$." + TOKEN).exists())
                            .andReturn();

        final var responseBody = result.getResponse().getContentAsString();
        return om.readTree(responseBody).get(TOKEN).asText();
    }
}
