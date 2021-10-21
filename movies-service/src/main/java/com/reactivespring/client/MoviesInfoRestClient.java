package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class MoviesInfoRestClient {

    @Value("${restClient.moviesInfoUrl}")
    private String moviesInfoUrl;

    private final WebClient webClient;

    public MoviesInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> retrieveMovieInfo(String movieId) {
        String url = moviesInfoUrl.concat("/{id}");

        return webClient
                .get()
                .uri(url, movieId)
                .retrieve()
                .bodyToMono(MovieInfo.class)
                .log();
    }
}
