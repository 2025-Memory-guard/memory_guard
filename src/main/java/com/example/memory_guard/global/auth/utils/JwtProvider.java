package com.example.memory_guard.global.auth.utils;
import com.example.memory_guard.global.auth.dto.TokenDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtProvider {

  private final Key key;
  private final long accessTokenExpirationMillis;
  private final long refreshTokenExpirationMillis;

  public JwtProvider(@Value("${jwt.secret}") String secretKey,
                     @Value("${jwt.access-token-expiration-millis}") long accessToken,
                     @Value("${jwt.refresh-token-expiration-millis}") long refreshToken) {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    this.key = Keys.hmacShaKeyFor(keyBytes);
    this.accessTokenExpirationMillis = accessToken;
    this.refreshTokenExpirationMillis = refreshToken;
  }

  public String getUserIdFromToken(String token) {
    return parseClaims(token).getSubject();
  }

  public TokenDto generateToken(Authentication authentication) {
    String authorities = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(","));

    String subject = authentication.getName();

    String accessToken = createAccessToken(subject, authorities);
    String refreshToken = createRefreshToken(subject);

    return TokenDto.builder()
        .grantType("Bearer")
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();
  }

  public String createAccessToken(String subject, String authorities) {
    long now = (new Date()).getTime();
    Date accessTokenExpiresIn = new Date(now + accessTokenExpirationMillis);

    return Jwts.builder()
        .setSubject(subject)
        .claim("auth", authorities)
        .setExpiration(accessTokenExpiresIn)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public String createRefreshToken(String subject) {
    long now = (new Date()).getTime();
    Date refreshTokenExpiresIn = new Date(now + refreshTokenExpirationMillis);

    return Jwts.builder()
        .setSubject(subject)
        .setExpiration(refreshTokenExpiresIn)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }


  public Authentication getAuthentication(String accessToken) {
    Claims claims = parseClaims(accessToken);

    if (claims.get("auth") == null) {
      throw new RuntimeException("권한 정보가 없는 토큰입니다.");
    }

    Collection<? extends GrantedAuthority> authorities =
        Arrays.stream(claims.get("auth").toString().split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

    UserDetails principal = new User(claims.getSubject(), "", authorities);
    return new UsernamePasswordAuthenticationToken(principal, "", authorities);
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
      log.info("Invalid JWT Token", e);
    } catch (ExpiredJwtException e) {
      log.info("Expired JWT Token", e);
    } catch (UnsupportedJwtException e) {
      log.info("Unsupported JWT Token", e);
    } catch (IllegalArgumentException e) {
      log.info("JWT claims string is empty.", e);
    }
    return false;
  }

  private Claims parseClaims(String accessToken) {
    try {
      return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
    } catch (ExpiredJwtException e) {
      return e.getClaims();
    }
  }
}