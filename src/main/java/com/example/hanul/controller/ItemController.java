package com.example.hanul.controller;

import com.example.hanul.dto.*;
import com.example.hanul.model.ItemEntity;
import com.example.hanul.model.MemberEntity;
import com.example.hanul.response.*;
import com.example.hanul.service.FlaskService;
import com.example.hanul.service.ItemService;
import com.example.hanul.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;
    private final MemberService memberService;
    private final FlaskService flaskService;
    private final WebClient webClient;

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Autowired
    public ItemController(ItemService itemService, MemberService memberService, FlaskService flaskService, WebClient.Builder webClientBuilder) {
        this.itemService = itemService;
        this.memberService = memberService;
        this.flaskService = flaskService;
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

    // 주어진 영화 ID에 해당하는 상품의 세부 정보를 가져옴
    @GetMapping("/movieId/{movieId}")
    public ResponseEntity<Object> getItemDetailsByMovieId(@PathVariable String movieId) {
        ItemEntity item = itemService.getItemById(movieId);
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

    @DeleteMapping("/deleteAdultItem")
    public ResponseEntity<String> deleteAdultItem() {
        int count = 0;
        for (ItemEntity item : itemService.getAllItems()) {
            String releaseDatesUrl = "https://api.themoviedb.org/3/movie/" + item.getId() + "/release_dates?api_key=" + apiKey;

            try {
                Mono<ReleaseDateListResponse> releaseDateResponseMono = webClient.get()
                        .uri(releaseDatesUrl)
                        .retrieve()
                        .bodyToMono(ReleaseDateListResponse.class);

                ReleaseDateListResponse releaseDateListResponse = releaseDateResponseMono.block();

                if (releaseDateListResponse != null) {
                    for (ReleaseDateDTO releaseDate : releaseDateListResponse.getResults()) {
                        if ("KR".equals(releaseDate.getRegion())) {
                            for (ReleaseInfoDTO releaseInfo : releaseDate.getRelease_dates()) {
                                String certification = releaseInfo.getCertification();
                                // 여기에서 certification 값을 사용할 수 있습니다.
                                if ("18".equals(certification) || "Restricted Screening".equals(certification)
                                        || "19+".equals(certification) || "Limited".equals(certification) || "".equals(certification)) {
                                    itemService.deleteAdultMovie(item.getId());
                                    count++;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
                // 줄거리 없으면 제외
                if(item.getItemDetail().equals("")) itemService.deleteAdultMovie(item.getId());
                // 키워드 없으면 제외
                if(item.getKeyword().equals("")) itemService.deleteAdultMovie(item.getId());
            } catch (WebClientResponseException.NotFound ex) {
                // 만약 해당 URL로 영화 정보를 가져올 수 없는 경우, 여기에 삭제 코드를 추가
                itemService.deleteAdultMovie(item.getId());
                count++;
            }
        }
        return ResponseEntity.ok("총 " + count + "개의 성인영화가 삭제되었습니다.");
    }


    @GetMapping("/provider/{movieId}")
    public ResponseEntity<ProviderDTO> getProviders(@PathVariable String movieId){
        ProviderDTO krProvider;
        krProvider = itemService.getProviders(movieId);
        return ResponseEntity.status(HttpStatus.OK).body(krProvider);
    }

    @GetMapping("/providers/{movieId}")
    public ResponseEntity<?> getProvidersWithFlask(@PathVariable String movieId) {
        try {
            // Flask 서버에서 데이터 가져오기
            ProviderDTO flaskResponse = flaskService.ProvidersWithFlask(movieId);
            ProviderDTO providerDTO = itemService.getProviders(movieId);
            ProviderDTO responseDTO = new ProviderDTO();

            if (flaskResponse != null) {
                // Spring Boot 서버에서 가져온 데이터를 조합
                if (providerDTO != null) {
                    providerDTO.setTmdb_id(flaskResponse.getTmdb_id());
                    // providerDTO의 데이터를 그대로 가져와서 URL만 추가
                    if (providerDTO.getBuy() != null) {
                        for (ProviderInfoDTO providerInfoDTO : providerDTO.getBuy()) {
                            for(ProviderInfoDTO flaskInfoDTO : flaskResponse.getBuy()){
                                if(providerInfoDTO.getProvider_name().equals(flaskInfoDTO.getProvider_name())){
                                    providerInfoDTO.setUrl(flaskInfoDTO.getUrl());
                                }
                            }
                        }
                    }

                    if (providerDTO.getFlatrate() != null) {
                        for (ProviderInfoDTO providerInfoDTO : providerDTO.getFlatrate()) {
                            for(ProviderInfoDTO flaskInfoDTO : flaskResponse.getFlatrate()){
                                if(providerInfoDTO.getProvider_name().equals(flaskInfoDTO.getProvider_name())){
                                    providerInfoDTO.setUrl(flaskInfoDTO.getUrl());
                                }
                            }
                        }
                    }

                    if (providerDTO.getRent() != null) {
                        for (ProviderInfoDTO providerInfoDTO : providerDTO.getRent()) {
                            for(ProviderInfoDTO flaskInfoDTO : flaskResponse.getRent()){
                                if(providerInfoDTO.getProvider_name().equals(flaskInfoDTO.getProvider_name())){
                                    providerInfoDTO.setUrl(flaskInfoDTO.getUrl());
                                }
                            }
                        }
                    }
                }

                // 조합된 데이터를 JSON 형식으로 반환
                return ResponseEntity.status(HttpStatus.OK).body(providerDTO);
            } else {
                // 플라스크 서버로부터 데이터를 받지 못한 경우 에러 응답을 반환
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch data from Flask server.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error.");
        }
    }

    @GetMapping("/keywords/{movieId}")
    public ResponseEntity<List<KeywordDTO>> getKeywords(@PathVariable String movieId){
        List<KeywordDTO> keywordDTOList;
        keywordDTOList = itemService.getKeyword(movieId);
        return ResponseEntity.status(HttpStatus.OK).body(keywordDTOList);
    }

    // 로그인에 성공하고 주어진 멤버 ID가 있는 경우, 해당 멤버에게 아이템을 저장
    @PostMapping("/{itemId}/bookmark/{memberId}")
    public ResponseEntity<ItemEntity> bookmarkItem(
            @PathVariable String itemId,
            @PathVariable String memberId
    ) {
        ItemEntity item = itemService.getItemById(itemId);
        MemberEntity member = memberService.getMemberById(memberId);

        if (item != null && member != null) {
            ItemEntity updatedItem = itemService.bookmarkItem(member, itemId);
            if (updatedItem != null) {
                return ResponseEntity.status(HttpStatus.OK).body(updatedItem);
            } else {
                // 중복된 아이템인 경우 409 Conflict 상태 코드와 메시지 반환
                return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // 북마크 삭제
    @DeleteMapping("/{itemId}/deletebookmark/{memberId}")
    public ResponseEntity<String> deleteBookmark(@PathVariable String memberId, @PathVariable String itemId) {
        try {
            // memberId와 itemId를 사용하여 북마크 삭제 서비스 호출
            MemberEntity member = memberService.getMemberById(memberId);
            itemService.deleteBookmark(member, itemId);
            return ResponseEntity.ok("Bookmark removed successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to remove bookmark.");
        }
    }

    // 해당 item에 북마크한 member 정보
    @GetMapping("/{itemId}/bookmarked-members")
    public ResponseEntity<List<MemberEntity>> getBookmarkedMembers(@PathVariable String itemId) {
        ItemEntity item = itemService.getItemById(itemId);
        if (item != null) {
            List<MemberEntity> members = item.getBookmarkedByMembers();
            return ResponseEntity.status(HttpStatus.OK).body(members);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // Flask 서버에서 추천된 아이템을 가져와서 응답
    @GetMapping("/recommend/{memberId}")
    public ResponseEntity<List<ItemEntity>> recommendItemsFromFlask(@PathVariable String memberId) {
        // memberId를 사용하여 Flask 서버에 추천 요청을 보내고 응답 받음
        List<ItemEntity> recommendedItems = flaskService.RecommendWithFlask(memberId);

        // 추천된 아이템 목록을 반환
        return ResponseEntity.status(HttpStatus.OK).body(recommendedItems);
    }

    @GetMapping("/emotion2/{memberId}")
    public ResponseEntity<String> emotionFromFlask(@PathVariable String memberId) {
        String emotion = flaskService.emotionWithFlask2(memberId);
        return ResponseEntity.ok(emotion);
    }
}