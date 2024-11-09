package imigration.api.service;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

@Service
public class JwtService {

    private final String secret;

    public JwtService(@Value("jwt.secret") final String secret) {
        this.secret = secret;
    }

    public String generate(final String subject, final List<String> authorities) {
        return JWT.create()
                    .withIssuer("ImigrationAPI")
                    .withSubject(subject)
                    .withExpiresAt(Instant.now().plusSeconds(900))
                    .withClaim("authorities", authorities)
                    .sign(Algorithm.HMAC512(secret));

    }
}
