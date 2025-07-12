package imigration.api.helper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import imigration.api.constant.Url;
import imigration.api.model.request.SignRequest;

@Component
public class SinginHelper {

    private final MockMvc mockMvc;
    private final ObjectMapper om;

    public SinginHelper(final MockMvc mockMvc, final ObjectMapper om) {
        this.mockMvc = mockMvc;
        this.om = om;
    }

    public String signin(final String username, final String password) throws Exception {
        final var responseBody = mockMvc.perform(post(Url.SIGNIN)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(om.writeValueAsString(new SignRequest(username, password))))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.token").exists())
                            .andReturn()
                            .getResponse()
                            .getContentAsString();        
        return om.readTree(responseBody).get("token").asText();
    }
}
