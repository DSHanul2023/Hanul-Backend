package com.example.hanul.repository;

import com.example.hanul.model.ChatEntity;
import com.example.hanul.model.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, String> {
    List<ChatEntity> findAllByMemberOrderByTimestampDesc(MemberEntity member);
}
