package com.example.hanul.repository;

import com.example.hanul.model.ItemEntity;
import com.example.hanul.model.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, String> {
    List<ItemEntity> findByMember(MemberEntity member);
    ItemEntity findByItemNm(String itemNm);
}
