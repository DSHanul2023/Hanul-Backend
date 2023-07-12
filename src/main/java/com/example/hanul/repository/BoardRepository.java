package com.example.hanul.repository;

import com.example.hanul.model.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Long> {

    BoardEntity findByIdx(String id);
    BoardEntity findByTitleContaining(String title);

    List<BoardEntity> findAll();
    long count();
}
