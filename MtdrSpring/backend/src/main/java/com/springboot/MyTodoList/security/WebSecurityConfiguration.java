package com.springboot.MyTodoList.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfiguration.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/update-token", "/send-email").permitAll() // Allow access to /update-token and /home without authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> {
                logger.info("Form login disabled for simplicity");
                form.disable();
            })
            .logout(logout -> {
                logger.info("Logout disabled for simplicity");
                logout.disable();
            });

        return http.build();
    }
}
