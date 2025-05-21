package org.vhv.virtualhumanvision.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.vhv.virtualhumanvision.dto.request.AddDetectionRequest;
import org.vhv.virtualhumanvision.dto.request.AddEmotionRequest;
import org.vhv.virtualhumanvision.dto.request.AddObjectRequest;
import org.vhv.virtualhumanvision.dto.request.AddTextRequest;
import org.vhv.virtualhumanvision.dto.response.DetectionDto;
import org.vhv.virtualhumanvision.dto.response.ObjectDto;
import org.vhv.virtualhumanvision.dto.response.PersonDto;
import org.vhv.virtualhumanvision.entity.Person;
import org.vhv.virtualhumanvision.enums.ObjectType;
import org.vhv.virtualhumanvision.service.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class VisionController {

    private final PersonService personService;
    private final ObjectService objectService;
    private final ImageService imageService;
    private final EmotionService emotionService;
    private final TextService textService;
    private final DetectionService detectionService;

    @Operation(summary = "Add a new person or object", description = "Creates a new person or object based on the provided type (PERSON or OBJECT).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully added"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping("/objects")
    public ResponseEntity<String> addObject(@Valid @RequestBody AddObjectRequest request) {
        if (ObjectType.PERSON.equals(request.type())) {
            personService.createPerson(request.name());
            return ResponseEntity.ok("Person added: " + request.name());
        } else {
            objectService.createObject(request.name());
            return ResponseEntity.ok("Object added: " + request.name());
        }
    }

    @Operation(summary = "Upload an image", description = "Uploads an image associated with a person or object to the Images table.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or request data")
    })
    @PostMapping(value = "/images", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "personId", required = false) Long personId,
            @RequestParam(value = "objectId", required = false) Long objectId) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        try {
            imageService.uploadImage(file, personId, objectId);
            return ResponseEntity.ok("Image uploaded.");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to upload image: " + e.getMessage());
        }
    }

    @Operation(summary = "Add a new emotion", description = "Records a new emotion for a person in the Emotions table.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Emotion added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping("/emotions")
    public ResponseEntity<String> addEmotion(@Valid @RequestBody AddEmotionRequest request) {
        emotionService.createEmotion(request.personId(), request.emotionType());
        return ResponseEntity.ok("Emotion added: " + request.emotionType());
    }

    @Operation(summary = "Add a new text", description = "Records a new detected text in the Texts table.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Text added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping("/texts")
    public ResponseEntity<String> addText(@Valid @RequestBody AddTextRequest request) {
        textService.createText(request.content());
        return ResponseEntity.ok("Text added.");
    }

    @Operation(summary = "Add a new detection", description = "Records a new detection with optional person, object, or text.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detection added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping("/detections")
    public ResponseEntity<String> addDetection(@Valid @RequestBody AddDetectionRequest request) {
        detectionService.createDetection(request);
        return ResponseEntity.ok("Detection added with confidence: " + request.confidence());
    }

    @Operation(summary = "Get all persons", description = "Retrieves a list of all persons from the Persons table.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved persons"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/persons")
    public ResponseEntity<List<PersonDto>> getPersons() {
        return ResponseEntity.ok(personService.getAllPersons());
    }

    @Operation(summary = "Get all objects", description = "Retrieves a list of all objects from the Objects table.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved objects"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/objects")
    public ResponseEntity<List<ObjectDto>> getObjects() {
        return ResponseEntity.ok(objectService.getAllObjects());
    }

    @Operation(summary = "Get all detections", description = "Retrieves a list of all detections from the Detections table.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved detections"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/detections")
    public ResponseEntity<List<DetectionDto>> getDetections() {
        return ResponseEntity.ok(detectionService.getAllDetections());
    }
}
