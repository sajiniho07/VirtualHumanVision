package org.vhv.virtualhumanvision.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vhv.virtualhumanvision.entity.CoordinatesInfo;

public interface CoordinatesInfoRepository extends JpaRepository<CoordinatesInfo, Long> {
}
