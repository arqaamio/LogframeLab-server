package com.arqaam.logframelab.configuration.security.jwt;

import com.arqaam.logframelab.exception.InvalidJwsTokenException;
import com.arqaam.logframelab.model.persistence.auth.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtTokenProvider {

    public static final String TOKEN_TYPE = "JWS";
    private static SecretKey secretKey;

    @Value("${jwt.expiration.in.hours}")
    private int expirationInHours;

    @Value("${jwt.header.prefix}")
  private String tokenType;

    @Value("${jwt.header}")
    private String tokenHeader;

    @Value("${jwt.header.prefix}")
    private String tokenHeaderPrefix;

    public JwtTokenProvider() {
        secretKey = secretKey == null ? Keys.secretKeyFor(SignatureAlgorithm.HS512) : secretKey;
    }

    /**
     * Generates a JWT token
     *
     * @param user User which will be the subject of JWT token
     * @return JWT token
     */
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(expirationInHours, ChronoUnit.HOURS)))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Retrieves username from a JWS String
     *
     * @param jws Compact serialized Claims JWS string
     * @return Username of the user who generated this JWS string
     */
    public String getUsernameFromJws(String jws) {
        Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(jws);
        return claims.getBody().getSubject();
    }

    /**
     * Verifies if the JWS is valid
     *
     * @param jws Compact serialized Claims JWS string
     * @return If token is valid
     */
    public boolean isTokenValid(String jws) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(jws);
            return true;
        } catch (ExpiredJwtException ex) {
            throw new InvalidJwsTokenException(TOKEN_TYPE, jws, "Expired token");
        } catch (UnsupportedJwtException ex) {
            throw new InvalidJwsTokenException(TOKEN_TYPE, jws, "Unsupported token");
        } catch (MalformedJwtException ex) {
            throw new InvalidJwsTokenException(TOKEN_TYPE, jws, "Malformed token");
        } catch (SignatureException ex) {
            throw new InvalidJwsTokenException(TOKEN_TYPE, jws, "Incorrect token signature");
        } catch (IllegalArgumentException ex) {
            throw new InvalidJwsTokenException(TOKEN_TYPE, jws, "Illegal token");
        } catch (Exception e) {
            throw new InvalidJwsTokenException(TOKEN_TYPE, jws, e.getMessage());
        }
    }

    /**
     * Retrieves expiry date of JWS string
     *
     * @param jws Compact serialized Claims JWS string
     * @return Date in which JWS expiries
     */
    public Date jwtExpiry(String jws) {
        if (StringUtils.isBlank(jws)) {
            return Date.from(Instant.now());
        }

        return Jwts.parserBuilder().setSigningKey(secretKey).build()
                .parseClaimsJws(jws).getBody().getExpiration();
    }

    /**
     * Return the expiration of a JWT in seconds
     *
     * @return Expiration of a JWT in seconds
     */
    public Long getExpirationInSeconds() {
        return expirationInHours * 60 * 60L;
    }

    /**
     * Returns the token type
     *
     * @return Token type
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * Returns the token header
     *
     * @return Token header
     */
    public String getTokenHeader() {
        return tokenHeader;
    }

    /**
     * Retuns the token header prefix
     *
     * @return Token header prefix
     */
    public String getTokenHeaderPrefix() {
        return tokenHeaderPrefix;
    }

    /**
     * Generates a JWT Token from a previous token
     *
     * @param jws Compact serialized Claims JWS string
     * @return JWT token
     */
    public String refreshToken(String jws){

        isTokenValid(jws);
        String username = getUsernameFromJws(jws);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(expirationInHours, ChronoUnit.HOURS)))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

}
