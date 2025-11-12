package iuh.fit.haitebooks_backend.security;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

@Component
public class JwtUtil {

    private final Key key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret:}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs
    ) {
        this.expirationMs = expirationMs;

        if (secret == null || secret.isBlank()) {
            // dev fallback: dùng một key tạm (CHỈ DÙNG TRONG DEV)
            byte[] fallback = "dev-secret-dev-secret-dev-secret-1234".getBytes(StandardCharsets.UTF_8);
            this.key = Keys.hmacShaKeyFor(Arrays.copyOf(fallback, 32)); // đảm bảo 32 bytes min
            System.out.println("⚠️ Warning: JWT secret empty, using DEV fallback key. Set app.jwt.secret in properties.");
        } else {
            try {
                // thử decode base64 trước; nếu fail thì dùng raw bytes
                byte[] keyBytes;
                try {
                    keyBytes = Base64.getDecoder().decode(secret);
                } catch (IllegalArgumentException ex) {
                    keyBytes = secret.getBytes(StandardCharsets.UTF_8);
                }
                // ensure length >= 32
                if (keyBytes.length < 32) {
                    keyBytes = Arrays.copyOf(keyBytes, 32);
                }
                this.key = Keys.hmacShaKeyFor(keyBytes);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create JWT signing key: " + e.getMessage(), e);
            }
        }
    }

    public String generateToken(String username, List<String> roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(username) // ✅ giữ lại subject
                .claim("roles", roles) // ✅ thêm roles đúng cách, không đè mất subject
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            System.out.println("Token hợp lệ!");
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token hết hạn!");
            return false;
        } catch (JwtException | IllegalArgumentException ex) {
            System.out.println("Token không hợp lệ: " + ex.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
        Object roles = claims.get("roles");
        if (roles instanceof List) {
            return (List<String>) roles;
        }
        return List.of();
    }
}
