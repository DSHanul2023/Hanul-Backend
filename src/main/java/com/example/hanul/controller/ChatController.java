package com.example.hanul.controller;

import com.example.hanul.dto.ChatDTO;
import com.example.hanul.model.ChatEntity;
import com.example.hanul.service.ChatService;
import com.example.hanul.service.DialogflowService;
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
        ChatEntity savedChat = chatService.saveChatMessage(chatDTO.getMemberId(), chatDTO.getMessage());

        if (savedChat != null) {
            try {
                String userMessage = chatDTO.getMessage();
                String dialogflowResponse = dialogflowService.sendToDialogflow(userMessage);
                String action = dialogflowService.getActionFromResponse(userMessage);

                logger.info("[사용자 채팅 저장 및 응답] 입력: {} 응답: {} 액션: {}", userMessage, dialogflowResponse, action);

                String botResponseContent;

                if ("listen.support".equals(action)) {
                    botResponseContent = handleFlask(userMessage); // 플라스크 응답 사용
                } else {
                    botResponseContent = dialogflowResponse;
                }

                return ResponseEntity.status(HttpStatus.OK).body(new ChatEntity(null, botResponseContent, null, LocalDateTime.now()));
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
