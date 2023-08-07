package com.example.hanul.controller;

import com.example.hanul.dto.BoardDTO;
import com.example.hanul.dto.InquiryDTO;
import com.example.hanul.dto.ResponseDTO;
import com.example.hanul.model.BoardEntity;
import com.example.hanul.model.InquiryEntity;
import com.example.hanul.model.MemberEntity;
import com.example.hanul.repository.MemberRepository;
import com.example.hanul.service.BoardService;
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
@RequestMapping("board")
public class BoardController {

    @Autowired
    private BoardService service;
    @Autowired
    private MemberRepository memberRepository;

    @GetMapping
    public ResponseEntity<?> retrieve(){
        List<BoardEntity> entities = service.retrieve();
        List<BoardDTO> dtos = entities.stream().map(BoardDTO::new).collect(Collectors.toList());
        ResponseDTO<BoardDTO> response = ResponseDTO.<BoardDTO>builder().data(dtos).build();

        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<?> create(@AuthenticationPrincipal String userId, @RequestBody BoardDTO dto) {
        Optional<MemberEntity> member = memberRepository.findById(userId);
        if (member == null) {
            throw new RuntimeException("User not found.");
        }
        try {
            BoardEntity entity = BoardDTO.toEntity(dto);
            entity.setAuthor(member.get().getName());
            entity.setMember_id(member.get().getId());
            entity.setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            List<BoardEntity> entities = service.create(entity);
            List<BoardDTO> dtos = entities.stream().map(BoardDTO::new).collect(Collectors.toList());
            ResponseDTO<BoardDTO> response = ResponseDTO.<BoardDTO>builder().data(dtos).build();

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO<BoardDTO> response = ResponseDTO.<BoardDTO>builder().error(error).build();

            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@AuthenticationPrincipal String userId, @RequestBody BoardDTO dto) {
        try {
            BoardEntity entity = BoardDTO.toEntity(dto);

            List<BoardEntity> entities = service.delete(entity);
            List<BoardDTO> dtos = entities.stream().map(BoardDTO::new).collect(Collectors.toList());
            ResponseDTO<BoardDTO> response = ResponseDTO.<BoardDTO>builder().data(dtos).build();

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO<BoardDTO> response = ResponseDTO.<BoardDTO>builder().error(error).build();

            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping
    public ResponseEntity<?> update(@AuthenticationPrincipal String userId, @RequestBody BoardDTO dto) {
        try {
            BoardEntity entity = BoardDTO.toEntity(dto);
            entity.setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            List<BoardEntity> entities = service.update(entity);
            List<BoardDTO> dtos = entities.stream().map(BoardDTO::new).collect(Collectors.toList());
            ResponseDTO<BoardDTO> response = ResponseDTO.<BoardDTO>builder().data(dtos).build();

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO<BoardDTO> response = ResponseDTO.<BoardDTO>builder().error(error).build();

            return ResponseEntity.badRequest().body(response);
        }
    }
    @GetMapping("/{query}")
    public ResponseEntity<?> searchBoard(@PathVariable("query") String query) {
        try {
            List<BoardEntity> entities = service.searchBoard(query);
            List<BoardDTO> dtos = entities.stream().map(BoardDTO::new).collect(Collectors.toList());
            ResponseDTO<BoardDTO> response = ResponseDTO.<BoardDTO>builder().data(dtos).build();
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO<BoardDTO> response = ResponseDTO.<BoardDTO>builder().error(error).build();
            return ResponseEntity.badRequest().body(response);
        }
    }
}
