package imigration.api.filter;

import static org.springframework.http.HttpStatus.FORBIDDEN;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;

import imigration.api.model.enums.Error;
import imigration.api.service.JwtService;
import imigration.api.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public AuthFilter(final JwtService jwtService,
                        final UserService userService,
                        final ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull final HttpServletRequest request, 
                                    @NonNull final HttpServletResponse response, 
                                    @NonNull final FilterChain filterChain) throws ServletException, IOException {
        final var bearerHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (Objects.nonNull(bearerHeader)) {
            try {
                final var username = jwtService.validate(bearerHeader.replace("Bearer ", ""));
                userService.findByUsername(username)
                    .ifPresent(u -> {
                        final var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    });
            } catch (final JWTDecodeException e) {
                makeBearerInvalidTokenResponse(request, response);
            } catch (final TokenExpiredException e) {
                makeBearerTokeExpiredResponse(request, response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void makeBearerInvalidTokenResponse(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        response.reset();
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        final var description = Error.E1014.getTitle();
        final var body = ProblemDetail.forStatusAndDetail(FORBIDDEN, description);
        body.setTitle(description);
        body.setInstance(URI.create(request.getRequestURI().toString()));
        body.setProperty("code", Error.E1014);
        response.getOutputStream().write(objectMapper.writeValueAsString(body).getBytes());
    }

    private void makeBearerTokeExpiredResponse(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        response.reset();
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        final var description = Error.E1013.getTitle();
        final var body = ProblemDetail.forStatusAndDetail(FORBIDDEN, description);
        body.setTitle(description);
        body.setInstance(URI.create(request.getRequestURI().toString()));
        body.setProperty("code", Error.E1013);
        response.getOutputStream().write(objectMapper.writeValueAsString(body).getBytes());
    }
}
