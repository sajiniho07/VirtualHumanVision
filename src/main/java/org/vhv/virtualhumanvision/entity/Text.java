package org.vhv.virtualhumanvision.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "Texts", schema = "VisionCore")
@Data
public class Text {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "detected_at")
    private LocalDateTime detectedAt;
}