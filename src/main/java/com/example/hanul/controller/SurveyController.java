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
    public ResponseEntity<RecommandMovieDTO> processSurvey(@RequestBody SurveyDTO surveyDTO) {
        String category = surveyDTO.getCategory();
        List<String> selectedItems = surveyDTO.getSelectedItems();

        ResponseEntity<Map> flaskResponse = sendRequestToFlask(selectedItems);
        Map<String, Object> responseBody = flaskResponse.getBody();

        List<List<String>> recommendedMoviesData = (List<List<String>>) responseBody.get("recommended_movies");

        // Convert each string list to a list of maps
        List<List<Map<String, Object>>> recommendedMovies = recommendedMoviesData.stream()
                .map(movieList -> movieList.stream()
                        .map(movieId -> {
                            Map<String, Object> movieMap = new HashMap<>();
                            movieMap.put("movieId", movieId);
                            return movieMap;
                        })
                        .collect(Collectors.toList())
                )
                .collect(Collectors.toList());

        String response = category + " : " + selectedItems.toString() + " 에 대한 추천 결과입니다. ";

        RecommandMovieDTO recommandMovieDTO = RecommandMovieDTO.builder()
                .recommendedMovies(recommendedMovies)
                .response(response)
                .build();

        return ResponseEntity.ok(recommandMovieDTO);
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
