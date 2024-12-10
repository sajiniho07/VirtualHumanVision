package org.vhv.virtualhumanvision.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.vhv.virtualhumanvision.configuration.MediaConfig;
import org.vhv.virtualhumanvision.dto.response.CoordinatesInfoDTO;
import org.vhv.virtualhumanvision.dto.response.GeneralResult;
import org.vhv.virtualhumanvision.dto.response.MediaDTO;
import org.vhv.virtualhumanvision.dto.response.ParkingSlotInfoDTO;
import org.vhv.virtualhumanvision.entity.CoordinatesInfo;
import org.vhv.virtualhumanvision.entity.Media;
import org.vhv.virtualhumanvision.entity.ParkingSlotInfo;
import org.vhv.virtualhumanvision.service.MediaService;

import java.io.File;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/media")
@Slf4j
public class MediaAPI {

    private final MediaService mediaService;
    private final MediaConfig mediaConfig;

    @GetMapping("/type/{mediaTypeId}")
    public ResponseEntity<List<MediaDTO>> getTopFiveMediasByTypeId(@PathVariable Long mediaTypeId) {
        List<Media> medias = mediaService.getTopFiveMediasByTypeId(mediaTypeId);
        List<MediaDTO> mediaDTOS = medias.stream()
                .map(this::convertToDtoForFindAll)
                .toList();
        return ResponseEntity.ok(mediaDTOS);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MediaDTO> getMediasById(@PathVariable Long id) {
        return mediaService.getMediasById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private MediaDTO convertToDtoForFindAll(Media media) {
        return new MediaDTO(
                media.getId(),
                media.getName(),
                media.getPath(),
                null
        );
    }

    private MediaDTO convertToDto(Media media) {
        List<ParkingSlotInfoDTO> parkingSlotInfos = media.getParkingSlotInfos().stream()
                .map(this::convertToParkingSlotInfoDto)
                .toList();
        return new MediaDTO(
                media.getId(),
                media.getName(),
                media.getPath(),
                parkingSlotInfos
        );
    }

    private ParkingSlotInfoDTO convertToParkingSlotInfoDto(ParkingSlotInfo parkingSlotInfo) {
        List<CoordinatesInfoDTO> coordinatesInfos = parkingSlotInfo.getCoordinatesInfos().stream()
                .map(this::convertToCoordinatesInfoDto)
                .toList();

        return new ParkingSlotInfoDTO(
                parkingSlotInfo.getIsEmpty(),
                coordinatesInfos
        );
    }

    private CoordinatesInfoDTO convertToCoordinatesInfoDto(CoordinatesInfo coordinatesInfo) {
        return new CoordinatesInfoDTO(
                coordinatesInfo.getPointOrder(),
                coordinatesInfo.getX(),
                coordinatesInfo.getY(),
                coordinatesInfo.getRealX(),
                coordinatesInfo.getRealY()
        );
    }

    @PostMapping("/upload")
    public ResponseEntity<GeneralResult> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new GeneralResult(-1L, "", "Please select a file to upload."));
        }

        try {
            File destinationFile = new File(mediaConfig.getUploadBaseDir() + File.separator + file.getOriginalFilename());
            file.transferTo(destinationFile);
            Media media = mediaService.save(destinationFile);
            GeneralResult result = new GeneralResult(media.getId(), media.getPath(), "File uploaded successfully.");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new GeneralResult(-1L, null, "Failed to upload file: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<GeneralResult> disableMedia(@PathVariable Long id) {
        boolean isDisabled = mediaService.disableMedia(id);

        if (isDisabled) {
            return ResponseEntity.ok(new GeneralResult(1L, null, "File deleted successfully."));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
