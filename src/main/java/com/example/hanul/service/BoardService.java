package com.example.hanul.service;

import com.example.hanul.model.BoardEntity;
import com.example.hanul.model.MemberEntity;
import com.example.hanul.repository.BoardRepository;
import com.example.hanul.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BoardService {
    @Autowired
    private BoardRepository boardRepository;
    private void validate(final BoardEntity entity) {
        if (entity == null) {
            log.warn("Entity cannot be null.");
            throw new RuntimeException(("Entity cannot be null."));
        }
        if (entity.getAuthor() == null) {
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
        log.info("서비스 일단 들어옴");
        boardRepository.save(entity);
        log.info("엔티티 저장됨.");

        return boardRepository.findAll();
    }
    @Value("${app.upload-dir-board}")
    private String uploadDir;
    public boolean saveImage(MultipartFile file,BoardEntity entity){
        try{
            String fileId = entity.getTitle()+entity.getMemberId();
//            Path filePath = Paths.get(uploadDir, fileName);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "board_picture_" + fileId + "_" + timestamp + ".jpg";
            String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String folderPath = Paths.get(uploadDir, dateFolder).toString();
            Files.createDirectories(Paths.get(folderPath));
            // 대상 경로 생성
            String destinationPath = Paths.get(folderPath, fileName).toString();
            Path destination = Paths.get(destinationPath);
            // 업로드된 파일을 대상으로 복사
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            // Update the member's profile picture path
            entity.setImage(dateFolder+"/"+fileName);
            file.getInputStream().close();
            return true;
        } catch (Exception e) {
            throw new RuntimeException("error Message", e);
        }
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
            if (entity.getImage() != null) {
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