package com.example.demo.Entitty;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import  com.example.demo.enums.Role;

import java.util.List;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(nullable = false)
    @Check(constraints = "LENGTH(email) >= 5 AND email LIKE '%@%'")
    private String email;

    @Column(
            nullable = false,
            length = 255
    )
    @Check(constraints = "LENGTH(password) >= 8")
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String permission;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Images> images;

    public List<String> getPermissionList() {
        if (permission == null || permission.isBlank()) {
            return List.of(); // no extra permissions
        }
        return List.of(permission.split(","));
    }

}