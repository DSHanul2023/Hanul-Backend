package com.example.hanul.controller;

import com.example.hanul.dto.ItemDTO;
import com.example.hanul.dto.TMDBMovieDTO;
import com.example.hanul.model.ItemEntity;
import com.example.hanul.model.MemberEntity;
import com.example.hanul.repository.ItemRepository;
import com.example.hanul.response.TMDBMovieListResponse;
import com.example.hanul.service.ItemService;
import com.example.hanul.service.MemberService;
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
import reactor.core.publisher.Mono;

import org.springframework.data.domain.Pageable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        // 중복 등록 방지를 위해 이미 저장된 아이템인지 확인
        ItemEntity existingItem = itemService.saveItemWithPoster(itemEntity);
        if (existingItem != null) {
            return ResponseEntity.status(HttpStatus.OK).body("이미 등록된 상품입니다.");
        }

        ItemEntity createdItem = itemService.saveItem(itemEntity);
        if (createdItem != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body("상품 저장이 성공하였습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상품 저장에 실패하였습니다.");
        }
    }

    // 데이터 초기화를 위해 사용되는 클래스
    @Component
    @Slf4j
    static class DataInitializer implements CommandLineRunner {
        private final ItemService itemService;
        private final ItemRepository itemRepository;
        private final WebClient webClient;

        @Value("${tmdb.api.key}")
        private String tmdbApiKey;

        @Autowired
        public DataInitializer(ItemService itemService, ItemRepository itemRepository, WebClient.Builder webClientBuilder) {
            this.itemService = itemService;
            this.itemRepository = itemRepository;
            this.webClient = webClientBuilder.build();
        }

        @Override
        public void run(String... args) throws Exception {
            registerTMDBMovies();
        }

        private void registerTMDBMovies() {
            String apiKey = getTmdbApiKey();
            int currentPage = 1;

            while (currentPage <= 500) {
                // TMDB API에서 한국어 영화 목록 가져오기 (페이지별로 데이터 요청, language 파라미터 추가)
                String url = "https://api.themoviedb.org/3/movie/popular?api_key=" + apiKey + "&page=" + currentPage + "&language=ko-KR";
                Mono<TMDBMovieListResponse> responseMono = webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(TMDBMovieListResponse.class);

                try {
                    // 응답 데이터에서 필요한 정보 추출하여 ItemEntity에 등록
                    TMDBMovieListResponse movieListResponse = responseMono.block();
                    if (movieListResponse != null) {
                        List<TMDBMovieDTO> movies = movieListResponse.getResults();

                        // 중복 등록 방지를 위해 이미 저장된 영화 목록 확인
                        Set<String> existingMovies = new HashSet<>();
                        for (ItemEntity item : itemService.getAllItems()) {
                            existingMovies.add(item.getItemNm());
                        }

                        // 이미 등록된 영화인 경우 건너뛰고 아닌 경우만 등록
                        for (TMDBMovieDTO movie : movies) {
                            if (existingMovies.contains(movie.getTitle())) {
                                log.info("이미 등록된 영화: " + movie.getTitle());
                                continue;
                            }

                            ItemDTO itemDTO = new ItemDTO();
                            itemDTO.setItemNm(movie.getTitle());
                            itemDTO.setItemDetail(movie.getOverview());

                            // 포스터 URL을 기본 URL과 poster_path를 이용하여 구성
                            String posterUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
                            itemDTO.setPosterUrl(posterUrl);

                            // ItemEntity를 생성하고 포스터 URL을 설정한 후 아이템으로 등록합니다.
                            ItemEntity itemEntity = ItemEntity.builder()
                                    .itemNm(itemDTO.getItemNm())
                                    .itemDetail(itemDTO.getItemDetail())
                                    .posterUrl(itemDTO.getPosterUrl()) // 포스터 URL 설정
                                    .build();

                            itemService.saveItemWithPoster(itemEntity);
                        }

                        // 총 페이지 수 갱신
                        int totalPages = movieListResponse.getTotalPages();

                        currentPage++;
                    } else {
                        log.error("TMDB 데이터 등록 중 오류가 발생하였습니다.");
                        break;
                    }
                } catch (Exception e) {
                    log.error("TMDB 데이터 등록 중 오류가 발생하였습니다.", e);
                    break;
                }
            }

            log.info("모든 TMDB 데이터 등록이 완료되었습니다.");
            log.info("총 등록된 아이템 수: " + itemService.getTotalItemCount());
        }

        private String getTmdbApiKey() {
            return tmdbApiKey;
        }
    }
}
