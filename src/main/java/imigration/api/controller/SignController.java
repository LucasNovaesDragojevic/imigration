package imigration.api.controller;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import imigration.api.constant.Url;
import imigration.api.exception.TokenNotValidatedException;
import imigration.api.exception.UserAccountExpiredException;
import imigration.api.exception.UserCredentialsExpiredException;
import imigration.api.exception.UserDisabledException;
import imigration.api.exception.UserLockedException;
import imigration.api.exception.UserOrPasswordInvalidException;
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
    private final AuthenticationManager authenticationManager;
    
    public SignController(final UserService userService,
                            final AuthorityService authorityService,
                            final EmailService emailService,
                            final JwtService jwtService,
                            final PasswordEncoder passwordEncoder,
                            final AuthenticationManager authenticationManager
    ) {
        this.userService = userService;
        this.authorityService = authorityService;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.defaultSignupAuthorities = this.authorityService.getDefaultSignupAuthorities();
        this.authenticationManager = authenticationManager;
    }

    @PostMapping(Url.SIGNUP)
    @ResponseStatus(HttpStatus.CREATED)
    void signup(
        @RequestBody @Valid final SignRequest signRequest
    ) {
        final var user = userService.save(new User(signRequest.username(), passwordEncoder.encode(signRequest.password()), defaultSignupAuthorities));
        final var token = userService.generateEmailValidationToken(user);
        emailService.sendVerification(user.getUsername(), token);
    }

    @PostMapping(Url.SIGNIN)
    JwtResponse signin(
        @RequestBody @Valid final SignRequest signRequest
    ) {
        try {
            final var user = (User) authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signRequest.username(), signRequest.password())).getPrincipal();
            return new JwtResponse(jwtService.generate(user.getUsername(), user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()));
        } catch (LockedException e) {
            throw new UserLockedException();
        } catch (BadCredentialsException | InternalAuthenticationServiceException e) {
            throw new UserOrPasswordInvalidException();
        } catch (AccountExpiredException e) {
            throw new UserAccountExpiredException();
        } catch (CredentialsExpiredException e) {
            throw new UserCredentialsExpiredException();
        } catch (DisabledException e) {
            throw new UserDisabledException();
        }
    }

    @GetMapping(Url.EMAIL_VERIFICATIONS)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void emailVerification(
        @PathVariable(Url.ID) final String uuid
    ) {
        userService.verifyEmailValidationToken(uuid);
    }

    @PostMapping(Url.PASSWORDS_RECOVERY)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void passwordsRecovery(
        @RequestBody @Valid final EmailRequest emailRequest
    ) {
        userService
        .findByUsername(emailRequest.email())
        .ifPresent(u -> {
            if (userService.hasPasswordResetToken(u)) 
                throw new TokenNotValidatedException();
            final var token = userService.generatePasswordRecoveryToken(u);
            emailService.sendRecovery(u.getUsername(), token);
        });
    }

    @PostMapping(Url.PASSWORDS_RESET)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void passwordsRecovery(
        @PathVariable(Url.ID) final String uuid, 
        @RequestBody @Valid final SignRequest signRequest
    ) {
        userService.resetPassword(uuid, signRequest.username(), signRequest.password(), passwordEncoder);
    }
}
