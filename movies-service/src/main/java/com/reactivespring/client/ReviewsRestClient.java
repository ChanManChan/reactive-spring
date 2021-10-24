package com.reactivespring.client;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ClientException;
import com.reactivespring.exception.ServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.reactivespring.utils.RetryUtil.retrySpec;

@Component
@Slf4j
public class ReviewsRestClient {

    @Value("${restClient.reviewsUrl}")
    private String reviewsUrl;

    private final WebClient webClient;

    public ReviewsRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<Review> retrieveReviews(String movieId) {
        String url = UriComponentsBuilder.fromHttpUrl(reviewsUrl)
                .queryParam("movieInfoId", movieId)
                .buildAndExpand()
                .toUriString();

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    log.info("Status code is: {}", clientResponse.statusCode().value());
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.empty();
                    }
                    return clientResponse
                            .bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new ClientException(
                                    responseMessage,
                                    clientResponse.statusCode().value()
                            )));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    log.info("Status code is: {}", clientResponse.statusCode().value());
                    return clientResponse
                            .bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new ServerException(
                                    "Server exception in Reviews service: " + responseMessage
                            )));
                })
                .bodyToFlux(Review.class)
                .retryWhen(retrySpec())
                .log();
    }

    public Flux<Review> retrieveReviewsStream() {
        String url = reviewsUrl.concat("/stream");
        return webClient
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    log.info("Status code is: {}", clientResponse.statusCode().value());
                    return clientResponse
                            .bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new ClientException(
                                    responseMessage,
                                    clientResponse.statusCode().value()
                            )));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    log.info("Status code is: {}", clientResponse.statusCode().value());
                    return clientResponse
                            .bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new ServerException(
                                    "Server exception in Reviews service: " + responseMessage
                            )));
                })
                .bodyToFlux(Review.class)
                .retryWhen(retrySpec())
                .log();
    }
}
