package com.example.hanul.service;

import com.example.hanul.dto.ChatDTO;
import com.example.hanul.model.ChatEntity;
import com.example.hanul.model.MemberEntity;
import com.example.hanul.repository.ChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

//회원별로 채팅 내용을 저장하고 조회
@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final MemberService memberService;

    @Autowired
    public ChatService(ChatRepository chatRepository, MemberService memberService) {
        this.chatRepository = chatRepository;
        this.memberService = memberService;
    }

    public ChatEntity saveChatMessage(String memberId, String message) {
        MemberEntity member = memberService.getMemberById(memberId);
        if (member != null) {
            ChatEntity chatEntity = ChatEntity.builder()
                    .message(message)
                    .member(member)
                    .timestamp(LocalDateTime.now())
                    .build();
            return chatRepository.save(chatEntity);
        }
        return null;
    }

    public List<ChatEntity> getChatHistory(String memberId) {
        MemberEntity member = memberService.getMemberById(memberId);
        if (member != null) {
            return chatRepository.findAllByMemberOrderByTimestampDesc(member);
        }
        return null;
    }
}
