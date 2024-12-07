package org.vhv.virtualhumanvision.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vhv.virtualhumanvision.dto.request.ParkingTrainingRequest;
import org.vhv.virtualhumanvision.service.ParkingSlotService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/parking-slot")
@Slf4j
public class ParkingSlotAPI {

    private final ParkingSlotService parkingSlotService;

    @PostMapping("/saveAndTrainModel")
    public ResponseEntity<String> saveAndTrainModel(@RequestBody ParkingTrainingRequest request) {
        parkingSlotService.saveAndTrainModel(request);
        return ResponseEntity.ok("Parking training data saved successfully.");
    }

}
