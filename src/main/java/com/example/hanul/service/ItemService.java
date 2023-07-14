package com.example.hanul.service;

import com.example.hanul.dto.ItemDTO;
import com.example.hanul.model.ItemEntity;
import com.example.hanul.model.MemberEntity;
import com.example.hanul.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ItemService {
    private final ItemRepository itemRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public ItemEntity saveItem(MemberEntity member, ItemDTO itemDTO) {
        ItemEntity itemEntity = ItemEntity.builder()
                .itemNm(itemDTO.getItemNm())
                .itemDetail(itemDTO.getItemDetail())
                .member(member)
                .build();

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
}
