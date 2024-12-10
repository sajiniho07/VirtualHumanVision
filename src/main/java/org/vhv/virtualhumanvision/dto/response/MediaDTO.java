package org.vhv.virtualhumanvision.dto.response;

import java.util.List;

public record MediaDTO(
        Long id,
        String name,
        String path,
        List<ParkingSlotInfoDTO> parkingSlotInfos
) {
}
