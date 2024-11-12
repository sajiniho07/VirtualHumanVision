package org.vhv.virtualhumanvision.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vhv.virtualhumanvision.entity.Media;
import org.vhv.virtualhumanvision.entity.MediaType;
import org.vhv.virtualhumanvision.enums.EnumMediaType;
import org.vhv.virtualhumanvision.repository.MediaRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;

    public List<Media> getMedias() {
        return mediaRepository.findAllByDisabledFalse();
    }

    @Transactional
    public void save(File destinationFile) {
        Media media = new Media();
        media.setName(destinationFile.getName());
        MediaType mediaType = new MediaType();
        mediaType.setId(EnumMediaType.VIDEO.getId());
        mediaType.setName(EnumMediaType.VIDEO.getName());
        media.setMediaType(mediaType);
        media.setPath(destinationFile.getPath());
        ZonedDateTime currentDateTime = ZonedDateTime.now();
        media.setCreatedAt(currentDateTime);
        mediaRepository.save(media);
    }

    @Transactional
    public boolean disableMedia(Long id) {
        return mediaRepository.findById(id)
                .map(media -> {
                    removeFileFromServer(media.getPath());
                    media.setDisabled(true);
                    media.setModifiedAt(ZonedDateTime.now());
                    mediaRepository.save(media);
                    return true;
                })
                .orElse(false);
    }

    private void removeFileFromServer(String filePath) {
        try {
            Path pathToFile = Paths.get(filePath);
            Files.deleteIfExists(pathToFile);
        } catch (IOException e) {
            log.error("Failed to delete file: " + e.getMessage());
        }
    }
}
