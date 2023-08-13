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
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;

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

                logger.info("[사용자 채팅 저장 및 응답] 입력: {} 응답: {}", userMessage, dialogflowResponse);

                String botResponseContent = dialogflowResponse; // 수정: dialogflowResponse를 직접 사용
                return ResponseEntity.status(HttpStatus.OK).body(new ChatEntity(null, botResponseContent, null, LocalDateTime.now()));
            } catch (IOException e) {
                logger.error("Dialogflow 통신 실패", e);
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