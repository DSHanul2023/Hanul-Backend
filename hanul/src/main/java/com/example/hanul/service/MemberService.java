package com.example.hanul.service;

import com.example.hanul.dto.MemberDTO;
import com.example.hanul.model.MemberEntity;
import com.example.hanul.repository.MemberRepository;
import com.example.hanul.security.TokenProvider;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String secret;
    private final Key jwtSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long jwtExpirationMs = 86400000; // 24시간 (토큰 만료 시간)

//    public MemberService(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }
@Autowired
public MemberService(MemberRepository memberRepository, TokenProvider tokenProvider) {
    this.memberRepository = memberRepository;
    this.tokenProvider = tokenProvider;
}

    // 회원 생성
    public MemberEntity createMember(MemberDTO memberDTO) {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setName(memberDTO.getName());
        memberEntity.setEmail(memberDTO.getEmail());
        memberEntity.setPassword(memberDTO.getPassword());
        return memberRepository.save(memberEntity);
    }

    public MemberEntity getMemberById(String memberId) {
        return memberRepository.findById(memberId).orElse(null);
    }

    // 인증
//    public MemberEntity getByCredentials(String email, String password) {
//        return memberRepository.findByEmailAndPassword(email, password);
//    }
    public MemberEntity getByCredentials(String email, String password) {
        MemberEntity memberEntity = memberRepository.findByEmail(email);
        if (memberEntity != null && passwordEncoder.matches(password, memberEntity.getPassword())) {
            return memberEntity;
        }
        return null;
    }


    // 로그아웃
    public void logout() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

//    // 로그인 및 토큰 발급
//    public String loginAndGetToken(String email, String password, @Value("${jwt.secret}") String secret) {
//        MemberEntity authenticatedMember = memberRepository.findByEmailAndPassword(email, password);
//        if (authenticatedMember != null) {
//            String memberId = authenticatedMember.getId();
//            return generateJwtToken(memberId, email, secret);
//        }
//        return null;
//    }

    // 로그인 및 토큰 발급
    public MemberDTO loginAndGetMemberInfo(String email, String password, @Value("${jwt.secret}") String secret) {
        MemberEntity authenticatedMember = memberRepository.findByEmailAndPassword(email, password);
        if (authenticatedMember != null) {
            String memberId = authenticatedMember.getId();
            MemberDTO memberDTO = new MemberDTO();
            memberDTO.setId(memberId);
            memberDTO.setName(authenticatedMember.getName());
            memberDTO.setEmail(authenticatedMember.getEmail());
            memberDTO.setToken(generateJwtToken(memberId, email, secret));
            return memberDTO;
        }
        return null;
    }

    // 로그인
    public MemberDTO login(String email, String password, @Value("${jwt.secret}") String secret) {
        MemberEntity authenticatedMember = memberRepository.findByEmailAndPassword(email, password);
        if (authenticatedMember != null && passwordEncoder.matches(password, authenticatedMember.getPassword())) {
            String memberId = authenticatedMember.getId();
            MemberDTO memberDTO = new MemberDTO();
            memberDTO.setId(memberId);
            memberDTO.setName(authenticatedMember.getName());
            memberDTO.setEmail(authenticatedMember.getEmail());
            memberDTO.setToken(generateJwtToken(memberId, email, secret));
            return memberDTO;
        }
        return null;
    }

    private String generateJwtToken(String memberId, String email, String secret) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .claim("id", memberId)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public static String extractIdFromToken(String token, String secret) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
            return claims.get("id", String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract memberId from token");
        }
    }

    // 멤버 정보 조회
    public MemberDTO getMemberInfo(String token) {
        String memberId = tokenProvider.validateAndGetUserId(token);
        MemberEntity memberEntity = memberRepository.findById(memberId).orElse(null);

        if (memberEntity != null) {
            MemberDTO memberDTO = new MemberDTO();
            memberDTO.setId(memberId);
            memberDTO.setName(memberEntity.getName());
            memberDTO.setEmail(memberEntity.getEmail());
            return memberDTO;
        }

        return null;
    }

    // 멤버 정보 변경
    public boolean updateMemberInfo(MemberDTO memberDTO) {
        String memberId = tokenProvider.validateAndGetUserId(memberDTO.getToken());
        MemberEntity memberEntity = memberRepository.findById(memberId).orElse(null);
        if (memberEntity == null) {
            return false;
        }

        if (!memberDTO.getName().isEmpty()) {
            memberEntity.setName(memberDTO.getName());
        }

        if (!memberDTO.getPassword().isEmpty()) {
            String hashedPassword = passwordEncoder.encode(memberDTO.getPassword());
            memberEntity.setPassword(hashedPassword);
        }

        memberRepository.save(memberEntity);
        return true;
    }

    // 비밀번호 변경
    public boolean updatePassword(String memberId, String currentPassword, String newPassword) {
        MemberEntity memberEntity = memberRepository.findById(memberId).orElse(null);
        if (memberEntity == null) {
            return false;
        }

//        String hashedPassword = passwordEncoder.encode(newPassword);
        if (currentPassword.equals(memberEntity.getPassword())) {
            memberEntity.setPassword(newPassword);
            memberRepository.save(memberEntity);
            return true;
        }

        return false;
    }

    public boolean deleteMember(String memberId) {
        Optional<MemberEntity> memberOptional = memberRepository.findById(memberId);
        if (memberOptional.isPresent()) {
            MemberEntity member = memberOptional.get();
            memberRepository.delete(member);
            return true;
        }
        return false;
    }



}