package com.example.hanul;

import com.example.hanul.dto.ItemDTO;
import com.example.hanul.dto.TMDBMovieDTO;

import com.example.hanul.response.TMDBMovieListResponse;
import com.example.hanul.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

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
        String apiKey = itemService.getTmdbApiKey();

        // TMDB API에서 영화 목록 가져오기
        String url = "https://api.themoviedb.org/3/movie/popular?api_key=" + apiKey;
        Mono<TMDBMovieListResponse> responseMono = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(TMDBMovieListResponse.class);

        // 응답 데이터에서 필요한 정보 추출하여 ItemEntity에 등록
        TMDBMovieListResponse movieListResponse = responseMono.block();
        if (movieListResponse != null) {
            List<TMDBMovieDTO> movies = movieListResponse.getResults();
            for (TMDBMovieDTO movie : movies) {
                ItemDTO itemDTO = new ItemDTO();
                itemDTO.setItemNm(movie.getTitle());
                itemDTO.setItemDetail(movie.getOverview());
                itemService.registerItem(itemDTO);
            }
            log.info("TMDB 데이터 등록이 완료되었습니다.");
        } else {
            log.error("TMDB 데이터 등록 중 오류가 발생하였습니다.");
        }
    }
}
