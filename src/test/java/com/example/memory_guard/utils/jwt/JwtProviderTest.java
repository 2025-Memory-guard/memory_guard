package com.example.memory_guard.utils.jwt;

import com.example.memory_guard.global.auth.dto.TokenDto;
import com.example.memory_guard.global.auth.utils.JwtProvider;
import com.example.memory_guard.user.domain.Role;
import com.example.memory_guard.user.domain.User;
import com.example.memory_guard.user.domain.UserProfile;
import com.example.memory_guard.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtProviderTest {

    @Mock
    private UserRepository userRepository;

    private JwtProvider jwtProvider;

    private final String testSecretKey = "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdGVzdC1jYXNlLXRoaXMtaXMtc2VjcmV0LWtleQ==";
    private final long accessTokenExpiration = 1800000L;
    private final long refreshTokenExpiration = 1209600000L;

    private User mockUser;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(testSecretKey, accessTokenExpiration, refreshTokenExpiration, userRepository);

        UserProfile userProfile = UserProfile.builder()
            .userId("testUser")
            .username("테스트사용자")
            .password("encodedPassword")
            .build();
        mockUser = User.builder().userProfile(userProfile).build();
        mockUser.addRole(Role.builder().name("ROLE_USER").build());
    }

    @Test
    @DisplayName("토큰 생성 테스트 - 실제 User 객체 기반")
    void generateTokenTest() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            mockUser,
            null,
            mockUser.getAuthorities()
        );

        TokenDto tokenDto = jwtProvider.generateToken(authentication);

        assertThat(tokenDto).isNotNull();
        assertThat(tokenDto.getGrantType()).isEqualTo("Bearer");
        assertThat(tokenDto.getAccessToken()).isNotNull();
        assertThat(jwtProvider.getUserIdFromToken(tokenDto.getAccessToken())).isEqualTo("testUser");
    }

    @Test
    @DisplayName("성공: 토큰에서 Authentication 객체 추출")
    void getAuthentication_Success() {
        String accessToken = jwtProvider.createAccessToken("testUser", "ROLE_USER");

        when(userRepository.findByUserProfileUserId("testUser")).thenReturn(Optional.of(mockUser));

        Authentication authentication = jwtProvider.getAuthentication(accessToken);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isInstanceOf(User.class);
        assertThat(authentication.getPrincipal()).isEqualTo(mockUser); // Principal이 우리가 설정한 mockUser와 동일한지 확인
        assertThat(authentication.getAuthorities()).hasSize(1);
        assertThat(authentication.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("실패: 토큰에 해당하는 사용자가 DB에 없을 경우 예외 발생")
    void getAuthentication_UserNotFound() {
        String accessToken = jwtProvider.createAccessToken("ghostUser", "ROLE_USER");

        when(userRepository.findByUserProfileUserId("ghostUser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jwtProvider.getAuthentication(accessToken))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessage("사용자를 찾을 수 없습니다: ghostUser");

        verify(userRepository, times(1)).findByUserProfileUserId("ghostUser");
    }
}