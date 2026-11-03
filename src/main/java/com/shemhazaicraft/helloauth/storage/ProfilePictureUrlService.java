
package com.shemhazaicraft.helloauth.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProfilePictureUrlService {

    private final String publicBaseUrl;

    public ProfilePictureUrlService(
            @Value("${helloauth.public-base-url}") String publicBaseUrl
    ) {
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
    }

    public String generateUrl(UUID userId) {
        return publicBaseUrl
                + "/api/v1/public/users/"
                + userId
                + "/picture";
    }
}
