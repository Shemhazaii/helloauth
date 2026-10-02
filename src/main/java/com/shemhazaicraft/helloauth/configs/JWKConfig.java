package com.shemhazaicraft.helloauth.configs;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JWKConfig {

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(
                jwkSource
        );
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {

        try {
            RSAPrivateKey privateKey = loadPrivateKey(
                    Path.of(".secrets/auth-private.pem")
            );

            RSAPublicKey publicKey = loadPublicKey(
                    Path.of(".secrets/auth-public.pem")
            );

            RSAKey rsaKey = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID("hello-auth-key")
                    .build();

            JWKSet jwkSet = new JWKSet(rsaKey);

            return (jwkSelector, securityContext) ->
                    jwkSelector.select(jwkSet);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to load RSA signing key",
                    exception
            );
        }
    }

    private RSAPrivateKey loadPrivateKey(Path path) throws Exception {

        byte[] keyBytes = readPem(path, "PRIVATE KEY");

        PKCS8EncodedKeySpec keySpec =
                new PKCS8EncodedKeySpec(keyBytes);

        KeyFactory keyFactory =
                KeyFactory.getInstance("RSA");

        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    private RSAPublicKey loadPublicKey(Path path) throws Exception {

        byte[] keyBytes = readPem(path, "PUBLIC KEY");

        X509EncodedKeySpec keySpec =
                new X509EncodedKeySpec(keyBytes);

        KeyFactory keyFactory =
                KeyFactory.getInstance("RSA");

        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    private byte[] readPem(Path path, String type) throws IOException {

        String pem = Files.readString(path);

        String base64 = pem
                .replace("-----BEGIN " + type + "-----", "")
                .replace("-----END " + type + "-----", "")
                .replaceAll("\\s+", "");

        return Base64.getDecoder().decode(base64);
    }



}
