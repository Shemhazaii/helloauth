package com.shemhazaicraft.helloauth.admin.user;

import com.shemhazaicraft.helloauth.admin.user.dto.CreateUserRequest;
import com.shemhazaicraft.helloauth.admin.user.dto.UserAdminResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
public class UserAdminController {

    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public Page<UserAdminResponse> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean enabled
    ) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must not be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Size must be between 1 and 100"
            );
        }

        return userAdminService.findUsers(
                page,
                size,
                sortBy,
                direction,
                search,
                enabled
        );
    }

    @GetMapping("/{id}")
    public UserAdminResponse getUserById(@PathVariable UUID id) {
        return userAdminService.findUserById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserAdminResponse createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return userAdminService.createUser(request);
    }
}