package com.shemhazaicraft.helloauth.admin.user.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserAdminResponse(
        UUID id,
        String username,
        String email,
        boolean enabled,
        Set<String> roles,
        String picture,
        Instant createdAt,
        Instant updatedAt
) {}