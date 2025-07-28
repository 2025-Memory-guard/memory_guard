package com.example.memory_guard.utils.jwt;

import com.example.memory_guard.global.auth.dto.TokenDto;
import com.example.memory_guard.global.auth.utils.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;
    private String testSecretKey = "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdGVzdC1jYXNlLXRoaXMtaXMtc2VjcmV0LWtleQ==";
    private long accessTokenExpiration = 1800000L; // 30분
    private long refreshTokenExpiration = 1209600000L; // 2주

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(testSecretKey, accessTokenExpiration, refreshTokenExpiration);
    }

    @Test
    @DisplayName("JWT 토큰 생성 테스트")
    void generateTokenTest() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "testUser",
            "password",
            Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        TokenDto tokenDto = jwtProvider.generateToken(authentication);

        assertThat(tokenDto).isNotNull();
        assertThat(tokenDto.getGrantType()).isEqualTo("Bearer");
        assertThat(tokenDto.getAccessToken()).isNotNull();
        assertThat(tokenDto.getRefreshToken()).isNotNull();
    }

    @Test
    @DisplayName("Access Token 생성 및 파싱 테스트")
    void createAccessTokenTest() {
        String subject = "testUser";
        String authorities = "ROLE_USER,ROLE_ADMIN";

        String accessToken = jwtProvider.createAccessToken(subject, authorities);

        assertThat(accessToken).isNotNull();
        
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(testSecretKey));
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(accessToken)
            .getBody();

        assertThat(claims.getSubject()).isEqualTo(subject);
        assertThat(claims.get("auth")).isEqualTo(authorities);
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    @DisplayName("Refresh Token 생성 테스트")
    void createRefreshTokenTest() {
        String subject = "testUser";

        String refreshToken = jwtProvider.createRefreshToken(subject);

        assertThat(refreshToken).isNotNull();
        
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(testSecretKey));
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(refreshToken)
            .getBody();

        assertThat(claims.getSubject()).isEqualTo(subject);
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    @DisplayName("토큰에서 Authentication 객체 추출 테스트")
    void getAuthenticationTest() {
        String subject = "testUser";
        String authorities = "ROLE_USER,ROLE_ADMIN";
        String accessToken = jwtProvider.createAccessToken(subject, authorities);

        Authentication authentication = jwtProvider.getAuthentication(accessToken);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo(subject);
        assertThat(authentication.getAuthorities()).hasSize(2);
        assertThat(authentication.getAuthorities())
            .extracting("authority")
            .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("유효한 토큰 검증 테스트")
    void validateValidTokenTest() {
        String subject = "testUser";
        String authorities = "ROLE_USER";
        String accessToken = jwtProvider.createAccessToken(subject, authorities);

        boolean isValid = jwtProvider.validateToken(accessToken);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("잘못된 토큰 검증 테스트")
    void validateInvalidTokenTest() {
        String invalidToken = "invalid.jwt.token";

        boolean isValid = jwtProvider.validateToken(invalidToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰 검증 테스트")
    void validateExpiredTokenTest() {
        JwtProvider expiredJwtProvider = new JwtProvider(testSecretKey, -1000L, refreshTokenExpiration);
        String expiredToken = expiredJwtProvider.createAccessToken("testUser", "ROLE_USER");

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean isValid = jwtProvider.validateToken(expiredToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("권한 정보가 없는 토큰에서 Authentication 추출 시 예외 발생 테스트")
    void getAuthenticationWithoutAuthoritiesTest() {
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(testSecretKey));
        String tokenWithoutAuth = Jwts.builder()
            .setSubject("testUser")
            .setExpiration(new Date(System.currentTimeMillis() + 1800000))
            .signWith(key)
            .compact();

        assertThatThrownBy(() -> jwtProvider.getAuthentication(tokenWithoutAuth))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("권한 정보가 없는 토큰입니다.");
    }

    @Test
    @DisplayName("토큰 만료 시간 검증 테스트")
    void tokenExpirationTest() {
        String subject = "testUser";
        String authorities = "ROLE_USER";
        
        String accessToken = jwtProvider.createAccessToken(subject, authorities);
        String refreshToken = jwtProvider.createRefreshToken(subject);

        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(testSecretKey));
        
        Claims accessClaims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(accessToken)
            .getBody();
            
        Claims refreshClaims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(refreshToken)
            .getBody();

        long accessTokenDuration = accessClaims.getExpiration().getTime() - new Date().getTime();
        assertThat(accessTokenDuration).isBetween(accessTokenExpiration - 1000, accessTokenExpiration + 1000);

        long refreshTokenDuration = refreshClaims.getExpiration().getTime() - new Date().getTime();
        assertThat(refreshTokenDuration).isBetween(refreshTokenExpiration - 1000, refreshTokenExpiration + 1000);
    }

    @Test
    @DisplayName("성공: 토큰에서 사용자 ID(subject)를 올바르게 추출한다")
    void should_get_userId_from_token() {
        String expectedUserId = "myUserId";
        String accessToken = jwtProvider.createAccessToken(expectedUserId, "ROLE_USER");

        String actualUserId = jwtProvider.getUserIdFromToken(accessToken);

        assertThat(actualUserId).isEqualTo(expectedUserId);
    }
}