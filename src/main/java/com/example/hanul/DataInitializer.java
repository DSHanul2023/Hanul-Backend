package com.example.hanul;

import com.example.hanul.dto.GenreDTO;
import com.example.hanul.dto.ItemDTO;
import com.example.hanul.dto.KeywordDTO;
import com.example.hanul.dto.TMDBMovieDTO;
import com.example.hanul.model.ItemEntity;
import com.example.hanul.response.GenreListResponse;
import com.example.hanul.response.KeywordListResponse;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {
    @JsonProperty("total_pages")
    private int totalPages;

    public int getTotalPages() {
        return totalPages;
    }
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
                        // 키워드로 성인영화 저장 안 하기
                        String keywordUrl = "https://api.themoviedb.org/3/movie/" + movie.getMovieId() + "/keywords?api_key=" + apiKey;
                        List<String> keywordNames = Arrays.asList("erotic movie", "adultery", "softcore");
                        boolean adultMovie = false;

                        Mono<KeywordListResponse> keywordResponseMono = webClient.get()
                                .uri(keywordUrl)
                                .retrieve()
                                .bodyToMono(KeywordListResponse.class);

                        KeywordListResponse keywordListResponse = keywordResponseMono.block();

                        if(keywordListResponse.getKeywords() == null || keywordListResponse.getKeywords().size() == 0){ // 키워드 없는 것도 제외
                            adultMovie = true;
                            continue;
                        }
                        else if (keywordListResponse != null) {
                            for (KeywordDTO keyword : keywordListResponse.getKeywords()) {
                                for(String keywordName : keywordNames){
                                    if(keyword.getKeywordName().equals(keywordName)){
                                        adultMovie = true;
                                        break;
                                    }
                                }
                                if(adultMovie == true) break;
                            }
                        }

                        if(adultMovie == true) continue; // 성인 영화가 아닌 경우에만 등록

                        ItemDTO itemDTO = new ItemDTO();
                        itemDTO.setItemNm(movie.getTitle());
                        itemDTO.setItemDetail(movie.getOverview());
                        itemDTO.setMovieId(movie.getMovieId());

                        // Fetch genre details for each genre ID
                        List<String> genreNames = new ArrayList<>();
                        for (Integer genreId : movie.getGenreIds()) {
                            // Construct the URL to fetch genre details using the genre ID
                            String genreUrl = "https://api.themoviedb.org/3/genre/movie/list?api_key=" + apiKey + "&language=ko";

                            // Make the API call to get genre details
                            Mono<GenreListResponse> genreResponseMono = webClient.get()
                                    .uri(genreUrl)
                                    .retrieve()
                                    .bodyToMono(GenreListResponse.class);

                            // Block and get the genre response
                            GenreListResponse genreListResponse = genreResponseMono.block();

                            // Find the genre name for the given genre ID
                            if (genreListResponse != null) {
                                for (GenreDTO genre : genreListResponse.getGenres()) {
                                    if (genre.getGenreId() == genreId) {
                                        genreNames.add(genre.getGenreName());
                                        break;
                                    }
                                }
                            }
                        }
                        itemDTO.setGenreName(String.join(", ", genreNames));

                        // 포스터 URL을 기본 URL과 poster_path를 이용하여 구성
                        String posterUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
                        itemDTO.setPosterUrl(posterUrl);

                        // ItemEntity를 생성하고 포스터 URL을 설정한 후 아이템으로 등록합니다.
                        ItemEntity itemEntity = ItemEntity.builder()
                                .itemNm(itemDTO.getItemNm())
                                .itemDetail(itemDTO.getItemDetail())
                                .posterUrl(itemDTO.getPosterUrl()) // 포스터 URL 설정
                                .genreName(itemDTO.getGenreName())
                                .movieId(itemDTO.getMovieId())
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
