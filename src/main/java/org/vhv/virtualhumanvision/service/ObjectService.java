package org.vhv.virtualhumanvision.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vhv.virtualhumanvision.dto.response.ObjectDto;
import org.vhv.virtualhumanvision.entity.VhvObject;
import org.vhv.virtualhumanvision.repository.ObjectRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ObjectService {

    private final ObjectRepository objectRepository;

    @Transactional
    public Long createObject(String name) {
        VhvObject vhvObject = new VhvObject();
        vhvObject.setName(name);
        vhvObject.setCreatedAt(LocalDateTime.now());
        objectRepository.save(vhvObject);
        return vhvObject.getId();
    }

    @Transactional(readOnly = true)
    public List<ObjectDto> getAllObjects() {
        return objectRepository.findAll().stream()
                .map(obj -> new ObjectDto(obj.getId(), obj.getName()))
                .collect(Collectors.toList());
    }
}
