package com.example.hanul.dto;

import com.example.hanul.model.InquiryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class InquiryDTO {
    private String id;
    private String inquiryNm;
    private String inquiryDetail;
    private boolean state;
    private String createdAt;
    private String answer;
    private String answer_date;
    public InquiryDTO(final InquiryEntity entity){
        this.id = entity.getId();
        this.inquiryNm = entity.getInquiryNm();
        this.inquiryDetail = entity.getInquiryDetail();
        this.state = entity.isState();
        this.createdAt = entity.getCreatedAt();
        this.answer = entity.getAnswer();
        this.answer_date = entity.getAnswer_date();
    }
    public static InquiryEntity toEntity(final InquiryDTO dto){
        return InquiryEntity.builder()
                .id(dto.getId())
                .inquiryNm(dto.getInquiryNm())
                .inquiryDetail(dto.getInquiryDetail())
                .state(dto.isState())
                .createdAt(dto.getCreatedAt())
                .answer(dto.getAnswer())
                .answer_date(dto.getAnswer_date())
                .build();
    }
}
