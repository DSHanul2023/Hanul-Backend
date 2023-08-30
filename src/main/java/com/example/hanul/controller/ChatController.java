package com.example.hanul.controller;

import com.example.hanul.dto.ChatDTO;
import com.example.hanul.model.ChatEntity;
import com.example.hanul.service.ChatService;
import com.example.hanul.service.DialogflowService;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.web.client.RestTemplate;
@RestController
@RequestMapping("/chats")
public class ChatController {
    private final ChatService chatService;
    private final DialogflowService dialogflowService;
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    public ChatController(ChatService chatService, DialogflowService dialogflowService) {
        this.chatService = chatService;
        this.dialogflowService = dialogflowService;
    }

    @GetMapping
    public String main() {
        return "chat 확인 test";
    }

    @PostMapping("/chatdialogflow")
    public ResponseEntity<ChatEntity> handleChatMessage(@RequestBody ChatDTO chatDTO) {
        long startTime = System.currentTimeMillis(); // 시작 시간 기록

        ChatEntity savedChat = chatService.saveChatMessage(chatDTO.getMemberId(), chatDTO.getMessage());

        long dbSaveEndTime = System.currentTimeMillis(); // DB 저장 종료 시간 기록

        if (savedChat != null) {
            try {
                long dialogflowStartTime = System.currentTimeMillis(); // Dialogflow 통신 시작 시간 기록
                String userMessage = chatDTO.getMessage();
                DetectIntentResponse dialogflowResponse = dialogflowService.sendToDialogflow(userMessage);
                String fullfillmentText = dialogflowResponse.getQueryResult().getFulfillmentText();
                String action = dialogflowResponse.getQueryResult().getAction();

                long dialogflowEndTime = System.currentTimeMillis(); // Dialogflow 통신 종료 시간 기록

                logger.info("[사용자 채팅 저장 및 응답] 입력: {} 응답: {} 액션: {}", userMessage, dialogflowResponse, action);

                String botResponseContent;

                boolean recommend_status = false;
                if(userMessage.contains("추천")){
                    recommend_status = true;
                    botResponseContent = dialogflowResponse + "\n 그럴때 이런 영화는 어때요?";
                }
                else if ("listen.support".equals(action)) {
                    botResponseContent = handleFlask(userMessage); // 플라스크 응답 사용
                    long flaskEndTime = System.currentTimeMillis(); // Flask 요청 종료 시간 기록

                    logger.info("Flask 요청 시간: {}ms", (flaskEndTime - flaskStartTime));
                } else {
                    botResponseContent = dialogflowResponse;
                    if ("recommend".equals(action)){
                        recommend_status = true;
                    }
                }

                return ResponseEntity.status(HttpStatus.OK).body(new ChatEntity(null, botResponseContent, null, LocalDateTime.now(),recommend_status));
            } catch (IOException e) {
                logger.error("Dialogflow 통신 실패", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            } catch (Exception e) {
                logger.error("Error handling Flask", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{memberId}/history")
    public ResponseEntity<List<ChatEntity>> getChatHistory(@PathVariable String memberId) {
        List<ChatEntity> chatHistory = chatService.getChatHistory(memberId);
        if (chatHistory != null) {
            return ResponseEntity.ok(chatHistory);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
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
