package org.vhv.virtualhumanvision.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vhv.virtualhumanvision.entity.Text;
import org.vhv.virtualhumanvision.repository.TextRepository;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class TextService {

    private final TextRepository textRepository;

    @Transactional
    public void createText(String content) {
        Text text = new Text();
        text.setContent(content);
        text.setDetectedAt(LocalDateTime.now());
        textRepository.save(text);
    }
}
