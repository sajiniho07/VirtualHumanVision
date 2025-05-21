package org.vhv.virtualhumanvision.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "Objects", schema = "VisionCore")
@Data
public class VhvObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
