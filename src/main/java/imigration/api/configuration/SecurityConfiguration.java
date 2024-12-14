package imigration.api.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import imigration.api.filter.AuthFilter;
import imigration.api.model.enums.AuthorityName;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    
    private final AuthFilter authFilter;

    public SecurityConfiguration(final AuthFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity httpSecurity) throws Exception {
        httpSecurity
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(request -> request
            .requestMatchers(HttpMethod.POST, "/signup").permitAll()
            .requestMatchers(HttpMethod.POST, "/signin").permitAll()
            .requestMatchers(HttpMethod.GET, "/email-verifications/{id}").permitAll()
            .requestMatchers(HttpMethod.POST, "/passwords/recovery").permitAll()
            .requestMatchers(HttpMethod.POST, "/passwords/reset/{id}").permitAll()

            .requestMatchers(HttpMethod.GET, "/processes").hasAuthority(AuthorityName.PROCESS_READ.name())
            .requestMatchers(HttpMethod.POST, "/processes").hasAuthority(AuthorityName.PROCESS_CREATE.name())
            .requestMatchers(HttpMethod.PATCH, "/processes/{id}").hasAuthority(AuthorityName.PROCESS_UPDATE.name())

            .requestMatchers(HttpMethod.GET, "/processes/{id}/comments").hasAuthority(AuthorityName.COMMENT_READ.name())
            .requestMatchers(HttpMethod.POST, "/processes/{id}/comments").hasAuthority(AuthorityName.COMMENT_CREATE.name())
            
            .requestMatchers(HttpMethod.GET, "/comments/{id}/attachments").hasAuthority(AuthorityName.COMMENT_READ.name())
            .anyRequest().authenticated()
        )
        .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
