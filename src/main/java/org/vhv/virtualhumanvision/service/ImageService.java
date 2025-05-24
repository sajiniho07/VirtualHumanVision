package org.vhv.virtualhumanvision.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.vhv.virtualhumanvision.configuration.MediaConfig;
import org.vhv.virtualhumanvision.entity.Image;
import org.vhv.virtualhumanvision.entity.Person;
import org.vhv.virtualhumanvision.entity.VhvObject;
import org.vhv.virtualhumanvision.repository.ImageRepository;
import org.vhv.virtualhumanvision.repository.ObjectRepository;
import org.vhv.virtualhumanvision.repository.PersonRepository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class ImageService {

    private final ImageRepository imageRepository;
    private final PersonRepository personRepository;
    private final ObjectRepository objectRepository;

    private final MediaConfig mediaConfig;

    @Transactional
    public void uploadImage(MultipartFile file, Long personId, Long objectId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String uploadDir = mediaConfig.getUploadBaseDir();
        File dest = new File(uploadDir + fileName);
        file.transferTo(dest);

        Image image = new Image();
        image.setFilePath(uploadDir + fileName);
        image.setOriginalPath(file.getOriginalFilename());
        image.setUploadedAt(LocalDateTime.now());
        if (personId != null) {
            Person person = personRepository.findById(personId)
                    .orElseThrow(() -> new IllegalArgumentException("Person not found"));
            image.setPerson(person);
        } else if (objectId != null) {
            VhvObject vhvObject = objectRepository.findById(objectId)
                    .orElseThrow(() -> new IllegalArgumentException("Object not found"));
            image.setVhvObject(vhvObject);
        }
        imageRepository.save(image);
    }
}
