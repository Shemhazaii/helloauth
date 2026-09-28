package com.shemhazaicraft.helloauth.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class AuthorizationServerConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .oauth2AuthorizationServer(authorizationServer -> {
                    http.securityMatcher(
                            authorizationServer.getEndpointsMatcher()
                    );

                    authorizationServer
                            .oidc(Customizer.withDefaults());
                });

        return http.build();
    }
}
