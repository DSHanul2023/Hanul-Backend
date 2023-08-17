package com.example.hanul.controller;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.dialogflow.v3.model.GoogleCloudDialogflowV2WebhookRequest;
import com.google.api.services.dialogflow.v3.model.GoogleCloudDialogflowV2WebhookResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

@RestController
public class DialogFlowRest {

    private static JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
    private String question;
    @GetMapping
    public String main(){
        return "ngrok 연결 테스트";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/dialogFlowWebHook")
    public ResponseEntity<?> dialogFlowWebHook(@RequestBody String requestStr, HttpServletRequest servletRequest) throws IOException {

        try {

            GoogleCloudDialogflowV2WebhookResponse response = new GoogleCloudDialogflowV2WebhookResponse(); // response 객체
            GoogleCloudDialogflowV2WebhookRequest request = jacksonFactory.createJsonParser(requestStr).parse(GoogleCloudDialogflowV2WebhookRequest.class); // request 객체에서 파싱

            question = request.getQueryResult().getQueryText();
            String res= handleFlask(question);
            Map<String,Object> params = request.getQueryResult().getParameters(); // 파라미터 받아서 map에다 저장

            if (params.size() > 0) {
                System.out.println(params);
                response.setFulfillmentText("다음과 같은 파라미터가 나왔습니다 " + params.toString());
            }
            else {
//                response.setFulfillmentText("Sorry you didn't send enough to process");
                response.setFulfillmentText(res.toString());
            }

            return new ResponseEntity<GoogleCloudDialogflowV2WebhookResponse>(response, HttpStatus.OK);
        }
        catch (Exception ex) {
            return new ResponseEntity<Object>(ex.getMessage(),HttpStatus.BAD_REQUEST); // 에러 발생 시 bad request 보내줌
        }
    }

    //    @PostMapping("/webhook")
//    public ResponseEntity<?> handleWebhook(@RequestBody Map<String, Object> request) throws Exception {
    public String handleFlask(String question) throws Exception {
        try{
            String flaskUrl = "http://localhost:5000/process";

            // 플라스크 서버에 전송할 JSON 데이터
            String requestData =  String.format("{\"question\": \"%s\"}", question);

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
        }catch (Exception e){
            return e.toString();
        }

    }

}
