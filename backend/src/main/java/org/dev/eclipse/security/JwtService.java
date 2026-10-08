package org.dev.eclipse.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.dev.eclipse.enumarate.Role;
import org.dev.eclipse.exception.AuthExceptions.AuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.csrf-token-expiration}")
    private long csrfTokenExpiration;

    public String generateAccessToken(String email, Role role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("token_type", "access")
                .claim("role", "ROLE_" + role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .claim("token_type", "refresh")
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateCsrfToken(String email) {
        return Jwts.builder()
                .claim("token_type", "csrf")
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + csrfTokenExpiration))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public void validateToken(String token) {
        try {
            extractAllClaims(token);
        } catch (ExpiredJwtException e) {
            throw new AuthException("Token süresi dolmuş");
        } catch (Exception e) {
            throw new AuthException("Token geçersiz");
        }
    }

    public void validateTokenType(String token, String expectedType) {
        validateToken(token);
        if (!expectedType.equals(extractAllClaims(token).get("token_type", String.class))) {
            throw new AuthException("Token türü geçersiz");
        }
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
    }
}
