package com.example.hanul.repository;

import com.example.hanul.model.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, String> {
    MemberEntity findByEmailAndPassword(String email, String password);
    Optional<MemberEntity> findById(String memberId);

    MemberEntity findByEmail(String email);
}


