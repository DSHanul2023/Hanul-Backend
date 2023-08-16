package com.example.hanul.repository;

import com.example.hanul.model.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, String> {
    List<CommentEntity> findByBoardId(String boardId);
    List<CommentEntity> findByMemberId(String memberId);

}
