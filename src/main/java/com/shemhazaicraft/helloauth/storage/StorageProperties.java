package com.shemhazaicraft.helloauth.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage")
public record StorageProperties(
        String url,
        String accessKey,
        String secretKey,
        String bucketName
) {
}
