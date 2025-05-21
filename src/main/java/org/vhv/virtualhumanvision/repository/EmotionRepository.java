package org.vhv.virtualhumanvision.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vhv.virtualhumanvision.entity.Emotion;

public interface EmotionRepository extends JpaRepository<Emotion, Long> {
}
