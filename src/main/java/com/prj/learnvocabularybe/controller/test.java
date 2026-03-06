package com.prj.learnvocabularybe.controller;

import com.prj.learnvocabularybe.entity.WordEntity;
import com.prj.learnvocabularybe.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class test {

    private final WordRepository wordRepository;

    @GetMapping("/hello")
    public String hello() {
        List<WordEntity> wordEntities = wordRepository.findAll();
        return "Hello World! " + wordEntities.size() + " words in database.";
    }
}
