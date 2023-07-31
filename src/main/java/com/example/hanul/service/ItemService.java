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

@Slf4j
@Service
public class ItemService {

    @Value("${tmdb.api.key}")
    private String tmdbApiKey;

    private final ItemRepository itemRepository;

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

    public Page<ItemEntity> getAllItemsPaged(Pageable pageable) {
        return itemRepository.findAll(pageable);
    }

    public ItemEntity registerItem(ItemDTO itemDTO) {

        ItemEntity itemEntity = ItemEntity.builder()
                .itemNm(itemDTO.getItemNm())
                .itemDetail(itemDTO.getItemDetail())
                .build();

        try {
            return itemRepository.save(itemEntity);
        } catch (Exception e) {
            log.error("상품 저장 중 오류가 발생하였습니다.", e);
            return null;
        }
    }
}
