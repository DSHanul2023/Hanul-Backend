package com.example.hanul.controller;

import com.example.hanul.dto.ChatDTO;
import com.example.hanul.model.ChatEntity;
import com.example.hanul.service.ChatService;
import com.example.hanul.service.DialogflowService;
import com.example.hanul.service.FlaskService;
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

    private final FlaskService flaskService;
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    public ChatController(ChatService chatService, DialogflowService dialogflowService, FlaskService flaskService) {
        this.chatService = chatService;
        this.dialogflowService = dialogflowService;
        this.flaskService = flaskService;
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
                logger.info("dialogflow 통신: {}", (dialogflowEndTime-dialogflowStartTime));

                logger.info("[사용자 채팅 저장 및 응답] 입력: {} 응답: {} 액션: {}", userMessage, fullfillmentText, action);

                String botResponseContent;

                boolean recommend_status = false;
                if(userMessage.contains("추천")){
                    recommend_status = true;
                    // botResponseContent = fullfillmentText + "\n 그럴때 이런 영화는 어때요?";
                    String emotion = flaskService.emotionWithFlask(chatDTO.getMemberId());
                    DetectIntentResponse recommendResponse = dialogflowService.sendRecommendToDialogflow(userMessage, emotion);
                    botResponseContent = recommendResponse.getQueryResult().getFulfillmentText();
                }
                else if ("listen.support".equals(action)) {
                    botResponseContent = flaskService.chatWithFlask(userMessage); // flaskService를 이용한 응답
                } else {
                    botResponseContent = fullfillmentText;
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
}
