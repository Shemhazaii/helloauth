package com.shemhazaicraft.helloauth.user;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_roles_name",
                        columnNames = "name"
                )
        }
)
@Getter
@Setter
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

}
