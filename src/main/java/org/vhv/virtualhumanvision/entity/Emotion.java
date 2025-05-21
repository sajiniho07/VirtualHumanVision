package org.vhv.virtualhumanvision.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "Emotions", schema = "VisionCore")
@Data
public class Emotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Column(name = "emotion_type", nullable = false)
    private String emotionType;

    @Column(name = "detected_at")
    private LocalDateTime detectedAt;
}
