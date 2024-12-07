package org.vhv.virtualhumanvision.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vhv.virtualhumanvision.entity.ParkingSlotInfo;

public interface ParkingSlotInfoRepository extends JpaRepository<ParkingSlotInfo, Long> {
}
