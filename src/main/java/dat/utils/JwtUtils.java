package dat.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;

public class JwtUtils {
    private static final Key key = loadSecretKey();
    private static final long EXPIRATION_TIME_MS = 86400000; // 24 hours

    private static Key loadSecretKey() {
        String base64Key = System.getenv("JWT_SECRET");
        if (base64Key == null || base64Key.isEmpty()) {
            throw new RuntimeException("❌ JWT_SECRET environment variable is not set");
        }

        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        return Keys.hmacShaKeyFor(decodedKey);
    }


    public static String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .signWith(key)
                .compact();
    }

    public static String getSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
