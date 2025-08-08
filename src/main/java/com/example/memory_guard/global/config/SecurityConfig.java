package com.example.memory_guard.global.config;

import com.example.memory_guard.global.auth.filter.JwtAuthenticationFilter;
import com.example.memory_guard.global.auth.utils.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtProvider jwtProvider;

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter(JwtProvider jwtProvider){
    return new JwtAuthenticationFilter(jwtProvider);
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .httpBasic(httpBasic -> httpBasic.disable())
        .formLogin(formLogin -> formLogin.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//       JWT 인증처리 비활성화
//        .authorizeHttpRequests(auth -> auth
//            .requestMatchers("/", "/user/login", "/guard/login", "/token/reissue").permitAll()
//            .requestMatchers("/api/ward/**").hasRole("USER")
//            .requestMatchers("/api/guard/**").hasRole("GUARD")
//            .anyRequest().authenticated()
//        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }
}