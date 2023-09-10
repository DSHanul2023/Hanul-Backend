package com.example.hanul.service;

import com.example.hanul.dto.MemberDTO;
import com.example.hanul.model.ItemEntity;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Key;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;
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
        // 이메일 중복 확인
        String email = memberDTO.getEmail();
        MemberEntity existingMember = memberRepository.findByEmail(email);
        if (existingMember != null) {
            // 이미 존재하는 이메일이면 null 반환
            return null;
        }

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

    // 로그인 및 토큰 발급
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
            memberDTO.setProfilePictureName(authenticatedMember.getProfilePictureName());
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
            memberDTO.setProfilePictureName(authenticatedMember.getProfilePictureName());
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
            memberDTO.setPassword(memberEntity.getPassword());
            memberDTO.setProfilePictureName(memberEntity.getProfilePictureName());
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

    @Value("${app.upload-dir}") // Inject the app.upload-dir property
    private String uploadDir;

    public void updateProfilePicture(String memberId, MultipartFile file) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Could not find member"));
        try {
//            String fileName = "profile_picture_" + memberId + ".jpg";
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "profile_picture_" + memberId + "_" + timestamp + ".jpg";

//            Path filePath = Paths.get(uploadDir, fileName);
            // Generate folder path based on the current date
            String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String folderPath = Paths.get(uploadDir, dateFolder).toString();

            // Create the folder if it doesn't exist
            Files.createDirectories(Paths.get(folderPath));

            // 대상 경로 생성
//            String destinationPath = uploadDir + "/" + fileName;
//            Path destination = Paths.get(destinationPath);
            String destinationPath = Paths.get(folderPath, fileName).toString();
            Path destination = Paths.get(destinationPath);

            // 업로드된 파일을 대상으로 복사
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            // Update the member's profile picture path
//            member.setProfilePictureName(fileName);
            member.setProfilePictureName(dateFolder + "/" + fileName);
            memberRepository.save(member);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while updating your profile picture.", e);
        }
    }

    public List<ItemEntity> getBookmarkedItems(MemberEntity member) {
        return member.getBookmarkedItems();
    }
}