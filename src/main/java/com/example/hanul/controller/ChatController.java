package com.example.hanul.controller;


import com.example.hanul.dto.ChatDTO;
import com.example.hanul.model.ChatEntity;
import com.example.hanul.service.ChatService;

import com.example.hanul.service.DialogflowService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/chats")
public class ChatController {
    private final ChatService chatService;
    private final DialogflowService dialogflowService; // Dialogflow 서비스 추가

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class); // Logger 추가


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
    public ResponseEntity<String> handleChatMessage(@RequestBody ChatDTO chatDTO) {
        ChatEntity savedChat = chatService.saveChatMessage(chatDTO.getMemberId(), chatDTO.getMessage());

        if (savedChat != null) {
            try {
                // Dialogflow에 사용자 입력 전달
                String userMessage = chatDTO.getMessage();
                String dialogflowResponse = dialogflowService.sendToDialogflow(userMessage);

                // 로그로 출력
                logger.info("[사용자 채팅 저장 및 응답] 입력: {} 응답: {}", userMessage, dialogflowResponse);

                return ResponseEntity.status(HttpStatus.OK).body(dialogflowResponse); // 응답은 dialogflowResponse로만 설정
            } catch (IOException e) {
                logger.error("Dialogflow 통신 실패", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to communicate with Dialogflow");
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save chat message");
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