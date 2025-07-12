package imigration.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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
import imigration.api.factory.AuthorityFactory;
import imigration.api.helper.DatabaseHelper;
import imigration.api.model.entity.Authority;
import imigration.api.model.entity.User;
import imigration.api.model.enums.Country;
import imigration.api.model.enums.Step;
import imigration.api.model.request.CommentRequest;
import imigration.api.model.request.ProcessRequest;
import imigration.api.model.request.SignRequest;
import imigration.api.model.update.ProcessUpdate;
import imigration.api.repository.AttachmentRepository;
import imigration.api.repository.AuthorityRepository;
import imigration.api.repository.CommentRepository;
import imigration.api.repository.PasswordResetTokenRepository;
import imigration.api.repository.ProcessRepository;
import imigration.api.repository.UserRepository;
import imigration.api.repository.VerificationTokenRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class ProcessControllerTest {

    private String jwt;
    private static final String TOKEN = "token";
    private static final String PASSWORD = "Aa!12345";
    private final MockMvc mockMvc;
    private final ObjectMapper om;
    private final UserBuilder ub;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final DatabaseHelper databaseHelper;
    private final AuthorityFactory authorityFactory;
    private Set<Authority> authorities;

    @Autowired
    ProcessControllerTest(
            final MockMvc mockMvc,
            final ObjectMapper om,
            final UserBuilder ub,
            final UserRepository userRepository,
            final PasswordEncoder passwordEncoder,
            final AuthorityRepository authorityRepository,
            final VerificationTokenRepository verificationTokenRepository,
            final PasswordResetTokenRepository passwordResetTokenRepository,
            final AttachmentRepository attachmentRepository,
            final CommentRepository commentRepository,
            final ProcessRepository processRepository,
            final DatabaseHelper databaseHelper,
            final AuthorityFactory authorityFabric
    ) {
        this.mockMvc = mockMvc;
        this.om = om;
        this.ub = ub;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authorityFactory = authorityFabric;
        this.databaseHelper = databaseHelper;
    }

    @BeforeEach
    void beforeEach() throws Exception {
        this.authorities = databaseHelper.saveAll(authorityFactory.fabricAll());
        final var rootUser = this.createAndSaveRootUser();
        this.jwt = this.signin(rootUser);
    }

    @AfterEach
    void afterEach() {
        databaseHelper.clearDatabase();
    }

    @Test
    void testCreateProcess() throws Exception {
        final var randomCountry = Country.values()[ThreadLocalRandom.current().nextInt(Country.values().length)];
        
        final var minDay = LocalDate.of(1950, 1, 1).toEpochDay();
        final var maxDay = LocalDate.of(2020, 12, 31).toEpochDay();
        final var randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
        final var randomDate = LocalDate.ofEpochDay(randomDay);
        
        final var randomPassport = UUID.randomUUID().toString();

        final var randomCommentRequest = new CommentRequest("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", null);

        final var randomProcessRequest = new ProcessRequest(randomCountry, randomDate, randomPassport, null, null, randomCommentRequest);

        mockMvc.perform(post(Url.PROCESSES)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(randomProcessRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.step").value(Step.SEND_DOCUMENTS.name()))
            .andExpect(jsonPath("$.nationality").value(randomCountry.name()))
            .andExpect(jsonPath("$.dateBirth").value(randomDate.toString()))
            .andExpect(jsonPath("$.passport").value(randomPassport))
            .andExpect(jsonPath("$.comments").isArray())
            .andExpect(jsonPath("$.comments.length()").value(1));
    }

    @Test
    void testFindAll() throws Exception {
        final var randomCountry = Country.values()[ThreadLocalRandom.current().nextInt(Country.values().length)];
        
        final var minDay = LocalDate.of(1950, 1, 1).toEpochDay();
        final var maxDay = LocalDate.of(2020, 12, 31).toEpochDay();
        final var randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
        final var randomDate = LocalDate.ofEpochDay(randomDay);
        
        final var randomPassport = UUID.randomUUID().toString();

        final var randomCommentRequest = new CommentRequest("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", null);

        final var randomProcessRequest = new ProcessRequest(randomCountry, randomDate, randomPassport, null, null, randomCommentRequest);

        final var result = mockMvc.perform(post(Url.PROCESSES)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(randomProcessRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andReturn();
        final var responseBody = result.getResponse().getContentAsString();
        final var processId = om.readTree(responseBody).get("id").asInt();
        mockMvc.perform(get(Url.PROCESSES)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].id").value(processId));
    }

    @Test
    void testFindAllByOwner() throws Exception {
        final var randomUser = createRandomUser();
        final var jwtRandomUser = signin(randomUser);

        final var randomCountry = Country.values()[ThreadLocalRandom.current().nextInt(Country.values().length)];
        
        final var minDay = LocalDate.of(1950, 1, 1).toEpochDay();
        final var maxDay = LocalDate.of(2020, 12, 31).toEpochDay();
        final var randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
        final var randomDate = LocalDate.ofEpochDay(randomDay);
        
        final var randomPassport = UUID.randomUUID().toString();

        final var randomCommentRequest = new CommentRequest("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", null);

        final var randomProcessRequest = new ProcessRequest(randomCountry, randomDate, randomPassport, null, null, randomCommentRequest);

        final var result = mockMvc.perform(post(Url.PROCESSES)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtRandomUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(randomProcessRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andReturn();
        final var responseBody = result.getResponse().getContentAsString();
        final var processId = om.readTree(responseBody).get("id").asInt();
        mockMvc.perform(get(Url.PROCESSES)
            .param("owner", String.valueOf(randomUser.getId()))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].id").value(processId));
    }

    @Test
    void testFindByIdAndOwner() throws Exception {
        final var randomUser = createRandomUser();
        final var jwtRandomUser = signin(randomUser);

        final var randomCountry = Country.values()[ThreadLocalRandom.current().nextInt(Country.values().length)];
        
        final var minDay = LocalDate.of(1950, 1, 1).toEpochDay();
        final var maxDay = LocalDate.of(2020, 12, 31).toEpochDay();
        final var randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
        final var randomDate = LocalDate.ofEpochDay(randomDay);
        
        final var randomPassport = UUID.randomUUID().toString();

        final var randomCommentRequest = new CommentRequest("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", null);

        final var randomProcessRequest = new ProcessRequest(randomCountry, randomDate, randomPassport, null, null, randomCommentRequest);

        final var result = mockMvc.perform(post(Url.PROCESSES)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtRandomUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(randomProcessRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andReturn();
        final var responseBody = result.getResponse().getContentAsString();
        final var processId = om.readTree(responseBody).get("id").asInt();
        mockMvc.perform(get(Url.PROCESS, processId)
            .param("owner", String.valueOf(randomUser.getId()))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.id").value(processId));
    }

    @Test
    void testUpdateProcess() throws Exception {
        final var randomUser = createRandomUser();
        final var jwtRandomUser = signin(randomUser);

        final var randomCountry = Country.values()[ThreadLocalRandom.current().nextInt(Country.values().length)];
        
        final var minDay = LocalDate.of(1950, 1, 1).toEpochDay();
        final var maxDay = LocalDate.of(2020, 12, 31).toEpochDay();
        final var randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
        final var randomDate = LocalDate.ofEpochDay(randomDay);
        
        final var randomPassport = UUID.randomUUID().toString();

        final var randomCommentRequest = new CommentRequest("Lorem ipsum dolor sit amet, consectetur adipiscing elit.", null);

        final var randomProcessRequest = new ProcessRequest(randomCountry, randomDate, randomPassport, null, null, randomCommentRequest);

        final var result = 
        mockMvc.perform(post(Url.PROCESSES)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtRandomUser)
            .contentType(MediaType.APPLICATION_JSON)
            .content(om.writeValueAsString(randomProcessRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andReturn();
        
        final var responseBody = result.getResponse().getContentAsString();
        final var processId = om.readTree(responseBody).get("id").asInt();

        mockMvc.perform(patch(Url.PROCESS, processId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(om.writeValueAsString(new ProcessUpdate(randomUser.getId(), Step.ANALYSIS))))
            .andExpect(status().isNoContent());

        mockMvc.perform(get(Url.PROCESS, processId)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtRandomUser))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.id").value(processId))
            .andExpect(jsonPath("$.step").value(Step.ANALYSIS.name()));
    }

    private User createAndSaveRootUser() {
        final var user = 
            ub.buildRootUser(
                passwordEncoder.encode(PASSWORD), 
                authorities);
        return userRepository.save(user);
    }

    private User createRandomUser() {
        final var user = ub.user()
                           .withUsername("user" + System.currentTimeMillis() + "@email.com")
                           .withPassword(passwordEncoder.encode(PASSWORD))
                           .withEnabled(true)
                           .withAuthorities(authorities)
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
