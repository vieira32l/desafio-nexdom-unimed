package com.nexdom.beneficiarios.security;

import com.nexdom.beneficiarios.usuario.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final String CLAIM_TENANT_ID = "tenantId";
    private static final String CLAIM_TENANT_NOME = "tenantNome";

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.expirationMs = properties.expirationMs();
    }

    public String generateToken(Usuario usuario) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(usuario.getLogin())
                .claim(CLAIM_TENANT_ID, usuario.getTenant().getId().toString())
                .claim(CLAIM_TENANT_NOME, usuario.getTenant().getNome())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key)
                .compact();
    }

    public AuthenticatedUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String login = claims.getSubject();
        UUID tenantId = UUID.fromString(claims.get(CLAIM_TENANT_ID, String.class));
        String tenantNome = claims.get(CLAIM_TENANT_NOME, String.class);
        return new AuthenticatedUser(login, tenantId, tenantNome);
    }
}
