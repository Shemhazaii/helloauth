package com.shemhazaicraft.helloauth.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import java.util.UUID;

@Configuration
public class RegisteredClientConfig {

    @Bean
    public RegisteredClientRepository registeredClientRepository() {

        RegisteredClient nextJsClient =
                RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId("hello-auth-nextjs")

                        .clientAuthenticationMethod(
                                ClientAuthenticationMethod.NONE
                        )

                        .authorizationGrantType(
                                AuthorizationGrantType.AUTHORIZATION_CODE
                        )

                        .redirectUri(
                                "http://localhost:3000/auth/callback"
                        )

                        .scope(OidcScopes.OPENID)
                        .scope(OidcScopes.PROFILE)
                        .scope(OidcScopes.EMAIL)

                        .clientSettings(
                                ClientSettings.builder()
                                        .requireProofKey(true)
                                        .requireAuthorizationConsent(false)
                                        .build()
                        )

                        .build();

        return new InMemoryRegisteredClientRepository(
                nextJsClient
        );
    }

}
