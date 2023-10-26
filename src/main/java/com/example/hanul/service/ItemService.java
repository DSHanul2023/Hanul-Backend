package com.example.hanul.service;

import com.example.hanul.dto.*;
import com.example.hanul.model.ItemEntity;
import com.example.hanul.model.MemberEntity;
import com.example.hanul.repository.ItemRepository;
import com.example.hanul.response.CreditListResponse;
import com.example.hanul.response.KeywordListResponse;
import com.example.hanul.response.ProviderListResponse;
import com.example.hanul.response.TMDBMovieListResponse;
import javassist.compiler.ast.Keyword;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ItemService {

    private final ItemRepository itemRepository;

    private final WebClient webClient;

    @Value("${tmdb.api.key}")
    private String tmdbApiKey;

    @Autowired
    public ItemService(ItemRepository itemRepository, WebClient.Builder webClientBuilder) {
        this.itemRepository = itemRepository;

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(-1)) // to unlimited memory size
                .build();
        this.webClient = webClientBuilder
                .exchangeStrategies(exchangeStrategies) // set exchange strategies
                .build();
    }

    // TMDB API 키를 가져오는 메서드
    public String getTmdbApiKey() {
        return tmdbApiKey;
    }

    // ItemEntity와 함께 포스터 URL을 저장
    public ItemEntity saveItemWithPoster(ItemEntity itemEntity) {
        try {
            // 중복 등록 방지를 위해 이미 저장된 아이템인지 확인
            ItemEntity existingItem = itemRepository.findByItemNm(itemEntity.getItemNm());
            if (existingItem != null) {
                log.info("이미 등록된 상품: " + existingItem.getItemNm());
                return existingItem;
            }

            return itemRepository.save(itemEntity);
        } catch (Exception e) {
            log.error("상품 저장 중 오류가 발생하였습니다.", e);
            return null;
        }
    }

    public ItemEntity getItemById(String itemId) {
        return itemRepository.findById(itemId).orElse(null);
    }

    public boolean updateItem(String itemId, ItemDTO itemDTO) {
        ItemEntity itemEntity = itemRepository.findById(itemId).orElse(null);
        if (itemEntity != null) {
            itemEntity.setItemNm(itemDTO.getItemNm());
            itemEntity.setItemDetail(itemDTO.getItemDetail());
            try {
                itemRepository.save(itemEntity);
                return true;
            } catch (Exception e) {
                log.error("상품 수정 중 오류가 발생하였습니다.", e);
                return false;
            }
        } else {
            return false;
        }
    }

    public List<ItemEntity> getRecommendedItems(String counselingText) {
        // 심리 상담 챗봇을 통해 추천된 상품 목록을 가져오는 로직을 구현
        List<ItemEntity> recommendedItems = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ItemEntity item = ItemEntity.builder()
                    .itemNm("Recommended Item " + (i + 1))
                    .itemDetail("Recommended Item Detail " + (i + 1))
                    .build();
            recommendedItems.add(item);
        }
        return recommendedItems;
    }

    public List<ItemEntity> getAllItems() {
        return itemRepository.findAll();
    }

    // 총 아이템 수를 가져오는 메서드 추가
    public long getTotalItemCount() {
        return itemRepository.count();
    }

    public Page<ItemEntity> getAllItemsPaged(Pageable pageable) {
        return itemRepository.findAll(pageable);
    }

    public ItemEntity registerItem(ItemDTO itemDTO) {

        ItemEntity itemEntity = ItemEntity.builder()
                .id(itemDTO.getId())
                .itemNm(itemDTO.getItemNm())
                .itemDetail(itemDTO.getItemDetail())
                .genreName(itemDTO.getGenreName())
                .keyword(itemDTO.getKeyword())
                .cast(itemDTO.getCast())
                .director(itemDTO.getDirector())
                .build();

        try {
            return itemRepository.save(itemEntity);
        } catch (Exception e) {
            log.error("상품 저장 중 오류가 발생하였습니다.", e);
            return null;
        }
    }


    public boolean deleteAdultMovie(String movieId) {
        Optional<ItemEntity> itemOptional = itemRepository.findById(movieId);
        if (itemOptional.isPresent()) {
            ItemEntity item = itemOptional.get();
            itemRepository.delete(item);
            return true;
        }
        return false;
    }

//    public ItemEntity getItemByMovieId(String movieId) {
//        return itemRepository.findByMovieId(movieId).orElse(null);
//    }

    public ProviderDTO getProviders(String movieId){
        String providerUrl = "https://api.themoviedb.org/3/movie/" + movieId + "/watch/providers?api_key=" + getTmdbApiKey();
        Mono<ProviderListResponse> providerResponseMono = webClient.get()
                .uri(providerUrl)
                .retrieve()
                .bodyToMono(ProviderListResponse.class);

        ProviderListResponse providerListResponse = providerResponseMono.block();

        if (providerListResponse != null) {
            ProviderDTO krProvider = providerListResponse.getResults().get("KR");
            return krProvider;
        } else{
            log.info("provider를 찾을 수 없습니다.");
            return null;
        }
    }

    public List<ProviderInfoDTO> getProviderInfo(List<ProviderInfoDTO> providerInfo){
        List<ProviderInfoDTO> currentProviders = providerInfo;
        List<ProviderInfoDTO> updatedProviders = new ArrayList<>();
        for(ProviderInfoDTO buy : currentProviders){
            String currentLogoPath = buy.getLogoPath();
            if (currentLogoPath != null && !currentLogoPath.isEmpty()) {
                String newLogoPath = "https://www.themoviedb.org/t/p/original" + currentLogoPath;
                buy.setLogoPath(newLogoPath); // 로고 경로 변경
            }
            updatedProviders.add(buy);
        }
        return updatedProviders;
    }

    public List<KeywordDTO> getKeyword(String movieId){
        String keywordsUrl = "https://api.themoviedb.org/3/movie/" + movieId + "/keywords?api_key=" + getTmdbApiKey();

        Mono<KeywordListResponse> keywordResponseMono = webClient.get()
                .uri(keywordsUrl)
                .retrieve()
                .bodyToMono(KeywordListResponse.class);

        KeywordListResponse keywordListResponse = keywordResponseMono.block();

        if(keywordListResponse != null){
            return keywordListResponse.getKeywords();
        }
        else return null;
    }

    public CreditListResponse getCredit(String movieId){
        String creditUrl = "https://api.themoviedb.org/3/movie/" + movieId + "/credits?api_key=" + getTmdbApiKey();

        Mono<CreditListResponse> creditResponseMono = webClient.get()
                .uri(creditUrl)
                .retrieve()
                .bodyToMono(CreditListResponse.class);

        CreditListResponse creditListResponse = creditResponseMono.block();

        return creditListResponse;
    }

    public ItemEntity bookmarkItem(MemberEntity member, String itemId) {
        try {
            ItemEntity itemEntity = itemRepository.findById(itemId)
                    .orElseThrow(() -> new EntityNotFoundException("Item not found"));

            List<ItemEntity> bookmarkedItems = member.getBookmarkedItems();

            // 중복 북마크 확인
            if (!bookmarkedItems.contains(itemEntity)) {
                bookmarkedItems.add(itemEntity);

                // itemEntity의 "bookmarkedByMembers" 필드에도 Member 추가
                itemEntity.getBookmarkedByMembers().add(member);

                return itemRepository.save(itemEntity);
            } else {
                // 이미 북마크된 아이템인 경우 기존 아이템 제거
                bookmarkedItems.remove(itemEntity);

                // itemEntity의 "bookmarkedByMembers" 필드에서 Member 제거
                itemEntity.getBookmarkedByMembers().remove(member);

                log.warn("북마크 취소");
                itemRepository.save(itemEntity);
                return null;
            }
        } catch (DataAccessException e) {
            log.error("북마크 중 데이터 접근 오류가 발생하였습니다.", e);
        } catch (EntityNotFoundException e) {
            log.error("북마크 중 아이템을 찾을 수 없습니다.", e);
        } catch (Exception e) {
            log.error("북마크 중 오류가 발생하였습니다.", e);
        }
        return null;
    }

    public void deleteBookmark(MemberEntity member, String itemId) {
        try {
            ItemEntity itemEntity = itemRepository.findById(itemId)
                    .orElseThrow(() -> new EntityNotFoundException("Item not found"));

            List<ItemEntity> bookmarkedItems = member.getBookmarkedItems();

            // 북마크 목록에서 해당 아이템 제거
            bookmarkedItems.remove(itemEntity);

            // itemEntity의 "bookmarkedByMembers" 필드에서도 Member 제거
            itemEntity.getBookmarkedByMembers().remove(member);

            // 변경된 정보 저장
            itemRepository.save(itemEntity);
        } catch (DataAccessException e) {
            log.error("북마크 삭제 중 데이터 접근 오류가 발생하였습니다.", e);
        } catch (EntityNotFoundException e) {
            log.error("북마크 삭제 중 아이템을 찾을 수 없습니다.", e);
        } catch (Exception e) {
            log.error("북마크 삭제 중 오류가 발생하였습니다.", e);
        }
    }

}
