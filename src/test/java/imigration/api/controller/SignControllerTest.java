package imigration.api.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

import imigration.api.constant.Url;
import imigration.api.model.request.SignRequest;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class SignControllerTest {

    private final MockMvc mockMvc;

    @Autowired
    SignControllerTest(
        final MockMvc mockMvc
    ) {
        this.mockMvc = mockMvc;
    }

    @Test
    void testSignup() throws Exception {
        SignRequest signRequest = new SignRequest("root@email.com", "Aa!12345");
        String writeValueAsString = new ObjectMapper().writeValueAsString(signRequest);
        MockHttpServletRequestBuilder postRequest = MockMvcRequestBuilders.post(Url.SIGNUP).contentType(MediaType.APPLICATION_JSON).content(writeValueAsString);
        mockMvc.perform(postRequest).andExpect(MockMvcResultMatchers.status().isCreated());
    }
}
