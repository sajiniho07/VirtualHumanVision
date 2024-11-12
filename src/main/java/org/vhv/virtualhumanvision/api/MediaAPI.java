package org.vhv.virtualhumanvision.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.vhv.virtualhumanvision.entity.Media;
import org.vhv.virtualhumanvision.service.MediaService;

import java.io.File;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/media")
@Slf4j
public class MediaAPI {

    private final MediaService mediaService;
    private final String UPLOAD_DIR = "D:\\final project dba\\server\\res";

    @GetMapping("/")
    public ResponseEntity<List<Media>> getMedias() {
        return ResponseEntity.ok(mediaService.getMedias());
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }

        try {
            File destinationFile = new File(UPLOAD_DIR + File.separator + file.getOriginalFilename());
            file.transferTo(destinationFile);
            mediaService.save(destinationFile);
            return ResponseEntity.ok("File uploaded successfully: " + destinationFile.getAbsolutePath());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to upload file: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<String> disableMedia(@PathVariable Long id) {
        boolean isDisabled = mediaService.disableMedia(id);

        if (isDisabled) {
            return ResponseEntity.ok("Media record disabled successfully.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
