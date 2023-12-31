package com.example.hanul.repository;

import com.example.hanul.model.ItemEntity;
import com.example.hanul.model.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, String> {
    ItemEntity findByItemNm(String itemNm);

    List<ItemEntity> findByBookmarkedByMembers(MemberEntity member);
}
