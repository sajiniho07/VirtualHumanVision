package org.vhv.virtualhumanvision.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vhv.virtualhumanvision.entity.Media;

import java.util.List;
import java.util.Optional;

public interface MediaRepository extends JpaRepository<Media, Long> {
    List<Media> findAllByDisabledFalse();

    List<Media> findTop5ByDisabledFalseAndMediaType_IdOrderByCreatedAtDesc(Long mediaTypeId);

    Optional<Media> findByIdAndDisabledFalse(Long id);

}
