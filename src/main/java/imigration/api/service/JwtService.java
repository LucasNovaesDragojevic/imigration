package imigration.api.service;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

@Service
public class JwtService {

    private final String issuer;
    private final Algorithm algorithm;

    public JwtService(@Value("jwt.secret") final String secret) {
        this.issuer = "ImigrationAPI";
        this.algorithm = Algorithm.HMAC512(secret);
    }

    public String generate(final String subject, final List<String> authorities) {
        return JWT.create()
                    .withIssuer(issuer)
                    .withSubject(subject)
                    .withExpiresAt(Instant.now().plusSeconds(900))
                    .withClaim("authorities", authorities)
                    .sign(algorithm);

    }

    public String validate(final String jwt) {
        return JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(jwt)
                    .getSubject();
    }
}
