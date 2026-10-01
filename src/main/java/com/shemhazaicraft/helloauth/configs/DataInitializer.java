package com.shemhazaicraft.helloauth.configs;

import com.shemhazaicraft.helloauth.user.Role;
import com.shemhazaicraft.helloauth.user.RoleRepository;
import com.shemhazaicraft.helloauth.user.User;
import com.shemhazaicraft.helloauth.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            Role userRole = roleRepository
                    .findByName("USER")
                    .orElseGet(() ->
                            roleRepository.save(
                                    new Role("USER")
                            )
                    );

            Role adminRole = roleRepository
                    .findByName("ADMIN")
                    .orElseGet(() ->
                            roleRepository.save(
                                    new Role("ADMIN")
                            )
                    );

            if (!userRepository.existsByUsername("admin")) {

                User admin = new User(
                        "admin",
                        "admin@localhost",
                        passwordEncoder.encode("admin123")
                );

                admin.getRoles().add(userRole);
                admin.getRoles().add(adminRole);

                userRepository.save(admin);
            }
        };
    }
}
