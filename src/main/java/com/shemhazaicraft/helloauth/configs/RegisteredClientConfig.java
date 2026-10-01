package com.shemhazaicraft.helloauth.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import java.util.UUID;


@Configuration
public class RegisteredClientConfig {

    @Bean
    public RegisteredClientRepository registeredClientRepository(
            JdbcTemplate jdbcTemplate
    ) {
        JdbcRegisteredClientRepository repository =
                new JdbcRegisteredClientRepository(jdbcTemplate);

        RegisteredClient client =
                repository.findByClientId("hello-auth-nextjs");

        if (client == null) {
            client = RegisteredClient
                    .withId(UUID.randomUUID().toString())
                    .clientId("hello-auth-nextjs")
                    .clientAuthenticationMethod(
                            org.springframework.security.oauth2.core
                                    .ClientAuthenticationMethod.NONE
                    )
                    .authorizationGrantType(
                            org.springframework.security.oauth2.core
                                    .AuthorizationGrantType.AUTHORIZATION_CODE
                    )
                    .redirectUri(
                            "http://localhost:3000/auth/callback"
                    )
                    .scope(
                            org.springframework.security.oauth2.core.oidc.OidcScopes.OPENID
                    )
                    .scope(
                            org.springframework.security.oauth2.core.oidc.OidcScopes.PROFILE
                    )
                    .scope(
                            org.springframework.security.oauth2.core.oidc.OidcScopes.EMAIL
                    )
                    .scope("wadididaw")
                    .clientSettings(
                            ClientSettings.builder()
                                    .requireProofKey(true)
                                    .requireAuthorizationConsent(true)
                                    .build()
                    )
                    .build();

            repository.save(client);
        }

        System.out.println(
                "=== HELLOAUTH REGISTERED CLIENT ==="
        );
        System.out.println(
                "clientId = " + client.getClientId()
        );
        System.out.println(
                "requireProofKey = " +
                        client.getClientSettings().isRequireProofKey()
        );
        System.out.println(
                "requireAuthorizationConsent = " +
                        client.getClientSettings()
                                .isRequireAuthorizationConsent()
        );
        System.out.println(
                "scopes = " + client.getScopes()
        );
        System.out.println(
                "===================================="
        );

        return repository;
    }

}
