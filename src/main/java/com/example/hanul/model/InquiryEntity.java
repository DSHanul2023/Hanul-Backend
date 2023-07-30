package com.example.hanul.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name="Inquiry")
public class InquiryEntity {
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name="system-uuid",strategy = "uuid")
    private String id;
    private String memberId;
    private String inquiryNm;
    private String inquiryDetail;
    private boolean state;
    private String createdAt;
}

