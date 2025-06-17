package com.dd3ok.authservice.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { csrf ->
                csrf
                    .ignoringRequestMatchers("/api/v1/auth/**") // API 경로 CSRF 제외
                    .ignoringRequestMatchers("/api/v1/**")      // 전체 API CSRF 제외
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers("/", "/login", "/auth/callback/**").permitAll()
                    .anyRequest().permitAll() // 테스트를 위해 임시로 모든 요청 허용
            }

        return http.build()
    }
}
