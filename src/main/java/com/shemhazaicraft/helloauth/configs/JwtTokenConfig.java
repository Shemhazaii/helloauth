package com.shemhazaicraft.helloauth.configs;

import com.shemhazaicraft.helloauth.user.Role;
import com.shemhazaicraft.helloauth.user.User;
import com.shemhazaicraft.helloauth.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Configuration
public class JwtTokenConfig {

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer(
            UserRepository userRepository
    ) {
        return context -> {

            String username = context.getPrincipal().getName();

            User user = userRepository
                    .findByUsername(username)
                    .orElse(null);

            if (user == null) {
                return;
            }

            if (context.getTokenType().getValue().equals("access_token")) {

                context.getClaims()
                        .claim("username", user.getUsername())
                        .claim("email", user.getEmail())
                        .claim(
                                "roles",
                                user.getRoles()
                                        .stream()
                                        .map(Role::getName)
                                        .collect(
                                                Collectors.toCollection(
                                                        ArrayList::new
                                                )
                                        )
                        );

                return;
            }

            if (context.getTokenType().getValue().equals("id_token")) {

                context.getClaims()
                        .claim(
                                "preferred_username",
                                user.getUsername()
                        )
                        .claim(
                                "email",
                                user.getEmail()
                        );
            }
        };
    }
}