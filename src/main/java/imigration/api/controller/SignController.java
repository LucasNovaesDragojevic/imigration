package imigration.api.controller;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import imigration.api.model.Authority;
import imigration.api.model.JwtResponse;
import imigration.api.model.SignRequest;
import imigration.api.model.User;
import imigration.api.service.AuthorityService;
import imigration.api.service.JwtService;
import imigration.api.service.UserService;
import jakarta.validation.Valid;

@RestController
public class SignController {

    private final UserService userService;
    private final AuthorityService authorityService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final Set<Authority> defaultSignupAuthorities;
    
    public SignController(final UserService userService,
                            final AuthorityService authorityService,
                            final JwtService jwtService,
                            final PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.authorityService = authorityService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.defaultSignupAuthorities = this.authorityService.getDefaultSignupAuthorities();
    }

    @PostMapping("signup")
    public ResponseEntity<?> signup(@RequestBody @Valid final SignRequest signRequest) {
        userService.save(new User(signRequest.username(), passwordEncoder.encode(signRequest.password()), defaultSignupAuthorities));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("signin")
    public JwtResponse signin(@RequestBody @Valid final SignRequest signRequest) {
        final var user = userService.findByUsername(signRequest.username());
        if (passwordEncoder.matches(signRequest.password(), user.getPassword())) {
            return new JwtResponse(jwtService.generate(user.getUsername(), user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()));
        }
        throw new RuntimeException("Invalid username or password.");
    }
}
