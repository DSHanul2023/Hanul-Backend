package com.example.hanul.service;

import com.example.hanul.model.CommentEntity;
import com.example.hanul.repository.CommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CommentService {
    @Autowired
    private CommentRepository repository;
    public List<CommentEntity> create(final CommentEntity entity){
        validate(entity);
        repository.save(entity);

        log.info("Entity Id : {} is saved.",entity.getId());

        return repository.findByMemberId(entity.getMemberId());
    }

    private static void validate(CommentEntity entity) {
        if(entity ==null){
            log.warn("Entity cannot be null.");
            throw new RuntimeException("Entity cannot be null");
        }
        if(entity.getAuthor()==null){
            log.warn("Unknown member.");
            throw new RuntimeException("Unknown member.");
        }
    }
    public List<CommentEntity> update(CommentEntity entity) {
        if(entity == null) {
            log.warn("Entity cannot be null.");
            throw new RuntimeException(("Entity cannot be null."));
        }
        final Optional<CommentEntity> original = repository.findById(entity.getId());
        original.ifPresent(board -> {
            //(3) 반환된 TodoEntity가 존재하면 값을 새 entity 값으로 덮어씌운다.
            board.setText(entity.getText());
            board.setDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            repository.save(board);
        });
        return repository.findAll();
    }
    public List<CommentEntity> delete(CommentEntity entity) {
        if(entity == null) {
            log.warn("Entity cannot be null.");
            throw new RuntimeException(("Entity cannot be null."));
        }
        try {
            repository.delete(entity);
        } catch (Exception e) {
            log.error("error deleting entity ");

            throw new RuntimeException("error deleting entity");
        }

        return repository.findAll();
    }
}
