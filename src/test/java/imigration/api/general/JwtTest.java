package imigration.api.general;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import imigration.api.constant.Url;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class JwtTest {

    private final MockMvc mockMvc;

    @Autowired
    JwtTest(final MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void testInvalidJwtReturnsForbidden() throws Exception {
        mockMvc.perform(get(Url.USERS)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer abc"))
                .andExpect(status().isForbidden());
    }
}
