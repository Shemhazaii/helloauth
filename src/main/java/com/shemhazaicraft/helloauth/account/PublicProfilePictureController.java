
package com.shemhazaicraft.helloauth.account;

import com.shemhazaicraft.helloauth.storage.ObjectStorageService;
import com.shemhazaicraft.helloauth.user.User;
import com.shemhazaicraft.helloauth.user.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/users")
public class PublicProfilePictureController {

    private final UserRepository userRepository;
    private final ObjectStorageService objectStorageService;

    public PublicProfilePictureController(
            UserRepository userRepository,
            ObjectStorageService objectStorageService
    ) {
        this.userRepository = userRepository;
        this.objectStorageService = objectStorageService;
    }

    @GetMapping("/{id}/picture")
    public ResponseEntity<Void> getPicture(@PathVariable UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        String objectKey = user.getPicture();

        if (objectKey == null || objectKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Profile picture not found"
            );
        }

        String downloadUrl =
                objectStorageService.generateDownloadUrl(objectKey);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, downloadUrl)
                .build();
    }
}
