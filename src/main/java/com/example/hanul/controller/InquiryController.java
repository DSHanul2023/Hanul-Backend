package com.example.hanul.controller;

import com.example.hanul.dto.InquiryDTO;
import com.example.hanul.dto.ResponseDTO;
import com.example.hanul.model.InquiryEntity;
import com.example.hanul.service.InquiryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("api/inquiry")
public class InquiryController {
    @Autowired
    private InquiryService service;
    @PostMapping
    public ResponseEntity<?> createInquiry(@AuthenticationPrincipal String memberId, @RequestBody InquiryDTO dto){
        try{
            InquiryEntity entity = InquiryDTO.toEntity(dto);
            entity.setId(null);
            entity.setMemberId(memberId);
            List<InquiryEntity> entities = service.create(entity);
            List<InquiryDTO> dtos = entities.stream().map(InquiryDTO::new).collect(Collectors.toList());
            ResponseDTO<InquiryDTO> response = ResponseDTO.<InquiryDTO>builder().data(dtos).build();
            return ResponseEntity.ok().body(response);
        } catch(Exception e){
            String error = e.getMessage();
            ResponseDTO<InquiryDTO> response = ResponseDTO.<InquiryDTO>builder().error(error).build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<?> retrieveInquiryList(@AuthenticationPrincipal String memberId){
        List<InquiryEntity> entities = service.retrieve(memberId);
        List<InquiryDTO> dtos = entities.stream().map(InquiryDTO::new).collect(Collectors.toList());
        ResponseDTO<InquiryDTO> response = ResponseDTO.<InquiryDTO>builder().data(dtos).build();
        return ResponseEntity.ok().body(response);
    }
    @PutMapping
    public ResponseEntity<?> updateTodo(@AuthenticationPrincipal String memberId,@RequestBody InquiryDTO dto){
        InquiryEntity entity = InquiryDTO.toEntity(dto);
        entity.setMemberId(memberId);
        List<InquiryEntity> entities = service.update(entity);
        List<InquiryDTO> dtos=entities.stream().map(InquiryDTO::new).collect(Collectors.toList());
        ResponseDTO<InquiryDTO> response = ResponseDTO.<InquiryDTO>builder().data(dtos).build();
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteTodo(@AuthenticationPrincipal String memberId,@RequestBody InquiryDTO dto){
        try{
            InquiryEntity entity = InquiryDTO.toEntity(dto);
            entity.setMemberId(memberId);
            List<InquiryEntity> entities = service.delete(entity);
            List<InquiryDTO> dtos = entities.stream().map(InquiryDTO::new).collect(Collectors.toList());
            ResponseDTO<InquiryDTO> response = ResponseDTO.<InquiryDTO>builder().data(dtos).build();
            return ResponseEntity.ok().body(response);
        }catch(Exception e){
            String error = e.getMessage();
            ResponseDTO<InquiryDTO> response = ResponseDTO.<InquiryDTO>builder().error(error).build();
            return ResponseEntity.badRequest().body(response);
        }
    }
    @GetMapping("/{query}")
    public ResponseEntity<?> searchInquiries(@AuthenticationPrincipal String memberId, @PathVariable("query") String query) {
        try {
            List<InquiryEntity> entities = service.searchInquiries(memberId, query);
            List<InquiryDTO> dtos = entities.stream().map(InquiryDTO::new).collect(Collectors.toList());
            ResponseDTO<InquiryDTO> response = ResponseDTO.<InquiryDTO>builder().data(dtos).build();
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO<InquiryDTO> response = ResponseDTO.<InquiryDTO>builder().error(error).build();
            return ResponseEntity.badRequest().body(response);
        }
    }
}