package imigration.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import imigration.api.builder.AttachmentBuilder;
import imigration.api.builder.CommentBuilder;
import imigration.api.builder.ProcessBuilder;
import imigration.api.builder.UserBuilder;
import imigration.api.constant.Url;
import imigration.api.factory.AuthorityFactory;
import imigration.api.helper.DatabaseHelper;
import imigration.api.helper.SinginHelper;
import imigration.api.model.entity.Authority;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class AttachmentControllerTest {

    private static final String PASSWORD = "Aa!12345";
    private final UserBuilder userBuilder;
    private final ProcessBuilder processBuilder;
    private final CommentBuilder commentBuilder;
    private final AttachmentBuilder attachmentBuilder;
    private final DatabaseHelper databaseHelper;
    private final SinginHelper signinHelper;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityFactory authorityFactory;
    private final MockMvc mockMvc;
    private final ObjectMapper om;
    private Set<Authority> authorities;

    @Autowired
    public AttachmentControllerTest(
        final UserBuilder userBuilder,
        final ProcessBuilder processBuilder,
        final CommentBuilder commentBuilder,
        final AttachmentBuilder attachmentBuilder,
        final DatabaseHelper databaseHelper,
        final SinginHelper signinHelper,
        final PasswordEncoder passwordEncoder,
        final AuthorityFactory authorityFactory,
        final MockMvc mockMvc,
        final ObjectMapper om
    ) {
        this.userBuilder = userBuilder;
        this.processBuilder = processBuilder;
        this.commentBuilder = commentBuilder;
        this.attachmentBuilder = attachmentBuilder;
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
    void testFindAllByCommentId() throws Exception {
        final var owner = databaseHelper.save(userBuilder.buildRootUser(passwordEncoder.encode(PASSWORD), authorities));
        final var process = databaseHelper.save(processBuilder.buildRandomProcess(owner));
        final var comment = databaseHelper.save(commentBuilder.buildRandomComment(owner, process));
        final var attachment = databaseHelper.save(attachmentBuilder.buildRandomAttachment(comment));
        final var jwt = signinHelper.signin(owner.getUsername(), PASSWORD);
        
        mockMvc.perform(get(Url.ATTACHMENTS_BY_COMMENT, comment.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[0].content").value(attachment.getContent()));
    }

    @Test
    void testFindAllByCommentIdWithInvalidId() throws Exception {
        final var owner = databaseHelper.save(userBuilder.buildRootUser(passwordEncoder.encode(PASSWORD), authorities));
        final var process = databaseHelper.save(processBuilder.buildRandomProcess(owner));
        final var comment = databaseHelper.save(commentBuilder.buildRandomComment(owner, process));
        databaseHelper.save(attachmentBuilder.buildRandomAttachment(comment));
        final var jwt = signinHelper.signin(owner.getUsername(), PASSWORD);
        
        mockMvc.perform(get(Url.ATTACHMENTS_BY_COMMENT, comment.getId() + 1)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    //TODO cant get attachments without authentication
    //TODO cant get attachments without role REVIEWER
    //TODO cant get attachments from other users
}
