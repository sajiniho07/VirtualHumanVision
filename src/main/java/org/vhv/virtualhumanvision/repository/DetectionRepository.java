package org.vhv.virtualhumanvision.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vhv.virtualhumanvision.entity.Detection;

public interface DetectionRepository extends JpaRepository<Detection, Long> {
}