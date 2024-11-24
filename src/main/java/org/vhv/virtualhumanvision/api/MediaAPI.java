package org.vhv.virtualhumanvision.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.vhv.virtualhumanvision.configuration.MediaConfig;
import org.vhv.virtualhumanvision.dto.GeneralResult;
import org.vhv.virtualhumanvision.entity.Media;
import org.vhv.virtualhumanvision.service.MediaService;

import java.io.File;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/media")
@Slf4j
public class MediaAPI {

    private final MediaService mediaService;
    private final MediaConfig mediaConfig;

    @GetMapping("/type/{mediaTypeId}")
    public ResponseEntity<List<Media>> getTopFiveMediasByTypeId(@PathVariable Long mediaTypeId) {
        return ResponseEntity.ok(mediaService.getTopFiveMediasByTypeId(mediaTypeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Media> getMediasById(@PathVariable Long id) {
        Optional<Media> optionalMedia = mediaService.getMediasById(id);
        return optionalMedia.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
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
