package com.example.hanul.controller;

import com.example.hanul.dto.RecommandMovieDTO;
import com.example.hanul.dto.ResponseDTO;
import com.example.hanul.dto.SurveyDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("/survey")
@CrossOrigin(origins = "http://localhost:3000")
public class SurveyController {

    @PostMapping
    public ResponseEntity<RecommandMovieDTO> processSurvey(@RequestBody SurveyDTO surveyDTO) {
        // 클라이언트로부터 받은 정보를 이용하여 추천 로직 수행
        String category = surveyDTO.getCategory();
        List<String> selectedItems = surveyDTO.getSelectedItems();

        // Flask 서버에 선택 항목 전달하고 응답 받아오기
        ResponseEntity<Map> flaskResponse = sendRequestToFlask(selectedItems);
        // Flask 서버로부터 받은 JSON 응답 데이터
        Map<String, Object> responseBody = flaskResponse.getBody();

        // 추천된 영화 목록 가져오기
        List<Map<String, Object>> recommendedMovies = (List<Map<String, Object>>) responseBody.get("recommended_movies");;

        // 추천 결과 문장 생성
        String response = category + " : " + selectedItems.toString() + " 에 대한 추천 결과입니다. ";

        // 여기에 response를 RecommandMovieDTO에 담기
        RecommandMovieDTO recommandMovieDTO = RecommandMovieDTO.builder()
                .recommendedMovies(recommendedMovies)
                .response(response)
                .build();

        return ResponseEntity.ok(recommandMovieDTO);
    }


    private ResponseEntity<Map> sendRequestToFlask(List<String> selectedItems) {
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
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    flaskUrl, HttpMethod.POST, requestEntity, Map.class
            );

            return responseEntity;

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.toString()));
        }
    }
}
