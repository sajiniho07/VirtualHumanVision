package org.vhv.virtualhumanvision.dto.response;

public record DetectionDto(
        Long id,
        Long personId,
        String personName,
        Long objectId,
        String objectName,
        Long textId,
        String textContent,
        Double confidence) {
}
