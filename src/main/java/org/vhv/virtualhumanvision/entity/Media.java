package org.vhv.virtualhumanvision.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity(name = "Media")
@Table(name = "media", schema = "media")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", columnDefinition = "NVARCHAR(250)", nullable = false)
    private String name;
    @Column(name = "path", columnDefinition = "NVARCHAR(1000)", nullable = false)
    private String path;
    @ManyToOne
    @JoinColumn(name = "media_typeid")
    private MediaType mediaType;
    @Column(name = "disabled", columnDefinition = "BIT", nullable = false)
    private boolean disabled;
    @Column(name = "created_at", columnDefinition = "DATETIME", nullable = false)
    private ZonedDateTime createdAt;
    @Column(name = "modified_at", columnDefinition = "DATETIME")
    private ZonedDateTime modifiedAt;
}
