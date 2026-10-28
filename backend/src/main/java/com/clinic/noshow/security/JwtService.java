package com.clinic.noshow.security;

import com.clinic.noshow.model.UserAccount;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;

    public JwtService(@Value("${app.auth.jwt-secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String issue(UserAccount account) {
        Long actorId = account.getRole() == UserAccount.Role.DOCTOR
                ? account.getDoctor().getId() : account.getPatient().getId();
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(account.getId().toString())
                .claim("actorId", actorId)
                .claim("email", account.getEmail())
                .claim("name", account.getDisplayName())
                .claim("role", account.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60 * 60 * 12)))
                .signWith(key)
                .compact();
    }

    public AccountPrincipal read(String token) {
        var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return new AccountPrincipal(
                Long.valueOf(claims.getSubject()),
                claims.get("actorId", Long.class),
                claims.get("email", String.class),
                claims.get("name", String.class),
                claims.get("role", String.class));
    }
}
