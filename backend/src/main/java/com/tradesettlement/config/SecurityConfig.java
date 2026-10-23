package com.tradesettlement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;

    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        if (!securityEnabled) {
            http.csrf().disable().cors().and().authorizeRequests().anyRequest().permitAll();
            return http.build();
        }
        http.csrf().disable()
            .cors().and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests(auth -> auth
                .antMatchers("/api/status", "/actuator/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth -> oauth.jwt());
        return http.build();
    }
}
