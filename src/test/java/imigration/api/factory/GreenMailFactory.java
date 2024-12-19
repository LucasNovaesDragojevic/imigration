package imigration.api.factory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.icegreen.greenmail.util.GreenMail;

@Configuration
class GreenMailFactory {

    @Bean
    GreenMail getGreenMail() {
        final var greenMail = new GreenMail();
        greenMail.setUser("test@email.com", "test@email.com", "1234");
        greenMail.start();
        return greenMail;
    }
}
