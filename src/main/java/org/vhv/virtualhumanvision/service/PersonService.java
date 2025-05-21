package org.vhv.virtualhumanvision.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vhv.virtualhumanvision.dto.response.PersonDto;
import org.vhv.virtualhumanvision.entity.Person;
import org.vhv.virtualhumanvision.repository.PersonRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;

    @Transactional
    public void createPerson(String name) {
        Person person = new Person();
        person.setName(name);
        person.setCreatedAt(LocalDateTime.now());
        personRepository.save(person);
    }

    @Transactional(readOnly = true)
    public List<PersonDto> getAllPersons() {
        return personRepository.findAll().stream()
                .map(person -> new PersonDto(person.getId(), person.getName()))
                .collect(Collectors.toList());
    }
}
