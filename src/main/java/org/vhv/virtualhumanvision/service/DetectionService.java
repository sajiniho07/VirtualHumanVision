package org.vhv.virtualhumanvision.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vhv.virtualhumanvision.dto.request.AddDetectionRequest;
import org.vhv.virtualhumanvision.dto.response.DetectionDto;
import org.vhv.virtualhumanvision.entity.Detection;
import org.vhv.virtualhumanvision.entity.Person;
import org.vhv.virtualhumanvision.entity.Text;
import org.vhv.virtualhumanvision.entity.VhvObject;
import org.vhv.virtualhumanvision.repository.DetectionRepository;
import org.vhv.virtualhumanvision.repository.ObjectRepository;
import org.vhv.virtualhumanvision.repository.PersonRepository;
import org.vhv.virtualhumanvision.repository.TextRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DetectionService {

    private final DetectionRepository detectionRepository;
    private final PersonRepository personRepository;
    private final ObjectRepository objectRepository;
    private final TextRepository textRepository;

    @Transactional
    public void createDetection(AddDetectionRequest request) {
        Double confidence = request.confidence();
        Long personId = request.personId();
        Long objectId = request.objectId();
        Long textId = request.textId();

        Detection detection = new Detection();
        detection.setConfidence(confidence);
        detection.setDetectedAt(LocalDateTime.now());
        if (personId != null) {
            Person person = personRepository.findById(personId)
                    .orElseThrow(() -> new IllegalArgumentException("Person not found"));
            detection.setPerson(person);
        }
        if (objectId != null) {
            VhvObject vhvObject = objectRepository.findById(objectId)
                    .orElseThrow(() -> new IllegalArgumentException("Object not found"));
            detection.setVhvObject(vhvObject);
        }
        if (textId != null) {
            Text text = textRepository.findById(textId)
                    .orElseThrow(() -> new IllegalArgumentException("Text not found"));
            detection.setText(text);
        }
        detectionRepository.save(detection);
    }

    @Transactional(readOnly = true)
    public List<DetectionDto> getAllDetections() {
        return detectionRepository.findAll().stream()
                .map(detection -> new DetectionDto(
                        detection.getId(),
                        detection.getPerson() != null ? detection.getPerson().getId() : null,
                        detection.getPerson() != null ? detection.getPerson().getName() : null,
                        detection.getVhvObject() != null ? detection.getVhvObject().getId() : null,
                        detection.getVhvObject() != null ? detection.getVhvObject().getName() : null,
                        detection.getText() != null ? detection.getText().getId() : null,
                        detection.getText() != null ? detection.getText().getContent() : null,
                        detection.getConfidence()
                ))
                .collect(Collectors.toList());
    }
}
