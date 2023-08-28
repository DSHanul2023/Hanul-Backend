package com.example.hanul.controller;

import com.example.hanul.dto.ResponseDTO;
import com.example.hanul.dto.SurveyDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/survey")
@CrossOrigin(origins = "http://localhost:3000")
public class SurveyController {

    @PostMapping
    public ResponseEntity<ResponseDTO<String>> processSurvey(@RequestBody SurveyDTO surveyDTO) {
        // 클라이언트로부터 받은 정보를 이용하여 추천 로직 수행
        String category = surveyDTO.getCategory();
        List<String> selectedItems = surveyDTO.getSelectedItems();

        // Flask 서버에 선택 항목 전달하고 응답 받아오기
        String flaskResponse = sendRequestToFlask(selectedItems);

        // 여기에 추천 로직을 구현하고 Flask 응답을 추가하여 ResponseDTO에 담기
        String response = "선택한 " + category + " : " + selectedItems.toString() + " 에 대한 추천 결과입니다. " + flaskResponse;

        ResponseDTO<String> responseDTO = ResponseDTO.<String>builder()
                .data(List.of(response))
                .build();

        return ResponseEntity.ok(responseDTO);
    }

    private String sendRequestToFlask(List<String> selectedItems) {
        try {
            String flaskUrl = "http://localhost:5000/survey";

            // 선택 항목을 JSON 형식으로 변환하여 전송
            ObjectMapper objectMapper = new ObjectMapper();
            String requestData = objectMapper.writeValueAsString(Map.of("selectedItems", selectedItems));

            // HTTP 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // HTTP 요청 엔티티 생성
            HttpEntity<String> requestEntity = new HttpEntity<>(requestData, headers);

            // RestTemplate을 사용하여 Flask 서버에 POST 요청 전송
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(flaskUrl, requestEntity, String.class);

            // Flask 서버로부터 받은 JSON 응답 데이터
            String responseBody = responseEntity.getBody();

            return responseBody;
        } catch (Exception e) {
            return e.toString();
        }
    }
}
