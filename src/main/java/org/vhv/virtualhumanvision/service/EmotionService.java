package org.vhv.virtualhumanvision.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vhv.virtualhumanvision.entity.Emotion;
import org.vhv.virtualhumanvision.entity.Person;
import org.vhv.virtualhumanvision.repository.EmotionRepository;
import org.vhv.virtualhumanvision.repository.PersonRepository;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class EmotionService {

    private final EmotionRepository emotionRepository;
    private final PersonRepository personRepository;

    @Transactional
    public void createEmotion(Long personId, String emotionType) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new IllegalArgumentException("Person not found"));
        Emotion emotion = new Emotion();
        emotion.setPerson(person);
        emotion.setEmotionType(emotionType);
        emotion.setDetectedAt(LocalDateTime.now());
        emotionRepository.save(emotion);
    }
}
