package com.shemhazaicraft.helloauth.admin.user;

import com.shemhazaicraft.helloauth.admin.user.dto.CreateUserRequest;
import com.shemhazaicraft.helloauth.admin.user.dto.UserAdminResponse;
import com.shemhazaicraft.helloauth.user.Role;
import com.shemhazaicraft.helloauth.user.RoleRepository;
import com.shemhazaicraft.helloauth.user.User;
import com.shemhazaicraft.helloauth.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserAdminService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "username",
            "email",
            "enabled",
            "createdAt",
            "updatedAt"
    );

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAdminService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserAdminResponse findUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        return toResponse(user);
    }

    public Page<UserAdminResponse> findUsers(
            int page,
            int size,
            String sortBy,
            String direction,
            String search,
            Boolean enabled
    ) {

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Unsupported sort field: " + sortBy
            );
        }

        Sort.Direction sortDirection;

        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Direction must be ASC or DESC"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        Specification<User> specification = Specification
                .where(hasSearch(search))
                .and(hasEnabledStatus(enabled));

        return userRepository.findAll(specification, pageable)
                .map(this::toResponse);
    }



    @Transactional
    public UserAdminResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists"
            );
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }

        Set<String> requestedRoles = request.roles() == null || request.roles().isEmpty()
                ? Set.of("USER")
                : request.roles();

        Set<Role> roles = requestedRoles.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Invalid role: " + roleName
                        )))
                .collect(Collectors.toSet());

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEnabled(true);
        user.setRoles(roles);

        User savedUser = userRepository.saveAndFlush(user);

        return toResponse(savedUser);
    }


    private Specification<User> hasSearch(String search) {

        return (root, query, criteriaBuilder) -> {

            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String keyword = "%"
                    + search.trim().toLowerCase(Locale.ROOT)
                    + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("username")),
                            keyword
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("email")),
                            keyword
                    )
            );
        };
    }

    private Specification<User> hasEnabledStatus(Boolean enabled) {

        return (root, query, criteriaBuilder) -> {

            if (enabled == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("enabled"),
                    enabled
            );
        };
    }

    private UserAdminResponse toResponse(User user) {

        Set<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toSet());

        return new UserAdminResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                roles,
                user.getPicture(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}