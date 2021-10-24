package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084)
@TestPropertySource(properties = {
        "restClient.moviesInfoUrl=http://localhost:8084/v1/movies-info",
        "restClient.reviewsUrl=http://localhost:8084/v1/reviews"
})
class MoviesControllerIntegrationTest {

    @Autowired
    WebTestClient webTestClient;

    @AfterEach
    void tearDown() {
        WireMock.reset();
    }

    @Test
    void retrieveMovieById() {
        String movieId = "abc";

        stubFor(
                get(urlEqualTo("/v1/movies-info/" + movieId))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("movie-info.json")
                        )
        );

        stubFor(
                get(urlPathEqualTo("/v1/reviews"))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("reviews.json")
                        )
        );

        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    Movie responseBody = movieEntityExchangeResult.getResponseBody();
                    assert responseBody != null;
                    assert responseBody.getReviewList().size() == 2;
                    assert responseBody.getMovieInfo().getName().equals("Batman Begins");
                });
    }

    @Test
    void retrieveMovieByIdMovieNotFound() {
        String movieId = "abc";

        stubFor(get(urlEqualTo("/v1/movies-info/" + movieId)).willReturn(aResponse().withStatus(404)));

        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(String.class)
                .isEqualTo("There is no MovieInfo available for the passed in id: " + movieId);

        WireMock.verify(1, getRequestedFor(urlEqualTo("/v1/movies-info/" + movieId)));
    }

    @Test
    void retrieveMovieByIdReviewsNotFound() {
        String movieId = "abc";

        stubFor(
                get(urlEqualTo("/v1/movies-info/" + movieId))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("movie-info.json")
                        )
        );

        stubFor(get(urlPathEqualTo("/v1/reviews")).willReturn(aResponse().withStatus(404)));

        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    Movie responseBody = movieEntityExchangeResult.getResponseBody();
                    assert responseBody != null;
                    assert responseBody.getReviewList().size() == 0;
                    assert responseBody.getMovieInfo().getName().equals("Batman Begins");
                });
    }

    @Test
    void retrieveMovieByIdInternalServerErrorForMovieInfoService() {
        String movieId = "abc";

        stubFor(get(urlEqualTo("/v1/movies-info/" + movieId)).willReturn(
                aResponse()
                        .withStatus(500)
                        .withBody("MovieInfo service is unavailable")
        ));

        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server exception in MoviesInfo service: MovieInfo service is unavailable");

        WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/movies-info/" + movieId)));
    }

    @Test
    void retrieveMovieByIdInternalServerErrorForReviewsService() {
        String movieId = "abc";

        stubFor(
                get(urlEqualTo("/v1/movies-info/" + movieId))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("movie-info.json")
                        )
        );

        stubFor(get(urlPathEqualTo("/v1/reviews")).willReturn(
                aResponse()
                        .withStatus(500)
                        .withBody("Review service is unavailable")
        ));

        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .exchange()
                .expectStatus()
                .is5xxServerError()
                .expectBody(String.class)
                .isEqualTo("Server exception in Reviews service: Review service is unavailable");

        WireMock.verify(4, getRequestedFor(urlPathMatching("/v1/reviews*")));
    }
}
