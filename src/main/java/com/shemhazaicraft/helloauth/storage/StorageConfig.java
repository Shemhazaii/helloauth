package com.shemhazaicraft.helloauth.storage;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    @Bean
    public MinioClient storageClient(StorageProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.url())
                .credentials(
                        properties.accessKey(),
                        properties.secretKey()
                )
                .build();
    }

}
