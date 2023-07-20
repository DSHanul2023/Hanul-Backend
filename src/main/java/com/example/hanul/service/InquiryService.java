package com.example.hanul.service;

import com.example.hanul.model.InquiryEntity;
import com.example.hanul.repository.InquiryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class InquiryService {

    @Autowired
    private InquiryRepository repository;
    public List<InquiryEntity> create(final InquiryEntity entity){
        validate(entity);
        repository.save(entity);

        log.info("Entity Id : {} is saved.",entity.getId());

        return repository.findByMemberId(entity.getMemberId());
    }

    private static void validate(InquiryEntity entity) {
        if(entity ==null){
            log.warn("Entity cannot be null.");
            throw new RuntimeException("Entity cannot be null");
        }
        if(entity.getMemberId()==null){
            log.warn("Unknown member.");
            throw new RuntimeException("Unknown member.");
        }
    }
    public List<InquiryEntity> retrieve(final String memberId){
        return repository.findByMemberId(memberId);
    }
    public List<InquiryEntity> update(final InquiryEntity entity){
        //(1) 저장할 엔티티가 유효한지 확인한다. 이 메서드는 2.3.1 create Todo에서 구현했다.
        validate(entity);

        //(2) 넘겨받은 엔티티 id를 이용해 TodoEntity를 가져온다. 존재하지 않는 엔티티는 업데이트할수없기 때문이다.
        final Optional<InquiryEntity> original = repository.findById(entity.getId());

        original.ifPresent(inquiry -> {
            //(3) 반환된 TodoEntity가 존재하면 값을 새 entity 값으로 덮어씌운다.
            inquiry.setInquiryNm(entity.getInquiryNm());
            inquiry.setInquiryDetail(entity.getInquiryDetail());
            inquiry.setState(entity.isState());

            //(4)데이터베이스에 새 값을 저장한다.
            repository.save(inquiry);
        });
        //2.3.2 Retrieve Todo에서 만든 메서드를 이용해 사용자의 모든 Todo리스트를 리턴한다.
        return retrieve(entity.getMemberId());
    }
    public List<InquiryEntity> delete(final InquiryEntity entity){
        //(1)저장할 엔티티가 유효한지 확인.
        validate(entity);
        try{
            //(2)엔티티삭제
            repository.delete(entity);
        }catch(Exception e){
            //(3)exception발생시 id와 exception로깅한다
            log.error("error deleting entity",entity.getId(),e);
            //(4) 컨트롤러로 exception 보낸다. 데이터베이스 내부 로직을 캡슐화하려면 e를 리턴하지않고 새 exception오브젝트를 리턴한다.
            throw new RuntimeException("error deleting entity"+entity.getId());
        }
        //(5)새 Todo리스트를 가져와 리턴
        return retrieve(entity.getMemberId());
    }
}