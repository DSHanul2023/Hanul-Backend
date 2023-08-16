package com.example.hanul.service;

import com.example.hanul.model.BoardEntity;
import com.example.hanul.repository.BoardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BoardService {
    @Autowired
    private BoardRepository boardRepository;

    private void validate(final BoardEntity entity) {
        if(entity == null) {
            log.warn("Entity cannot be null.");
            throw new RuntimeException(("Entity cannot be null."));
        } if(entity.getAuthor() == null) {
            log.warn("Unknown user.");
            throw new RuntimeException(("Unknown user."));
        }
    }

    public List<BoardEntity> retrieve() {
        return boardRepository.findAll();
    }

    public List<BoardEntity> retrieveMyPost(final String memberId){
        return boardRepository.findByMemberId(memberId);
    }

    public List<BoardEntity> create(BoardEntity entity) {
        validate(entity);

        boardRepository.save(entity);

        log.info("Entity saved.");

        return boardRepository.findAll();
    }

    public List<BoardEntity> delete(BoardEntity entity) {
        if(entity == null) {
            log.warn("Entity cannot be null.");
            throw new RuntimeException(("Entity cannot be null."));
        }
        try {
            boardRepository.delete(entity);
        } catch (Exception e) {
            log.error("error deleting entity ");

            throw new RuntimeException("error deleting entity");
        }

        return boardRepository.findAll();
    }

    public List<BoardEntity> update(BoardEntity entity) {
        if(entity == null) {
            log.warn("Entity cannot be null.");
            throw new RuntimeException(("Entity cannot be null."));
        }
        final Optional<BoardEntity> original = boardRepository.findById(entity.getIdx());
        original.ifPresent(board -> {
            board.setTitle(entity.getTitle());
            board.setContents(entity.getContents());
            if (entity.getImage() != null && entity.getImage().length > 0) {
                board.setImage(entity.getImage());
            }

            boardRepository.save(board);
        });
        return boardRepository.findAll();
    }
    public List<BoardEntity> searchBoard(String query) {
        return boardRepository.findByIdxContainingIgnoreCase(query);
    }
}