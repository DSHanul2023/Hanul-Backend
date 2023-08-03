package com.example.hanul.controller;

import com.example.hanul.dto.MemberDTO;
import com.example.hanul.model.MemberEntity;
import com.example.hanul.service.MemberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerMember(@RequestBody MemberDTO memberDTO) {
        MemberEntity createdMember = memberService.createMember(memberDTO);
        if (createdMember != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Member registered successfully");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to register member");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody MemberDTO memberDTO, @Value("${jwt.secret}") String secret) {
        String token = memberService.loginAndGetToken(memberDTO.getEmail(), memberDTO.getPassword(), secret);
        if (token != null) {
            String memberId = memberService.extractIdFromToken(token,secret); // 토큰에서 memberId 추출
            memberDTO.setId(memberId); // InquiryDTO에 memberId 설정

            return ResponseEntity.ok("{\"message\": \"Login successful\", \"token\": \"" + token + "\"}");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\": \"Invalid credentials\"}");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout() {
        memberService.logout();
        return ResponseEntity.ok("{\"message\": \"Logout successful\"}");
    }
}