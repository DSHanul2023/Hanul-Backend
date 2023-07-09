package com.example.hanul.service;

import com.example.hanul.dto.MemberDTO;
import com.example.hanul.model.MemberEntity;
import com.example.hanul.repository.MemberRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.Key;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final Key jwtSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long jwtExpirationMs = 86400000; // 24시간 (토큰 만료 시간)

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 회원 생성
    public MemberEntity createMember(MemberDTO memberDTO) {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setName(memberDTO.getName());
        memberEntity.setEmail(memberDTO.getEmail());
        memberEntity.setPassword(memberDTO.getPassword());
        return memberRepository.save(memberEntity);
    }

    // 인증
    public MemberEntity getByCredentials(String email, String password) {
        return memberRepository.findByEmailAndPassword(email, password);
    }

    // 로그아웃
    public void logout() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    // 로그인 및 토큰 발급
    public String loginAndGetToken(String email, String password) {
        MemberEntity authenticatedMember = memberRepository.findByEmailAndPassword(email, password);
        if (authenticatedMember != null) {
            String memberId = authenticatedMember.getId();
            return generateJwtToken(memberId, email);
        }
        return null;
    }

    private String generateJwtToken(String memberId, String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .claim("id", memberId)
                .signWith(jwtSecretKey)
                .compact();
    }
}
