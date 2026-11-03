
package com.shemhazaicraft.helloauth.account;

import com.shemhazaicraft.helloauth.admin.user.dto.ProfilePictureResponse;
import com.shemhazaicraft.helloauth.storage.ProfilePictureService;
import com.shemhazaicraft.helloauth.user.User;
import com.shemhazaicraft.helloauth.user.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/account/profile/picture")
public class AccountProfileController {

    private final ProfilePictureService profilePictureService;
    private final UserRepository userRepository;

    public AccountProfileController(
            ProfilePictureService profilePictureService,
            UserRepository userRepository
    ) {
        this.profilePictureService = profilePictureService;
        this.userRepository = userRepository;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfilePictureResponse uploadPicture(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart("file") MultipartFile file
    ) {
        User user = getAuthenticatedUser(jwt);

        return profilePictureService.upload(user, file);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePicture(
            @AuthenticationPrincipal Jwt jwt
    ) {
        User user = getAuthenticatedUser(jwt);

        profilePictureService.delete(user);
    }

    private User getAuthenticatedUser(Jwt jwt) {
        try {
            UUID userId = UUID.fromString(jwt.getSubject());

            return userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "User not found"
                    ));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid user identity"
            );
        }
    }
}
