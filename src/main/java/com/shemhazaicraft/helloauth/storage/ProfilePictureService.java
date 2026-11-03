
package com.shemhazaicraft.helloauth.storage;

import com.shemhazaicraft.helloauth.admin.user.dto.ProfilePictureResponse;
import com.shemhazaicraft.helloauth.user.User;
import com.shemhazaicraft.helloauth.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class ProfilePictureService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final ObjectStorageService objectStorageService;
    private final UserRepository userRepository;

    public ProfilePictureService(
            ObjectStorageService objectStorageService,
            UserRepository userRepository
    ) {
        this.objectStorageService = objectStorageService;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProfilePictureResponse upload(User user, MultipartFile file) {

        validateFile(file);

        byte[] content;

        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to read uploaded image",
                    e
            );
        }

        ImageType imageType = detectImageType(content);


        if (imageType == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only JPEG, PNG, and WebP images are supported"
            );
        }

        String objectKey = "avatars/%s/%s.%s".formatted(
                user.getId(),
                UUID.randomUUID(),
                imageType.extension()
        );



        try {
            objectStorageService.upload(
                    objectKey,
                    new ByteArrayInputStream(content),
                    content.length,
                    imageType.contentType()
            );
        } catch (RuntimeException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to upload profile picture",
                    e
            );
        }

        String previousPicture = user.getPicture();

        user.setPicture(objectKey);
        userRepository.saveAndFlush(user);

        if (isOwnedAvatar(user, previousPicture)) {
            deleteAfterCommit(previousPicture);
        }

        return new ProfilePictureResponse(
                objectStorageService.generateDownloadUrl(objectKey)
        );
    }


    @Transactional
    public void delete(User user) {
        String previousPicture = user.getPicture();

        if (previousPicture == null) {
            return;
        }

        user.setPicture(null);
        userRepository.saveAndFlush(user);

        if (isOwnedAvatar(user, previousPicture)) {
            deleteAfterCommit(previousPicture);
        }
    }


    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Profile picture is required"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.CONTENT_TOO_LARGE,
                    "Profile picture must not exceed 5 MB"
            );
        }
    }

    private ImageType detectImageType(byte[] content) {
        // JPEG: FF D8 FF
        if (content.length >= 3
                && (content[0] & 0xFF) == 0xFF
                && (content[1] & 0xFF) == 0xD8
                && (content[2] & 0xFF) == 0xFF) {
            return new ImageType("jpg", "image/jpeg");
        }

        // PNG signature
        if (content.length >= 8
                && (content[0] & 0xFF) == 0x89
                && content[1] == 0x50
                && content[2] == 0x4E
                && content[3] == 0x47
                && content[4] == 0x0D
                && content[5] == 0x0A
                && content[6] == 0x1A
                && content[7] == 0x0A) {
            return new ImageType("png", "image/png");
        }

        // WebP: RIFF....WEBP
        if (content.length >= 12
                && content[0] == 'R'
                && content[1] == 'I'
                && content[2] == 'F'
                && content[3] == 'F'
                && content[8] == 'W'
                && content[9] == 'E'
                && content[10] == 'B'
                && content[11] == 'P') {
            return new ImageType("webp", "image/webp");
        }

        return null;
    }

    private boolean isOwnedAvatar(User user, String objectKey) {
        return objectKey != null
                && objectKey.startsWith("avatars/" + user.getId() + "/");
    }

    private void deleteAfterCommit(String objectKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            log.warn("Transaction synchronization is unavailable; old avatar was not deleted: {}", objectKey);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            objectStorageService.delete(objectKey);
                        } catch (RuntimeException e) {
                            log.warn("Failed to delete previous avatar: {}", objectKey, e);
                        }
                    }
                }
        );
    }

    private record ImageType(String extension, String contentType) {}
}
