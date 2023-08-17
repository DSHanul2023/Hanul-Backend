package com.example.hanul;

import com.example.hanul.dto.GenreDTO;
import com.example.hanul.dto.ItemDTO;
import com.example.hanul.dto.TMDBMovieDTO;
import com.example.hanul.model.ItemEntity;
import com.example.hanul.repository.ItemRepository;
import com.example.hanul.response.TMDBMovieListResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ItemRepository itemRepository;
    private final WebClient webClient;

    @Value("${tmdb.api.key}")
    private String tmdbApiKey;

    @Autowired
    public DataInitializer(ItemRepository itemRepository, WebClient.Builder webClientBuilder) {
        this.itemRepository = itemRepository;
        this.webClient = webClientBuilder.build();
    }

    @Override
    public void run(String... args) throws Exception {
        registerTMDBMovies();
    }

    private void registerTMDBMovies() {
        String apiKey = getTmdbApiKey();
        int currentPage = 1;

        while (currentPage <= 500) { // 최대 500 페이지까지 데이터를 가져옴
            String url = "https://api.themoviedb.org/3/movie/popular?api_key=" + apiKey + "&page=" + currentPage + "&language=ko-KR";
            Mono<TMDBMovieListResponse> responseMono = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(TMDBMovieListResponse.class);

            try {
                TMDBMovieListResponse movieListResponse = responseMono.block();
                if (movieListResponse != null) {
                    List<TMDBMovieDTO> movies = movieListResponse.getResults();

                    // 중복을 확인하고 영화를 처리하여 저장
                    for (TMDBMovieDTO movie : movies) {
                        String movieTitle = movie.getTitle();
                        if (itemRepository.findByItemNm(movieTitle) != null) {
                            log.info("이미 등록된 영화: " + movieTitle);
                            continue;
                        }

                        ItemDTO itemDTO = new ItemDTO();
                        itemDTO.setItemNm(movieTitle);
                        itemDTO.setItemDetail(movie.getOverview());

                        // 장르가 있을 경우 설정
                        List<GenreDTO> genres = movie.getGenres();
                        if (genres != null && !genres.isEmpty()) {
                            String genreString = genres.stream()
                                    .map(GenreDTO::getName)
                                    .collect(Collectors.joining(", "));
                            itemDTO.setItemGenre(genreString);
                        }

                        String posterUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
                        itemDTO.setPosterUrl(posterUrl);

                        ItemEntity itemEntity = ItemEntity.builder()
                                .itemNm(itemDTO.getItemNm())
                                .itemDetail(itemDTO.getItemDetail())
                                .itemGenre(itemDTO.getItemGenre())
                                .posterUrl(itemDTO.getPosterUrl())
                                .build();

                        itemRepository.save(itemEntity);
                    }

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
        log.info("총 등록된 아이템 수: " + itemRepository.count());
    }

    private String getTmdbApiKey() {
        return tmdbApiKey;
    }
}
