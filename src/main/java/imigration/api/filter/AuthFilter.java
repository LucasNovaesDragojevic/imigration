package imigration.api.filter;

import java.io.IOException;
import java.util.Objects;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
    
    public AuthFilter(final JwtService jwtService,
                        final UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(@NonNull final HttpServletRequest request, 
                                    @NonNull final HttpServletResponse response, 
                                    @NonNull final FilterChain filterChain) throws ServletException, IOException {
        final var bearerHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (Objects.nonNull(bearerHeader)) {
            final var username = jwtService.validate(bearerHeader.replace("Bearer ", ""));
            final var user = userService.findByUsername(username);
            final var usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        }
        filterChain.doFilter(request, response);
    }

}
