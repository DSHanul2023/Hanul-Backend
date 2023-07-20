package com.example.hanul.repository;

import com.example.hanul.model.InquiryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquiryRepository extends JpaRepository<InquiryEntity,String> {
    List<InquiryEntity> findByMemberId(String memberId);
}
