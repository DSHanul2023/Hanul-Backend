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

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemDetails(@PathVariable String itemId) {
        ItemEntity item = itemService.getItemById(itemId);
        if (item != null) {
            return ResponseEntity.ok(item);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("상품을 찾을 수 없습니다.");
        }
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<String> updateItem(@PathVariable String itemId, @RequestBody ItemDTO itemDTO) {
        boolean updated = itemService.updateItem(itemId, itemDTO);
        if (updated) {
            return ResponseEntity.ok("상품 수정이 성공하였습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상품 수정에 실패하였습니다.");
        }
    }

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
}
