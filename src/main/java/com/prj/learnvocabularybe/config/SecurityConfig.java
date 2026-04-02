package com.prj.learnvocabularybe.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Tắt CSRF vì dùng JWT (REST API stateless)
            .csrf(AbstractHttpConfigurer::disable)

            // Cấu hình CORS cho phép mobile app gọi API
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Stateless session — không dùng HttpSession
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Quy tắc phân quyền
            .authorizeHttpRequests(auth -> auth
                    // Cho phép đăng ký / đăng nhập không cần token
                    .requestMatchers("/api/auth/**").permitAll()
                    // Cho phép màn Home lấy dashboard thống kê khi chưa đăng nhập trong chế độ test/demo
                    .requestMatchers("/api/study/dashboard").permitAll()
                    // Cho phép chat AI dùng được ở web/mobile test mà chưa cần đăng nhập
                    .requestMatchers("/api/study/chat").permitAll()
                    // Tất cả API còn lại cần token hợp lệ
                    .anyRequest().authenticated()
            )

            // Trả JSON thay vì redirect HTML khi chưa xác thực
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"message\": \"Bạn cần đăng nhập để thực hiện chức năng này\"}");
                    })
            )

            // Thêm JWT filter trước filter xác thực mặc định của Spring
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Cấu hình CORS: cho phép web (localhost) và mobile (mọi origin)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Chỉ cho phép web dev server khi test; mobile app không bị giới hạn bởi CORS
        config.setAllowedOriginPatterns(List.of("http://localhost:8081", "http://192.168.115.104:8081"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
