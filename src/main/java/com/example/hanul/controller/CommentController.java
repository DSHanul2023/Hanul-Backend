
package com.example.hanul.controller;

import com.example.hanul.dto.BoardDTO;
import com.example.hanul.dto.CommentDTO;
import com.example.hanul.dto.ResponseDTO;
import com.example.hanul.model.CommentEntity;
import com.example.hanul.model.MemberEntity;
import com.example.hanul.repository.CommentRepository;
import com.example.hanul.repository.MemberRepository;
import com.example.hanul.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/comments")
public class CommentController {
    private final CommentRepository commentRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CommentService service;
    @Autowired
    public CommentController(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<?> getCommentsForBoard(@AuthenticationPrincipal String userId,@PathVariable String boardId) {
        try {
            List<CommentEntity> comments = commentRepository.findByBoardId(boardId);
//            List<CommentDTO> commentDTOs = comments.stream()
//                    .map(CommentDTO::fromEntity)
//                    .collect(Collectors.toList());
            List<CommentDTO> commentDTOs = comments.stream().map(commentEntity -> {
                CommentDTO dto = new CommentDTO(commentEntity);
                // 현재 로그인된 사용자의 memberid와 글 작성자의 memberid가 일치하는지 확인
                if (userId.equals(commentEntity.getMemberId())) {
                    dto.setCanEdit(true);
                }
                return dto;
            }).collect(Collectors.toList());
            ResponseDTO<CommentDTO> response = ResponseDTO.<CommentDTO>builder().data(commentDTOs).build();
            return ResponseEntity.ok().body(response);
        }catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO<BoardDTO> response = ResponseDTO.<BoardDTO>builder().error(error).build();

            return ResponseEntity.badRequest().body(response);
        }

    }

    @PostMapping
    public ResponseEntity<?> addComment(@AuthenticationPrincipal String userId, @RequestBody CommentDTO commentDTO) {
        Optional<MemberEntity> member = memberRepository.findById(userId);
        if (member == null) {
            throw new RuntimeException("User not found.");
        }
        try {
            CommentEntity entity = CommentDTO.toEntity(commentDTO);
            entity.setAuthor(member.get().getName());
            entity.setMemberId(userId);
            entity.setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            List<CommentEntity> entities = service.create(entity);
            List<CommentDTO> dtos = entities.stream().map(CommentDTO::new).collect(Collectors.toList());
            ResponseDTO<CommentDTO> response = ResponseDTO.<CommentDTO>builder().data(dtos).build();

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO<CommentDTO> response = ResponseDTO.<CommentDTO>builder().error(error).build();

            return ResponseEntity.badRequest().body(response);
        }
    }
    @PutMapping
    public ResponseEntity<?> update(@AuthenticationPrincipal String userId, @RequestBody CommentDTO dto) {
        try {
            CommentEntity entity = CommentDTO.toEntity(dto);
            entity.setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            List<CommentEntity> entities = service.update(entity);
            List<CommentDTO> dtos = entities.stream().map(CommentDTO::new).collect(Collectors.toList());
            ResponseDTO<CommentDTO> response = ResponseDTO.<CommentDTO>builder().data(dtos).build();

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO<CommentDTO> response = ResponseDTO.<CommentDTO>builder().error(error).build();

            return ResponseEntity.badRequest().body(response);
        }
    }
    @DeleteMapping
    public ResponseEntity<?> delete(@AuthenticationPrincipal String userId, @RequestBody CommentDTO dto) {
        try {
            CommentEntity entity = CommentDTO.toEntity(dto);

            List<CommentEntity> entities = service.delete(entity);
            List<CommentDTO> dtos = entities.stream().map(CommentDTO::new).collect(Collectors.toList());
            ResponseDTO<CommentDTO> response = ResponseDTO.<CommentDTO>builder().data(dtos).build();

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO<CommentDTO> response = ResponseDTO.<CommentDTO>builder().error(error).build();

            return ResponseEntity.badRequest().body(response);
        }
    }
}

