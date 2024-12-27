package org.vhv.virtualhumanvision.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vhv.virtualhumanvision.dto.request.ParkingTrainingRequest;
import org.vhv.virtualhumanvision.dto.response.GeneralResult;
import org.vhv.virtualhumanvision.entity.Media;
import org.vhv.virtualhumanvision.enums.EnumMediaType;
import org.vhv.virtualhumanvision.service.ImageProcessingService;
import org.vhv.virtualhumanvision.service.MediaService;
import org.vhv.virtualhumanvision.service.ParkingSlotService;

import java.io.File;

@RestController
@RequiredArgsConstructor
@RequestMapping("/parking-slot")
@Slf4j
public class ParkingSlotAPI {

    private final ParkingSlotService parkingSlotService;
    private final MediaService mediaService;
    private final ImageProcessingService imageProcessingService;

    @PostMapping("/generateTrainArffFile")
    public ResponseEntity<GeneralResult> generateTrainArffFile(@RequestBody ParkingTrainingRequest request) {
        String mediaOriginalPath = parkingSlotService.saveParkingSlotInfos(request);
        File trainArrfFile = imageProcessingService.generateTrainArffFile(request, mediaOriginalPath);
        Media trainArffMedia = mediaService.saveByMediaTypeAndRefId(trainArrfFile, EnumMediaType.ARFF, request.getMediaId());
        GeneralResult result = new GeneralResult(trainArffMedia.getId(), trainArffMedia.getPath(), "Parking training data saved successfully.");
        return ResponseEntity.ok(result);
    }

}
