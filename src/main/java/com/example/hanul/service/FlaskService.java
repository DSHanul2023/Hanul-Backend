package com.example.hanul.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FlaskService {
    private final RestTemplate restTemplate;
    private final String flaskUrl = "http://localhost:5000";

    @Autowired
    public FlaskService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 챗봇 생성 모델 연결
    public String chatWithFlask(String question) {
        try {
            String chatUrl = flaskUrl + "/process";

            // Flask 서버에 보낼 JSON 데이터 생성
            String requestData = String.format("{\"question\": \"%s\"}", question);

            // HTTP 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // HTTP 요청 엔티티 생성
            HttpEntity<String> requestEntity = new HttpEntity<>(requestData, headers);

            // RestTemplate을 사용하여 플라스크 서버에 POST 요청 전송
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(chatUrl, requestEntity, String.class);

            // 플라스크 서버로부터 받은 JSON 응답 데이터
            String responseBody = responseEntity.getBody();

            return responseBody;
        } catch (Exception e) {
            return e.toString();
        }
    }
}
