package com.shemhazaicraft.helloauth.configs;

import com.shemhazaicraft.helloauth.storage.ProfilePictureUrlService;
import com.shemhazaicraft.helloauth.user.User;
import com.shemhazaicraft.helloauth.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Configuration
public class AuthorizationServerConfig {

    private final UserRepository userRepository;
    private final ProfilePictureUrlService profilePictureUrlService;

    public AuthorizationServerConfig(UserRepository userRepository, ProfilePictureUrlService profilePictureUrlService) {
        this.userRepository = userRepository;
        this.profilePictureUrlService = profilePictureUrlService;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http, OAuth2AuthorizationService authorizationService, OAuth2AuthorizationConsentService authorizationConsentService) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .oauth2AuthorizationServer(authorizationServer -> {

                    http.securityMatcher(
                            authorizationServer.getEndpointsMatcher()
                    );

                    authorizationServer
                            .authorizationService(authorizationService)
                            .authorizationConsentService(
                                    authorizationConsentService
                            )

                            .oidc(oidc -> oidc
                                    .userInfoEndpoint(userInfo -> userInfo
                                            .userInfoMapper(context -> {
                                                JwtAuthenticationToken principal =
                                                        (JwtAuthenticationToken) context
                                                                .getAuthentication()
                                                                .getPrincipal();

                                                UUID userId = UUID.fromString(
                                                        principal.getToken().getSubject()
                                                );

                                                User user = userRepository.findById(userId)
                                                        .orElseThrow(() -> new OAuth2AuthenticationException(
                                                                new OAuth2Error("invalid_token"),
                                                                "User not found"
                                                        ));

                                                Set<String> scopes = context.getAccessToken().getScopes();

                                                Map<String, Object> claims = new HashMap<>();
                                                claims.put("sub", userId.toString());

                                                if (scopes.contains("profile")) {
                                                    claims.put("preferred_username", user.getUsername());

                                                    if (user.getPicture() != null) {
                                                        claims.put(
                                                                "picture",
                                                                profilePictureUrlService.generateUrl(userId)
                                                        );
                                                    }
                                                }

                                                if (scopes.contains("email")) {
                                                    claims.put("email", user.getEmail());
                                                }

                                                return new OidcUserInfo(claims);
                                            })
                                    )
                            )

                            .authorizationEndpoint(
                                    authorizationEndpoint ->
                                            authorizationEndpoint
                                                    .consentPage(
                                                            "http://localhost:3000/consent"
                                                    )
                            );
                })
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(
                                new LoginUrlAuthenticationEntryPoint(
                                        "/login"
                                )
                        )
                );

        return http.build();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:9000")
                .build();
    }

}

