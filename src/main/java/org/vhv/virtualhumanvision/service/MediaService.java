package org.vhv.virtualhumanvision.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vhv.virtualhumanvision.configuration.MediaConfig;
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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;
    private final MediaConfig mediaConfig;

    @Transactional(readOnly = true)
    public List<Media> getMedias() {
        return mediaRepository.findAllByDisabledFalse();
    }

    @Transactional(readOnly = true)
    public List<Media> getTopFiveMediasByTypeId(Long mediaTypeId) {
        return mediaRepository.findTop5ByDisabledFalseAndMediaType_IdOrderByCreatedAtDesc(mediaTypeId);
    }

    @Transactional(readOnly = true)
    public Optional<Media> getMediaByRefId(Long refId) {
        return mediaRepository.findByRefIdAndAndDisabledFalse(refId);
    }

    @Transactional
    public Media save(File destinationFile) {
        Media media = new Media();
        media.setName(destinationFile.getName());
        MediaType mediaType = new MediaType();
        long mediaTypeId = EnumMediaType.VIDEO.getId();
        String mediaTypeName = EnumMediaType.VIDEO.getName();
        if (isImageFile(destinationFile)) {
            mediaTypeId = EnumMediaType.IMAGE.getId();
            mediaTypeName = EnumMediaType.IMAGE.getName();
        }
        mediaType.setId(mediaTypeId);
        mediaType.setName(mediaTypeName);
        media.setMediaType(mediaType);
        String path = mediaConfig.getMediaUrl() + destinationFile.getName();
        media.setPath(path);
        media.setOriginalPath(destinationFile.getPath());
        ZonedDateTime currentDateTime = ZonedDateTime.now();
        media.setCreatedAt(currentDateTime);
        return mediaRepository.save(media);
    }

    @Transactional
    public Media saveByMediaTypeAndRefId(File destinationFile, EnumMediaType mediaTypeItem, Long refId) {
        Media media = new Media();
        media.setName(destinationFile.getName());
        media.setRefId(refId);
        MediaType mediaType = new MediaType();
        mediaType.setId(mediaTypeItem.getId());
        mediaType.setName(mediaTypeItem.getName());
        media.setMediaType(mediaType);
        String path = mediaConfig.getMediaUrl() + destinationFile.getName();
        media.setPath(path);
        media.setOriginalPath(destinationFile.getPath());
        ZonedDateTime currentDateTime = ZonedDateTime.now();
        media.setCreatedAt(currentDateTime);
        return mediaRepository.save(media);
    }

    private boolean isImageFile(File file) {
        String[] imageExtensions = {"jpg", "jpeg", "png", "gif", "bmp"};
        String fileName = file.getName().toLowerCase();
        for (String extension : imageExtensions) {
            if (fileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public boolean disableMedia(Long id) {
        return mediaRepository.findById(id)
                .map(media -> {
                    removeFileFromServer(media.getOriginalPath());
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

    @Transactional(readOnly = true)
    public Optional<Media> getMediasById(Long id) {
        return mediaRepository.findByIdAndDisabledFalse(id);
    }
}
