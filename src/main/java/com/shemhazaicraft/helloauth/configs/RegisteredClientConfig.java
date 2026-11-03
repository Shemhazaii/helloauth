package com.shemhazaicraft.helloauth.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import java.util.UUID;


@Configuration
public class RegisteredClientConfig {


    private final PasswordEncoder passwordEncoder;

    public RegisteredClientConfig(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(
            JdbcTemplate jdbcTemplate
    ) {
        JdbcRegisteredClientRepository repository =
                new JdbcRegisteredClientRepository(jdbcTemplate);

        createHelloAuthNextJs(repository);
        createAccountCenter(repository);
        createOpenWebUi(repository);
        createPostman(repository);

        return repository;
    }

    private void createHelloAuthNextJs(
            RegisteredClientRepository repository
    ) {
        if (repository.findByClientId("hello-auth-nextjs") != null) {
            return;
        }

        RegisteredClient client = RegisteredClient
                .withId(UUID.randomUUID().toString())
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
                                .requireAuthorizationConsent(true)
                                .build()
                )
                .build();

        repository.save(client);
    }

    private void createAccountCenter(
            RegisteredClientRepository repository
    ) {
        if (repository.findByClientId("helloauth-account") != null) {
            return;
        }

        RegisteredClient client = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("helloauth-account")
                .clientAuthenticationMethod(
                        ClientAuthenticationMethod.NONE
                )
                .authorizationGrantType(
                        AuthorizationGrantType.AUTHORIZATION_CODE
                )
                .redirectUri(
                        "http://localhost:3001/api/auth/callback/helloauth"
                )
                .postLogoutRedirectUri(
                        "http://localhost:3001"
                )
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
                .clientSettings(
                        ClientSettings.builder()
                                .requireProofKey(true)
                                .requireAuthorizationConsent(true)
                                .build()
                )
                .build();

        repository.save(client);
    }


    private void createOpenWebUi(
            RegisteredClientRepository repository
    ) {
        if (repository.findByClientId("open-webui") != null) {
            return;
        }

        RegisteredClient client = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("open-webui")
                .clientSecret(passwordEncoder.encode(
                        "openWebUiClientSecret"
                ))
                .clientAuthenticationMethod(
                        ClientAuthenticationMethod.CLIENT_SECRET_BASIC
                )
                .authorizationGrantType(
                        AuthorizationGrantType.AUTHORIZATION_CODE
                )
                .redirectUri(
                        "http://localhost:3002/oauth/oidc/callback"
                )
                .postLogoutRedirectUri(
                        "http://localhost:3002"
                )
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
                .clientSettings(
                        ClientSettings.builder()
                                .requireProofKey(true)
                                .requireAuthorizationConsent(true)
                                .build()
                )
                .build();

        repository.save(client);
    }


    private void createPostman(
            RegisteredClientRepository repository
    ) {
        if (repository.findByClientId("postman") != null) {
            return;
        }

        RegisteredClient client = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("postman")
                .clientAuthenticationMethod(
                        ClientAuthenticationMethod.NONE
                )
                .authorizationGrantType(
                        AuthorizationGrantType.AUTHORIZATION_CODE
                )
                .redirectUri(
                        "https://oauth.pstmn.io/v1/callback"
                )
                .postLogoutRedirectUri(
                        "http://localhost:3001"
                )
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
                .clientSettings(
                        ClientSettings.builder()
                                .requireProofKey(true)
                                .requireAuthorizationConsent(true)
                                .build()
                )
                .build();

        repository.save(client);
    }
}
