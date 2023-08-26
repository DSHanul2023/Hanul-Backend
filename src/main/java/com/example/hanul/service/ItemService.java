package com.example.hanul.service;

import com.example.hanul.dto.ItemDTO;
import com.example.hanul.model.ItemEntity;
import com.example.hanul.model.MemberEntity;
import com.example.hanul.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ItemService {

    private final ItemRepository itemRepository;

    @Value("${tmdb.api.key}")
    private String tmdbApiKey;

    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    // TMDB API 키를 가져오는 메서드
    public String getTmdbApiKey() {
        return tmdbApiKey;
    }

    public ItemEntity saveItem(ItemEntity itemEntity) {
        try {
            return itemRepository.save(itemEntity);
        } catch (DataAccessException e) {
            log.error("상품 저장 중 데이터 접근 오류가 발생하였습니다.", e);
        } catch (Exception e) {
            log.error("상품 저장 중 오류가 발생하였습니다.", e);
        }
        return null;
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

    public List<ItemEntity> getItemsByMember(MemberEntity member) {
        return itemRepository.findByMember(member);
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
                .itemNm(itemDTO.getItemNm())
                .itemDetail(itemDTO.getItemDetail())
                .genreName(itemDTO.getGenreName())
                .movieId(itemDTO.getMovieId())
                .build();

        try {
            return itemRepository.save(itemEntity);
        } catch (Exception e) {
            log.error("상품 저장 중 오류가 발생하였습니다.", e);
            return null;
        }
    }

    // item 북마크에 저장
    public ItemEntity saveItemForMember(MemberEntity member, ItemDTO itemDTO) {
        // 중복 등록을 체크하는 로직 추가
        ItemEntity existingItem = itemRepository.findByItemNmAndMember(itemDTO.getItemNm(), member);
        if (existingItem != null) {
            return null; // 이미 등록된 아이템인 경우 null 반환
        }

        ItemEntity itemEntity = ItemEntity.builder()
                .itemNm(itemDTO.getItemNm())
                .itemDetail(itemDTO.getItemDetail())
                .genreName(itemDTO.getGenreName())
                .movieId(itemDTO.getMovieId())
                .member(member)
                .build();

        return itemRepository.save(itemEntity);
    }

    // item 저장 시 북마크 중복 확인
    public boolean checkIfItemAlreadySaved(MemberEntity member, ItemDTO itemDTO) {
        ItemEntity existingItem = itemRepository.findByItemNmAndMember(itemDTO.getItemNm(), member);
        return existingItem != null;
    }

    public boolean deleteAdultMovie(String movieId) {
        Optional<ItemEntity> itemOptional = itemRepository.findByMovieId(movieId);
        if (itemOptional.isPresent()) {
            ItemEntity item = itemOptional.get();
            itemRepository.delete(item);
            return true;
        }
        return false;
    }

    public ItemEntity getItemByMovieId(String movieId) {
        return itemRepository.findByMovieId(movieId).orElse(null);
    }
}
