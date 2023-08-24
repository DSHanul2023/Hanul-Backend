package com.example.hanul.controller;

import com.example.hanul.dto.BoardDTO;
import com.example.hanul.dto.ResponseDTO;
import com.example.hanul.dto.SurveyDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/survey")
public class SurveyController {
    @PostMapping
    public ResponseEntity<String> processSurvey(@RequestBody SurveyDTO surveyDTO) {
        // 클라이언트로부터 받은 정보를 이용하여 추천 로직 수행
        String category = surveyDTO.getCategory();
        List<String> selectedItems = surveyDTO.getSelectedItems();

        // 여기에 추천 로직을 구현하고 결과를 생성하여 반환하면 됩니다.
        // 예시로 응답을 만들어 반환하겠습니다.
        String response = "선택한 " + category + " : " + selectedItems.toString() + " 에 대한 추천 결과입니다.";

        return ResponseEntity.ok(response);
    }
    public String handleFlask(String question) throws Exception {
        try {
            String flaskUrl = "http://localhost:5000/process";

            // 플라스크 서버에 전송할 JSON 데이터
            String requestData = String.format("{\"question\": \"%s\"}", question);

            // HTTP 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // HTTP 요청 엔티티 생성
            HttpEntity<String> requestEntity = new HttpEntity<>(requestData, headers);

            // RestTemplate을 사용하여 플라스크 서버에 POST 요청 전송
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(flaskUrl, requestEntity, String.class);

            // 플라스크 서버로부터 받은 JSON 응답 데이터
            String responseBody = responseEntity.getBody();

            return responseBody;
        } catch (Exception e) {
            return e.toString();
        }
    }
}
