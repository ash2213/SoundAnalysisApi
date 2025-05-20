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
        try {
            Properties props = new Properties();
            props.load(JwtUtils.class.getClassLoader().getResourceAsStream("config.properties"));
            String base64Key = props.getProperty("jwt.secret");

            byte[] decodedKey = Base64.getDecoder().decode(base64Key);
            return Keys.hmacShaKeyFor(decodedKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JWT secret key", e);
        }
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
