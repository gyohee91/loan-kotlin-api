package com.ghyinc.finance.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .formLogin { it.disable() }
            .httpBasic { it.disable() } //H2 콘솔 열기 위해 필요(H2 콘솔은 iframe 사용하므로 frameOptions 설정 필요)
            .headers { headers ->
                headers.frameOptions { it.sameOrigin() }
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/api-docs/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/h2-console/**",
                        "/actuator/**"
                    ).permitAll()
                    .requestMatchers(
                        "/api/**"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
        return http.build()
    }
}
