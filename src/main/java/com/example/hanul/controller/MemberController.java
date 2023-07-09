package com.example.hanul.controller;

import com.example.hanul.dto.MemberDTO;
import com.example.hanul.model.MemberEntity;
import com.example.hanul.service.MemberService;
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
    public ResponseEntity<Object> login(@RequestBody MemberDTO memberDTO) {
        String token = memberService.loginAndGetToken(memberDTO.getEmail(), memberDTO.getPassword());
        if (token != null) {
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
