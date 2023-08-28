package com.example.hanul.controller;

import com.example.hanul.dto.GenreDTO;
import com.example.hanul.response.GenreListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

// 장르 id 리스트 확인
@Controller
@RequestMapping("/genres")
public class GenreController {

    private final WebClient webClient;

    @Value("${tmdb.api.key}")
    private String tmdbApiKey;

    @Autowired
    public GenreController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @GetMapping("/list")
    @ResponseBody
    public List<GenreDTO> getGenreList() {
        String apiKey = getTmdbApiKey();
        String genreUrl = "https://api.themoviedb.org/3/genre/movie/list?api_key=" + apiKey + "&language=ko";

        Mono<GenreListResponse> genreResponseMono = webClient.get()
                .uri(genreUrl)
                .retrieve()
                .bodyToMono(GenreListResponse.class);

        GenreListResponse genreListResponse = genreResponseMono.block();
        if (genreListResponse != null) {
            return genreListResponse.getGenres();
        } else {
            return null;
        }
    }

    private String getTmdbApiKey() {
        return tmdbApiKey;
    }
}
