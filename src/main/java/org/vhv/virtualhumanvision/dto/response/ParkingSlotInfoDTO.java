package org.vhv.virtualhumanvision.dto.response;

import java.util.List;

public record ParkingSlotInfoDTO(
        Boolean isEmpty,
        List<CoordinatesInfoDTO> coordinatesInfos) {
}
