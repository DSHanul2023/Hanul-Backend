package com.example.hanul.controller;


import com.example.hanul.dto.ChatDTO;
import com.example.hanul.model.ChatEntity;
import com.example.hanul.service.ChatService;

import com.example.hanul.service.DialogflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/chats")
public class ChatController {
    private final ChatService chatService;
    private final DialogflowService dialogflowService; // Dialogflow 서비스 추가

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

                String responseMessage = "[사용자 채팅 저장 및 응답]\n"
                        + "입력: " + userMessage + "\n"
                        + "응답: " + dialogflowResponse;

                return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
            } catch (IOException e) {
                e.printStackTrace();
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