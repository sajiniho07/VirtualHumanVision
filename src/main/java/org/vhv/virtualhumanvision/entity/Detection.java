package org.vhv.virtualhumanvision.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "Detections", schema = "VisionCore")
@Data
public class Detection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;

    @ManyToOne
    @JoinColumn(name = "object_id")
    private VhvObject vhvObject;

    @ManyToOne
    @JoinColumn(name = "text_id")
    private Text text;

    @Column(nullable = false)
    private Double confidence;

    @Column(name = "detected_at")
    private LocalDateTime detectedAt;
}
