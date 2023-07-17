package com.example.hanul.service;

import com.example.hanul.dto.ItemDTO;
import com.example.hanul.model.ItemEntity;
import com.example.hanul.model.MemberEntity;
import com.example.hanul.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ItemService {
    private final ItemRepository itemRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public ItemEntity saveItem(ItemEntity itemEntity) {
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
        // counselingText를 기반으로 상품을 추천하고, 추천된 상품 목록을 반환
        // 예시로 랜덤하게 상품을 생성하는 로직을 작성 -> 실제 추천 알고리즘을 구현
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
}
