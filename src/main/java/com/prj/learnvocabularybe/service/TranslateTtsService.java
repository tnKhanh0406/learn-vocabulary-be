package com.prj.learnvocabularybe.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class TranslateTtsService {

    public byte[] synthesizeEnglishToMp3(String englishText) {
        String encoded = URLEncoder.encode(englishText, StandardCharsets.UTF_8);
        String url = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=en&q=" + encoded;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0");

        ResponseEntity<byte[]> res = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);

        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            throw new RuntimeException("TTS failed: " + res.getStatusCode());
        }
        return res.getBody();
    }
}
