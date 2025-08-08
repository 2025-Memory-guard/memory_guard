package com.example.memory_guard.global.auth.utils;
import com.example.memory_guard.global.auth.dto.TokenDto;
import com.example.memory_guard.user.domain.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtProvider {

  private final Key key;
  private final long accessTokenExpirationMillis;
  private final long refreshTokenExpirationMillis;
  private final UserRepository userRepository;

  public JwtProvider(@Value("${jwt.secret}") String secretKey,
                     @Value("${jwt.access-token-expiration-millis}") long accessToken,
                     @Value("${jwt.refresh-token-expiration-millis}") long refreshToken, UserRepository userRepository) {
    this.userRepository = userRepository;
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


  /// // 여기가 문제 나의 User객체를 UserDetail로 구현하면 해결가능
  public Authentication getAuthentication(String accessToken) {
    Claims claims = parseClaims(accessToken);

    if (claims.get("auth") == null) {
      throw new RuntimeException("권한 정보가 없는 토큰입니다.");
    }

    String userId = claims.getSubject();

    UserDetails principal = userRepository.findByUserProfileUserId(userId)
        .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));

    return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
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