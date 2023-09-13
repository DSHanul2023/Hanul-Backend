package com.example.hanul.controller;

import com.example.hanul.dto.MemberDTO;
import com.example.hanul.model.ItemEntity;
import com.example.hanul.model.MemberEntity;
import com.example.hanul.service.ChatService;
import com.example.hanul.service.ItemService;
import com.example.hanul.service.MemberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;
    private final ChatService chatService;

    public MemberController(MemberService memberService, ChatService chatService) {
        this.memberService = memberService;
        this.chatService = chatService;
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

//    @PostMapping("/login")
//    public ResponseEntity<Object> login(@RequestBody MemberDTO memberDTO, @Value("${jwt.secret}") String secret) {
//        String token = memberService.loginAndGetToken(memberDTO.getEmail(), memberDTO.getPassword(), secret);
//        if (token != null) {
//            String memberId = memberService.extractIdFromToken(token,secret); // 토큰에서 memberId 추출
//            memberDTO.setId(memberId); // InquiryDTO에 memberId 설정
//
//            String responseJson = "{\"message\": \"Login successful\", \"token\": \"" + token + "\", \"memberId\": \"" + memberId + "\"}";
//            return ResponseEntity.ok(responseJson);
//        } else {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\": \"Invalid credentials\"}");
//        }
//    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody MemberDTO memberDTO, @Value("${jwt.secret}") String secret) {
        MemberDTO loggedInMember = memberService.loginAndGetMemberInfo(memberDTO.getEmail(), memberDTO.getPassword(), secret);
        if (loggedInMember != null) {
            return ResponseEntity.ok(loggedInMember);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\": \"Invalid credentials\"}");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout() {
        memberService.logout();
        return ResponseEntity.ok("{\"message\": \"Logout successful\"}");
    }

    @PostMapping("/{memberid}/logout")
    public ResponseEntity<Object> logoutdata(@PathVariable("memberid") String memberId) {
        // memberId를 사용하여 해당 멤버의 채팅 데이터를 삭제
        boolean chatDataDeleted = chatService.deleteChatDataForUser(memberId);

        if (chatDataDeleted) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            HttpSession session = request.getSession(false);

            if (session != null) {
                session.invalidate();
            }

            return ResponseEntity.ok("{\"message\": \"Logout successful\"}");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"message\": \"Failed to delete chat data\"}");
        }
    }


    @GetMapping("/getMemberInfo")
    public ResponseEntity<MemberDTO> getMemberInfo(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7); // Remove "Bearer " prefix
            MemberDTO memberDTO = memberService.getMemberInfo(token);

            if (memberDTO != null) {
                return ResponseEntity.ok(memberDTO);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }

    @PutMapping("/updateMemberInfo")
    @PreAuthorize("isAuthenticated() and #memberDTO.id == authentication.principal.id")
    public ResponseEntity<MemberDTO> updateMemberInfo(@RequestBody MemberDTO memberDTO) {
        boolean success = memberService.updateMemberInfo(memberDTO);
        if (success) {
            MemberDTO updatedMember = memberService.getMemberInfo(memberDTO.getToken());
            if (updatedMember != null) {
                return ResponseEntity.ok(updatedMember);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PutMapping("/updatePassword")
    @PreAuthorize("isAuthenticated() and #memberDTO.id == authentication.principal.id")
    public ResponseEntity<Object> updatePassword(@RequestBody MemberDTO memberDTO) {
        boolean success = memberService.updatePassword(memberDTO.getId(), memberDTO.getPassword(), memberDTO.getNewPassword());
        if (success) {
            return ResponseEntity.ok("{\"message\": \"Password updated successfully\"}");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"Current password does not match\"}");
        }
    }

    @DeleteMapping("/deleteMember")
    @PreAuthorize("isAuthenticated() and #memberDTO.id == authentication.principal.id")
    public ResponseEntity<String> deleteMember(@RequestBody MemberDTO memberDTO) {
        boolean success = memberService.deleteMember(memberDTO.getId());
        if (success) {
            return ResponseEntity.ok("Member deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member not found");
        }
    }

    @PostMapping("/uploadProfilePicture/{memberId}")
    public ResponseEntity<String> uploadProfilePicture(
            @PathVariable String memberId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            memberService.updateProfilePicture(memberId, file);
            return ResponseEntity.ok("프로필 사진이 성공적으로 업로드되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("프로필 사진 업로드 중에 오류가 발생했습니다.");
        }
    }

    @GetMapping("/{memberId}/bookmarked-items")
    public ResponseEntity<List<ItemEntity>> getBookmarkedItems(@PathVariable String memberId) {
        MemberEntity member = memberService.getMemberById(memberId);
        if (member != null) {
            List<ItemEntity> bookmarkedItems = memberService.getBookmarkedItems(member);
            return new ResponseEntity<>(bookmarkedItems, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 비밀번호 잊은 경우
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        memberService.forgotPassword(request.getEmail());
        return ResponseEntity.ok("임시 비밀번호가 이메일로 전송되었습니다.");
    }

    static class ForgotPasswordRequest {
        private String email;
        public String getEmail() {
            return email;
        }
        public void setEmail(String email) {
            this.email = email;
        }
    }
}