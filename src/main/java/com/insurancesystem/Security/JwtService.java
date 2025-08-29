package com.insurancesystem.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtService {

    private final Key key;
    private final long ttlMillis;

    // تخزين التوكنات الملغاة حتى انتهاء صلاحيتها (in-memory)
    private final ConcurrentHashMap<String, Long> revoked = new ConcurrentHashMap<>();

    public JwtService(
            @Value("${app.jwt.secret}") String secret, @Value("${app.jwt.ttl-ms:86400000}") long ttlMillis // 24h افتراضياً
    ) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("app.jwt.secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlMillis = ttlMillis;
    }

    // أبسط توليد
    public String generateToken(String username) {
        return generateToken(username, Map.of());
    }

    // توليد مع Claims إضافية (اختياري)
    public String generateToken(String username, Map<String, Object> extraClaims) {
        long now = System.currentTimeMillis();
        JwtBuilder builder = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ttlMillis))
                .signWith(key, SignatureAlgorithm.HS256);

        if (extraClaims != null && !extraClaims.isEmpty()) {
            builder.addClaims(extraClaims);
        }
        return builder.compact();
    }

    public String extractUsername(String token) {
        return getAllClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return getAllClaims(token).getExpiration();
    }

    public boolean isTokenValid(String token, String username) {
        try {
            if (isRevoked(token)) return false;
            Claims claims = getAllClaims(token);
            boolean notExpired = claims.getExpiration() != null && claims.getExpiration().after(new Date());
            boolean subjectOk = username == null || username.equalsIgnoreCase(claims.getSubject());
            return notExpired && subjectOk;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // تعليم التوكن كملغى حتى وقت انتهاء صلاحيته
    public void revoke(String token) {
        try {
            Date exp = extractExpiration(token);
            if (exp != null) {
                revoked.put(token, exp.getTime());
            }
        } catch (Exception ignored) { }
    }

    public boolean isRevoked(String token) {
        Long exp = revoked.get(token);
        if (exp == null) return false;
        if (exp <= System.currentTimeMillis()) {
            revoked.remove(token);
            return false;
        }
        return true;
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
