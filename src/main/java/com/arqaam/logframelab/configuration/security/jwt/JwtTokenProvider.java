package com.arqaam.logframelab.configuration.security.jwt;

import com.arqaam.logframelab.exception.InvalidJwsTokenException;
import com.arqaam.logframelab.model.persistence.auth.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

  public static final String TOKEN_TYPE = "JWS";
  private static SecretKey secretKey;

  @Value("${jwt.expiration}")
  private Long jwtExpirationInMillis;

  @Value("${jwt.header.prefix}")
  private String tokenType;

  @Value("${jwt.header}")
  private String tokenHeader;

  @Value("${jwt.header.prefix}")
  private String tokenHeaderPrefix;

  public JwtTokenProvider() {
    secretKey = secretKey == null ? Keys.secretKeyFor(SignatureAlgorithm.HS512) : secretKey;
  }

  public String generateJwsToken(User user) {
    return Jwts.builder()
        .setSubject(user.getUsername())
        .setIssuedAt(Date.from(Instant.now()))
        .setExpiration(Date.from(Instant.now().plusMillis(jwtExpirationInMillis)))
        .signWith(secretKey, SignatureAlgorithm.HS512)
        .compact();
  }

  public String getUsernameFromJws(String jws) {
    Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(jws);
    return claims.getBody().getSubject();
  }

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

  public Long getJwtExpirationInMillis() {
    return jwtExpirationInMillis;
  }

  public String getTokenType() {
    return tokenType;
  }

  public String getTokenHeader() {
    return tokenHeader;
  }

  public String getTokenHeaderPrefix() {
    return tokenHeaderPrefix;
  }
}
