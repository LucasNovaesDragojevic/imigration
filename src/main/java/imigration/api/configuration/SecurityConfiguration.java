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

import imigration.api.constant.Url;
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
        return httpSecurity
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(request -> request
            .requestMatchers(HttpMethod.POST, Url.SIGNIN).permitAll()
            .requestMatchers(HttpMethod.POST, Url.SIGNUP).permitAll()
            .requestMatchers(HttpMethod.GET, Url.EMAIL_VERIFICATIONS).permitAll()
            .requestMatchers(HttpMethod.POST, Url.PASSWORDS_RECOVERY).permitAll()
            .requestMatchers(HttpMethod.POST, Url.PASSWORDS_RESET).permitAll()
            .requestMatchers(HttpMethod.GET, Url.USERS).hasAuthority(AuthorityName.USER_READ.name())
            .requestMatchers(HttpMethod.GET, Url.USER).hasAuthority(AuthorityName.USER_READ.name())
            .requestMatchers(HttpMethod.PATCH, Url.USER).hasAuthority(AuthorityName.USER_UPDATE.name())
            .requestMatchers(HttpMethod.GET, Url.PROCESSES).hasAuthority(AuthorityName.PROCESS_READ.name())
            .requestMatchers(HttpMethod.GET, Url.PROCESS).hasAuthority(AuthorityName.PROCESS_READ.name())
            .requestMatchers(HttpMethod.POST, Url.PROCESSES).hasAuthority(AuthorityName.PROCESS_CREATE.name())
            .requestMatchers(HttpMethod.PATCH, Url.PROCESS).hasAuthority(AuthorityName.PROCESS_UPDATE.name())
            .requestMatchers(HttpMethod.GET, Url.COMMENTS_BY_PROCESS).hasAuthority(AuthorityName.COMMENT_READ.name())
            .requestMatchers(HttpMethod.POST, Url.COMMENTS_BY_PROCESS).hasAuthority(AuthorityName.COMMENT_CREATE.name())
            .requestMatchers(HttpMethod.GET, Url.ATTACHMENTS_BY_COMMENT).hasAuthority(AuthorityName.COMMENT_READ.name())
            .anyRequest().authenticated()
        )
        .build();
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
