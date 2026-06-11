package com.company.dms.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey key;
    private final long expireMillis;

    public JwtUtil(@Value("${dms.jwt.secret}") String secret,
                   @Value("${dms.jwt.expire-minutes}") long expireMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMillis = expireMinutes * 60_000L;
    }

    public String generate(Long userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(key)
                .compact();
    }

    private Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }

    public String getUsername(String token) {
        return parse(token).get("username", String.class);
    }

    public boolean isValid(String token) {
        try {
            if (!isCanonical(token)) {
                return false;
            }
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Rejects tokens whose base64url segments are not canonically encoded.
     * jjwt's parser leniently decodes a final segment that has a trailing
     * character carrying no full byte (length % 4 == 1), so e.g. {@code token + "x"}
     * decodes to the same signature bytes and would otherwise verify as valid.
     */
    private boolean isCanonical(String token) {
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) {
            return false;
        }
        Base64.Decoder decoder = Base64.getUrlDecoder();
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        for (String part : parts) {
            String reencoded = encoder.encodeToString(decoder.decode(part));
            if (!reencoded.equals(part)) {
                return false;
            }
        }
        return true;
    }
}
