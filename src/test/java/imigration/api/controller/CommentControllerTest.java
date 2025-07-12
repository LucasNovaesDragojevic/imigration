package imigration.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

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

import imigration.api.builder.ProcessBuilder;
import imigration.api.builder.UserBuilder;
import imigration.api.constant.Url;
import imigration.api.factory.AuthorityFactory;
import imigration.api.helper.DatabaseHelper;
import imigration.api.helper.SinginHelper;
import imigration.api.model.entity.Authority;
import imigration.api.model.enums.AuthorityName;
import imigration.api.model.enums.Error;
import imigration.api.model.request.CommentRequest;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class CommentControllerTest {
    
    private static final String PASSWORD = "Aa!12345";
    private final UserBuilder userBuilder;
    private final ProcessBuilder processBuilder;
    private final DatabaseHelper databaseHelper;
    private final SinginHelper signinHelper;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityFactory authorityFactory;
    private final MockMvc mockMvc;
    private final ObjectMapper om;
    private Set<Authority> authorities;

    @Autowired
    CommentControllerTest(final UserBuilder userBuilder,
                            final ProcessBuilder processBuilder,
                            final DatabaseHelper databaseHelper,
                            final SinginHelper signinHelper,
                            final PasswordEncoder passwordEncoder,
                            final AuthorityFactory authorityFactory, 
                            final MockMvc mockMvc,
                            final ObjectMapper om) {                                
        this.userBuilder = userBuilder;
        this.processBuilder = processBuilder;
        this.databaseHelper = databaseHelper;
        this.signinHelper = signinHelper;
        this.passwordEncoder = passwordEncoder;
        this.authorityFactory = authorityFactory;
        this.mockMvc = mockMvc;
        this.om = om;
    }

    @BeforeEach
    void beforeEach() {
        this.authorities = databaseHelper.saveAll(authorityFactory.fabricAll());
    }

    @AfterEach
    void afterEach() {
        databaseHelper.clearDatabase();
    }

    @Test
    void testCreateComment() throws Exception {
        final var owner = databaseHelper.save(userBuilder.buildRootUser(passwordEncoder.encode(PASSWORD), authorities));
        final var process = databaseHelper.save(processBuilder.buildRandomProcess(owner));
        final var jwt = signinHelper.signin(owner.getUsername(), PASSWORD);
        final var randomComment = UUID.randomUUID().toString();
        final var commentRequest = new CommentRequest(randomComment, null);
        mockMvc.perform(post(Url.COMMENTS_BY_PROCESS, process.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(commentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.owner").value(owner.getId()))
                .andExpect(jsonPath("$.content").value(randomComment))
                .andExpect(jsonPath("$.attachments").isEmpty());
    }

    @Test
    void testCreateCommentWithAttachment() throws Exception {
        final var owner = databaseHelper.save(userBuilder.buildRootUser(passwordEncoder.encode(PASSWORD), authorities));
        final var process = databaseHelper.save(processBuilder.buildRandomProcess(owner));
        final var jwt = signinHelper.signin(owner.getUsername(), PASSWORD);
        final var randomComment = UUID.randomUUID().toString();
        final var commentRequest = new CommentRequest(randomComment, List.of(UUID.randomUUID().toString()));
        mockMvc.perform(post(Url.COMMENTS_BY_PROCESS, process.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(commentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.owner").value(owner.getId()))
                .andExpect(jsonPath("$.content").value(randomComment))
                .andExpect(jsonPath("$.attachments").isNotEmpty());
    }

    @Test
    void testCreateCommentInProcessOfOtherOwner() throws Exception {
        final var owner = databaseHelper.save(userBuilder.buildRootUser(passwordEncoder.encode(PASSWORD), authorities));
        final var process = databaseHelper.save(processBuilder.buildRandomProcess(owner));
        signinHelper.signin(owner.getUsername(), PASSWORD);
        final var randomComment = UUID.randomUUID().toString();
        final var commentRequest = new CommentRequest(randomComment, null);
        final var randomUserAutorities = 
            authorities.stream()
                .filter(a -> AuthorityName.PROCESS_REVIEWER.name().equals(a.getAuthority())
                        || AuthorityName.COMMENT_CREATE.name().equals(a.getAuthority()))
                .collect(Collectors.toSet());
        final var randomUser = databaseHelper.save(userBuilder.buildRandomUser(passwordEncoder.encode(PASSWORD), randomUserAutorities));
        final var randomUserJwt = signinHelper.signin(randomUser.getUsername(), PASSWORD);
        
        mockMvc.perform(post(Url.COMMENTS_BY_PROCESS, process.getId())
                .param("owner", String.valueOf(owner.getId()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + randomUserJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(commentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.owner").value(randomUser.getId()))
                .andExpect(jsonPath("$.content").value(randomComment))
                .andExpect(jsonPath("$.attachments").isEmpty());
    }

    @Test
    void testCreateCommentWithoutRequiredAutority() throws Exception {
        final var owner = databaseHelper.save(userBuilder.buildRootUser(passwordEncoder.encode(PASSWORD), authorities));
        final var process = databaseHelper.save(processBuilder.buildRandomProcess(owner));
        final var randomUser = databaseHelper.save(userBuilder.buildRandomUser(passwordEncoder.encode(PASSWORD), Collections.emptySet()));
        final var randomUserJwt = signinHelper.signin(randomUser.getUsername(), PASSWORD);
        
        mockMvc.perform(post(Url.COMMENTS_BY_PROCESS, process.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + randomUserJwt))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateCommentWithInvalidProcessId() throws Exception {
        final var owner = databaseHelper.save(userBuilder.buildRootUser(passwordEncoder.encode(PASSWORD), authorities));
        final var process = databaseHelper.save(processBuilder.buildRandomProcess(owner));
        final var jwt = signinHelper.signin(owner.getUsername(), PASSWORD);
        
        mockMvc.perform(post(Url.COMMENTS_BY_PROCESS, process.getId() + 1)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CommentRequest(UUID.randomUUID().toString(), null))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(Error.E1015.name()));
    }

    @Test
    void testCreateCommentInProcessOfOtherOwnerNotBeingReviewer() throws Exception {
        final var owner = databaseHelper.save(userBuilder.buildRootUser(passwordEncoder.encode(PASSWORD), authorities));
        final var process = databaseHelper.save(processBuilder.buildRandomProcess(owner));
        final var randomUserAutorities = 
            authorities.stream()
                .filter(a -> AuthorityName.COMMENT_CREATE.name().equals(a.getAuthority()))
                .collect(Collectors.toSet());
        final var randomUser = databaseHelper.save(userBuilder.buildRandomUser(passwordEncoder.encode(PASSWORD), randomUserAutorities));
        final var randomUserJwt = signinHelper.signin(randomUser.getUsername(), PASSWORD);
        
        mockMvc.perform(post(Url.COMMENTS_BY_PROCESS, process.getId())
                .param("owner", String.valueOf(owner.getId()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + randomUserJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CommentRequest(UUID.randomUUID().toString(), null))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(Error.E1015.name()));
    }

    @Test
    void testFindAllByProcessIdAndOwnerId() throws Exception {
        final var owner = databaseHelper.save(userBuilder.buildRootUser(passwordEncoder.encode(PASSWORD), authorities));
        final var process = databaseHelper.save(processBuilder.buildRandomProcess(owner));
        final var jwt = signinHelper.signin(owner.getUsername(), PASSWORD);

        final var contentAsString = 
            mockMvc.perform(post(Url.COMMENTS_BY_PROCESS, process.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CommentRequest(UUID.randomUUID().toString(), null))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final var commentId = om.readTree(contentAsString).get("id").asInt();
        
        mockMvc.perform(get(Url.COMMENTS_BY_PROCESS, process.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isNotEmpty())
            .andExpect(jsonPath("$.content[0].id").exists())
            .andExpect(jsonPath("$.content[0].id").value(commentId));
    }

    @Test
    void testFindAllByProcessIdAndOwnerIdOfOtherOwner() throws Exception {
        final var owner = databaseHelper.save(userBuilder.buildRootUser(passwordEncoder.encode(PASSWORD), authorities));
        final var process = databaseHelper.save(processBuilder.buildRandomProcess(owner));
        final var jwt = signinHelper.signin(owner.getUsername(), PASSWORD);
        final var randomUserAutorities = 
            authorities.stream()
                .filter(a -> AuthorityName.PROCESS_REVIEWER.name().equals(a.getAuthority())
                        || AuthorityName.COMMENT_READ.name().equals(a.getAuthority()))
                .collect(Collectors.toSet());
        final var randomUser = databaseHelper.save(userBuilder.buildRandomUser(passwordEncoder.encode(PASSWORD), randomUserAutorities));
        final var randomUserJwt = signinHelper.signin(randomUser.getUsername(), PASSWORD);

        final var contentAsString = 
            mockMvc.perform(post(Url.COMMENTS_BY_PROCESS, process.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(new CommentRequest(UUID.randomUUID().toString(), null))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final var commentId = om.readTree(contentAsString).get("id").asInt();
        
        mockMvc.perform(get(Url.COMMENTS_BY_PROCESS, process.getId())
            .param("owner", String.valueOf(owner.getId()))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + randomUserJwt))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isNotEmpty())
            .andExpect(jsonPath("$.content[0].id").exists())
            .andExpect(jsonPath("$.content[0].id").value(commentId));
    }

    @Test
    void testFindAllByProcessIdAndOwnerIdOfOtherOwnerWithoutRequiredAutority() throws Exception {
        final var randomUser = databaseHelper.save(userBuilder.buildRandomUser(passwordEncoder.encode(PASSWORD), Collections.emptySet()));
        final var randomUserJwt = signinHelper.signin(randomUser.getUsername(), PASSWORD);
        
        mockMvc.perform(get(Url.COMMENTS_BY_PROCESS, ThreadLocalRandom.current().nextInt(1, 1000))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + randomUserJwt))
            .andExpect(status().isForbidden());
    }

    @Test
    void testFindAllByProcessIdAndOwnerIdOfOtherOwnerNotBeingReviewer() throws Exception {
        final var owner = databaseHelper.save(userBuilder.buildRootUser(passwordEncoder.encode(PASSWORD), authorities));
        final var process = databaseHelper.save(processBuilder.buildRandomProcess(owner));
        final var jwt = signinHelper.signin(owner.getUsername(), PASSWORD);
        final var randomUserAutorities = 
            authorities.stream()
                .filter(a -> AuthorityName.COMMENT_READ.name().equals(a.getAuthority()))
                .collect(Collectors.toSet());
        final var randomUser = databaseHelper.save(userBuilder.buildRandomUser(passwordEncoder.encode(PASSWORD), randomUserAutorities));
        final var randomUserJwt = signinHelper.signin(randomUser.getUsername(), PASSWORD);

        mockMvc.perform(post(Url.COMMENTS_BY_PROCESS, process.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(new CommentRequest(UUID.randomUUID().toString(), null))))
            .andExpect(status().isCreated());
        
        mockMvc.perform(get(Url.COMMENTS_BY_PROCESS, process.getId())
            .param("owner", String.valueOf(owner.getId()))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + randomUserJwt))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty());
    }
}
