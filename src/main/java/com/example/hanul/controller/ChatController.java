package com.example.hanul.controller;

import com.example.hanul.dto.ChatDTO;
import com.example.hanul.model.ChatEntity;
import com.example.hanul.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chats")
public class ChatController {
    private final ChatService chatService;

    @GetMapping
    public String main(){
        return "chat 확인 test";
    }

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }
    
    //사용자의 채팅 내용을 저장
    @PostMapping("/save")
    public ResponseEntity<String> saveChatMessage(@RequestBody ChatDTO chatDTO) {
        ChatEntity savedChat = chatService.saveChatMessage(chatDTO.getMemberId(), chatDTO.getMessage());
        if (savedChat != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Chat message saved successfully");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save chat message");
        }
    }
    //사용자의 채팅 내용 조회
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
