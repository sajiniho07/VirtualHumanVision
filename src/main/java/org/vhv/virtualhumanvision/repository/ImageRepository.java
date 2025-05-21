package org.vhv.virtualhumanvision.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vhv.virtualhumanvision.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
