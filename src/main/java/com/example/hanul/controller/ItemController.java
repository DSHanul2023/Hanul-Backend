package com.example.hanul.controller;

import com.example.hanul.dto.ItemDTO;
import com.example.hanul.model.ItemEntity;
import com.example.hanul.model.MemberEntity;
import com.example.hanul.service.ItemService;
import com.example.hanul.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private final MemberService memberService;

    @Autowired
    public ItemController(ItemService itemService, MemberService memberService) {
        this.itemService = itemService;
        this.memberService = memberService;
    }

    // 멤버 아이디를 지정하고 해당 멤버를 찾아 상품을 등록
    @PostMapping("/register")
    public ResponseEntity<String> registerItem(@RequestBody ItemDTO itemDTO) {
        String memberId = "example_member_id"; // 멤버 아이디를 지정하세요
        MemberEntity member = memberService.getMemberById(memberId);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("해당 멤버를 찾을 수 없습니다.");
        }

        ItemEntity createdItem = itemService.saveItem(member, itemDTO);
        if (createdItem != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("상품 등록이 성공하였습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상품 등록에 실패하였습니다.");
        }
    }

    // 주어진 상품 ID에 해당하는 상품의 세부 정보를 가져옴
    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemDetails(@PathVariable String itemId) {
        ItemEntity item = itemService.getItemById(itemId);
        if (item != null) {
            return ResponseEntity.ok(item);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("상품을 찾을 수 없습니다.");
        }
    }

    // 주어진 상품 ID에 해당하는 상품을 업데이트
    @PutMapping("/{itemId}")
    public ResponseEntity<String> updateItem(@PathVariable String itemId, @RequestBody ItemDTO itemDTO) {
        boolean updated = itemService.updateItem(itemId, itemDTO);
        if (updated) {
            return ResponseEntity.ok("상품 수정이 성공하였습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상품 수정에 실패하였습니다.");
        }
    }

    //주어진 멤버 ID에 해당하는 멤버의 모든 상품 목록을 가져옴
    @GetMapping("/members/{memberId}")
    public ResponseEntity<List<ItemEntity>> getItemsByMember(@PathVariable String memberId) {
        MemberEntity member = memberService.getMemberById(memberId);
        if (member != null) {
            List<ItemEntity> items = itemService.getItemsByMember(member);
            return ResponseEntity.ok(items);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    //심리 상담 챗봇을 통해 추천된 상품 목록을 가져옴
    @PostMapping("/recommend")
    public ResponseEntity<List<ItemEntity>> recommendItems(@RequestBody String counselingText) {
        // 심리 상담 챗봇을 통해 추천된 상품 목록을 가져오는 로직을 구현
        List<ItemEntity> recommendedItems = itemService.getRecommendedItems(counselingText);
        if (recommendedItems != null) {
            return ResponseEntity.ok(recommendedItems);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 로그인에 성공하고 주어진 멤버 ID가 있는 경우, 해당 멤버에게 아이템을 저장
    @PostMapping("/members/{memberId}/save")
    public ResponseEntity<String> saveItemForMember(@PathVariable String memberId, @RequestBody ItemDTO itemDTO) {
        MemberEntity member = memberService.getMemberById(memberId);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("해당 멤버를 찾을 수 없습니다.");
        }

        ItemEntity createdItem = itemService.saveItem(member, itemDTO);
        if (createdItem != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("상품 저장이 성공하였습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상품 저장에 실패하였습니다.");
        }
    }
}
