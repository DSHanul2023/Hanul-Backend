package com.example.hanul.service;

import com.example.hanul.dto.ProviderDTO;
import com.example.hanul.dto.ProviderInfoDTO;
import com.example.hanul.model.ItemEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

@Service
public class FlaskService {
    private final RestTemplate restTemplate;
    private final String flaskBaseUrl = "http://localhost:5000";
    private final ItemService itemService;

    @Autowired
    public FlaskService(RestTemplate restTemplate, ItemService itemService) {
        this.restTemplate = restTemplate;
        this.itemService = itemService;
    }

    // 챗봇 생성 모델 연결
    public String chatWithFlask(String question) {
        try {
            String chatUrl = flaskBaseUrl + "/process";

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


    // 추천 모델 연결
    // Flask 서버에 GET 요청을 보내고 JSON 응답을 받는 함수
    private String sendGetRequestToFlask(String memberId) {
        try {
            String recommendUrl = flaskBaseUrl + "/recommend2?memberId=" + memberId;

            // RestTemplate을 사용하여 플라스크 서버에 GET 요청 전송
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(recommendUrl, String.class);

            // 플라스크 서버로부터 받은 JSON 응답 데이터
            String responseBody = responseEntity.getBody();

            return responseBody;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String emotionWithFlask2(String memberId) {
        try {
            String emotionUrl = flaskBaseUrl + "/emotion2?memberId=" + memberId;
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(emotionUrl, String.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // Flask에서 받은 JSON 응답을 ItemEntity 객체 리스트로 변환하는 함수
    public List<ItemEntity> RecommendWithFlask(String memberId) {
        String jsonResponse = sendGetRequestToFlask(memberId);

        if (jsonResponse == null) {
            // 오류 처리
            return new ArrayList<>();
        }

        List<ItemEntity> items = new ArrayList<>();

        try {
            // JSON 문자열을 파싱
            JSONArray jsonArray = new JSONArray(jsonResponse);

            // 각 JSON 객체에서 id를 추출하여 ItemEntity를 조회하고 리스트에 추가
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String itemId = jsonObject.getString("id");
                ItemEntity itemEntity = itemService.getItemById(itemId);

                if (itemEntity != null) {
                    items.add(itemEntity);
                }
            }
        } catch (Exception e) {
            // 예외 처리
            e.printStackTrace();
        }

        return items;
    }

    public ProviderDTO ProvidersWithFlask(String itemId){
        try {
            String providerUrl = flaskBaseUrl + "/providers?item_id=" + itemId;

            // RestTemplate을 사용하여 플라스크 서버에 GET 요청 전송
            ResponseEntity<ProviderDTO> responseEntity = restTemplate.getForEntity(providerUrl, ProviderDTO.class);

            // 플라스크 서버로부터 받은 JSON 응답 데이터
            ProviderDTO responseBody = responseEntity.getBody();

            return responseBody;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String emotionWithFlask(String question) {
        try {
            String chatUrl = flaskBaseUrl + "/emotion";

            // Flask 서버에 보낼 JSON 데이터 생성
            String requestData = String.format("{\"sentence\": \"%s\"}", question);

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
