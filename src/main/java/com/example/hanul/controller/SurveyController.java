package com.example.hanul.controller;

import com.example.hanul.dto.RecommandMovieDTO;
import com.example.hanul.dto.SurveyDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/survey")
@CrossOrigin(origins = "http://localhost:3000")
public class SurveyController {

    @PostMapping
    public ResponseEntity<Map<String, Object>> processSurvey(@RequestBody SurveyDTO surveyDTO) {
        List<String> selectedItems = surveyDTO.getSelectedItems();

        ResponseEntity<Map> flaskResponse = sendRequestToFlask(selectedItems);
        Map<String, Object> responseBody = flaskResponse.getBody();

        List<List<String>> recommendedMoviesData = (List<List<String>>) responseBody.get("recommended_movies");

        // Convert each string list to a list of maps
        List<Map<String, Object>> recommendedMovies = recommendedMoviesData.stream()
                .map(movieList -> {
                    Map<String, Object> movieMap = new HashMap<>();
                    movieMap.put("movieId", movieList.get(0));
                    movieMap.put("genreName", movieList.get(1));
                    movieMap.put("itemDetail", movieList.get(2));
                    movieMap.put("itemNm", movieList.get(3));
                    movieMap.put("posterUrl", movieList.get(4));
                    // You can add more key-value pairs if needed
                    return movieMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("recommended_movies", recommendedMovies);

        // 사용자의 선택에 따른 메시지 생성
        String category = surveyDTO.getCategory();
        String selectedItemsString = String.join(", ", selectedItems);
        String responseMessage = category + " : " + selectedItemsString + " 에 대한 추천 결과입니다.";
        responseMap.put("response", responseMessage);

        return ResponseEntity.ok(responseMap);
    }

    private ResponseEntity<Map> sendRequestToFlask(List<String> selectedItems) {
        try {
            String flaskUrl = "http://localhost:5000/survey";

            ObjectMapper objectMapper = new ObjectMapper();
            String requestData = objectMapper.writeValueAsString(Map.of("selectedItems", selectedItems));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(requestData, headers);

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
