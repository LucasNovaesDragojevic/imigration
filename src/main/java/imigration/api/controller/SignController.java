package imigration.api.controller;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import imigration.api.constant.Url;
import imigration.api.model.entity.Authority;
import imigration.api.model.entity.User;
import imigration.api.model.request.EmailRequest;
import imigration.api.model.request.SignRequest;
import imigration.api.model.response.JwtResponse;
import imigration.api.service.AuthorityService;
import imigration.api.service.EmailService;
import imigration.api.service.JwtService;
import imigration.api.service.UserService;
import jakarta.validation.Valid;

@RestController
public class SignController {

    private final UserService userService;
    private final AuthorityService authorityService;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final Set<Authority> defaultSignupAuthorities;
    
    public SignController(final UserService userService,
                            final AuthorityService authorityService,
                            final EmailService emailService,
                            final JwtService jwtService,
                            final PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.authorityService = authorityService;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.defaultSignupAuthorities = this.authorityService.getDefaultSignupAuthorities();
    }

    @PostMapping(Url.SIGNUP)
    public ResponseEntity<?> signup(@RequestBody @Valid final SignRequest signRequest) {
        final var user = userService.save(new User(signRequest.username(), passwordEncoder.encode(signRequest.password()), defaultSignupAuthorities));
        final var token = userService.generateEmailValidationToken(user);
        emailService.sendVerification(user.getUsername(), token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(Url.SIGNIN)
    public JwtResponse signin(@RequestBody @Valid final SignRequest signRequest) {
        final var user = userService.findByUsername(signRequest.username()).get();
        if (passwordEncoder.matches(signRequest.password(), user.getPassword()))
            return new JwtResponse(jwtService.generate(user.getUsername(), user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()));
        throw new RuntimeException("Invalid username or password.");
    }

    @GetMapping(Url.EMAIL_VERIFICATIONS)
    ResponseEntity<?> emailVerification(@PathVariable("id") final String uuid) {
        userService.verifyEmailValidationToken(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(Url.PASSWORDS_RECOVERY)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void passwordsRecovery(@RequestBody @Valid final EmailRequest emailRequest) {
        userService.findByUsername(emailRequest.email())
            .ifPresent(u -> {
                if (userService.hasPasswordResetToken(u)) 
                    return;
                final var token = userService.generatePasswordRecoveryToken(u);
                emailService.sendRecovery(u.getUsername(), token);
            });
    }

    @PostMapping(Url.PASSWORDS_RESET)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void passwordsRecovery(
        @PathVariable("id") final String uuid, 
        @RequestBody @Valid final SignRequest signRequest
    ) {
        userService.resetPassword(uuid, signRequest.username(), signRequest.password(), passwordEncoder);
    }
}
