package com.example.hanul.controller;

import com.example.hanul.dto.ItemDTO;
import com.example.hanul.dto.TMDBMovieDTO;
import com.example.hanul.model.ItemEntity;
import com.example.hanul.model.MemberEntity;
import com.example.hanul.response.TMDBMovieListResponse;
import com.example.hanul.service.ItemService;
import com.example.hanul.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private final MemberService memberService;
    private final WebClient webClient;

    @Autowired
    public ItemController(ItemService itemService, MemberService memberService, WebClient.Builder webClientBuilder) {
        this.itemService = itemService;
        this.memberService = memberService;
        this.webClient = webClientBuilder.baseUrl("https://api.themoviedb.org/3").build();
    }

    // 등록된 모든 상품을 가져옴
    // 웹 응답의 기본 설정에 따라서 한번에 보여지는 데이터의 양이 제한되어 있을 수 있음
    @GetMapping("/all")
    public ResponseEntity<Page<ItemEntity>> getAllItems(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ItemEntity> itemPage = itemService.getAllItemsPaged(pageable);

        if (!itemPage.isEmpty()) {
            return ResponseEntity.ok(itemPage);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
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

        ItemEntity itemEntity = ItemEntity.builder()
                .itemNm(itemDTO.getItemNm())
                .itemDetail(itemDTO.getItemDetail())
                .member(member)
                .build();

        ItemEntity createdItem = itemService.saveItem(itemEntity);
        if (createdItem != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("상품 저장이 성공하였습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상품 저장에 실패하였습니다.");
        }
    }
}
