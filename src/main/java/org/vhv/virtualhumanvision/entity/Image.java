package org.vhv.virtualhumanvision.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "Images", schema = "VisionCore")
@Data
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToOne
    @JoinColumn(name = "object_id")
    private VhvObject vhvObject;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "original_path", nullable = false)
    private String originalPath;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
}