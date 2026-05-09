package com.example.demo.Entitty;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Reports {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String failureType;       // what failed ex: "Crack", "Blur", "Missing Part"

    @Column(nullable = false, length = 1000)
    private String description;       // details about the failure

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    private boolean resolved = false;          // whether the issue has been addressed

    @OneToOne
    @JoinColumn(name = "image_id", nullable = false, unique = true)
    @JsonBackReference
    private Images image;
}