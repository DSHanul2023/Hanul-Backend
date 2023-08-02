package com.example.hanul;

import com.example.hanul.dto.ItemDTO;
import com.example.hanul.dto.TMDBMovieDTO;
import com.example.hanul.model.ItemEntity;
import com.example.hanul.response.TMDBMovieListResponse;
import com.example.hanul.service.ItemService;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {
    @JsonProperty("total_pages")
    private int totalPages;

    private final ItemService itemService;
    private final WebClient webClient;

    @Value("${tmdb.api.key}")
    private String tmdbApiKey;

    @Autowired
    public DataInitializer(ItemService itemService, WebClient.Builder webClientBuilder) {
        this.itemService = itemService;
        this.webClient = webClientBuilder.build();
    }

    @Override
    public void run(String... args) throws Exception {
        registerTMDBMovies();
    }

    private void registerTMDBMovies() {
        String apiKey = getTmdbApiKey();
        int currentPage = 1;

        while (currentPage <= 500) { // 최대 500 페이지까지 생성하도록 수정
            // TMDB API에서 한국어 영화 목록 가져오기 (페이지별로 데이터 요청, language 파라미터 추가)
            String url = "https://api.themoviedb.org/3/movie/popular?api_key=" + apiKey + "&page=" + currentPage + "&language=ko-KR";
            Mono<TMDBMovieListResponse> responseMono = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(TMDBMovieListResponse.class);

            try {
                // 응답 데이터에서 필요한 정보 추출하여 ItemEntity에 등록
                TMDBMovieListResponse movieListResponse = responseMono.block();
                if (movieListResponse != null) {
                    List<TMDBMovieDTO> movies = movieListResponse.getResults();
                    for (TMDBMovieDTO movie : movies) {
                        ItemDTO itemDTO = new ItemDTO();
                        itemDTO.setItemNm(movie.getTitle());
                        itemDTO.setItemDetail(movie.getOverview());

                        // 포스터 URL을 기본 URL과 poster_path를 이용하여 구성
                        String posterUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
                        itemDTO.setPosterUrl(posterUrl);

                        // ItemEntity를 생성하고 포스터 URL을 설정한 후 아이템으로 등록합니다.
                        ItemEntity itemEntity = ItemEntity.builder()
                                .itemNm(itemDTO.getItemNm())
                                .itemDetail(itemDTO.getItemDetail())
                                .posterUrl(itemDTO.getPosterUrl()) // 포스터 URL 설정
                                .build();

                        itemService.saveItemWithPoster(itemEntity);
                    }
                    log.info("페이지 " + currentPage + "의 TMDB 데이터 등록이 완료되었습니다.");

                    // 총 페이지 수 갱신
                    totalPages = movieListResponse.getTotalPages();
                    currentPage++;
                } else {
                    log.error("TMDB 데이터 등록 중 오류가 발생하였습니다.");
                    break;
                }
            } catch (Exception e) {
                log.error("TMDB 데이터 등록 중 오류가 발생하였습니다.", e);
                break;
            }
        }

        log.info("모든 TMDB 데이터 등록이 완료되었습니다.");
        log.info("총 등록된 아이템 수: " + itemService.getTotalItemCount());
    }

    private String getTmdbApiKey() {
        return tmdbApiKey;
    }
}
