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
    public ResponseEntity<MemberResponseDTO> registerMember(@RequestBody MemberDTO memberDTO) {
        MemberResponseDTO createdMember = memberService.createMember(memberDTO);
        if (createdMember != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMember);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody MemberDTO memberDTO) {
        String token = memberService.loginAndGetToken(memberDTO.getEmail(), memberDTO.getPassword());
        if (token != null) {
            MemberEntity authenticatedMember = memberService.getByCredentials(memberDTO.getEmail(), memberDTO.getPassword());
            MemberResponseDTO memberResponseDTO = memberService.convertToMemberResponseDTO(authenticatedMember);
            return ResponseEntity.ok(memberResponseDTO);
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
