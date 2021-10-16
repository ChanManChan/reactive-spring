package com.u4.controller;

import com.u4.domain.MovieInfo;
import com.u4.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerIntegrationTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    MovieInfoRepository movieInfoRepository;

    private static final String MOVIES_INFO_URL = "/v1/movies-info";

    @BeforeEach
    void setUp() {
        List<MovieInfo> movieInfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("jkr", "Joker",
                        2019, List.of("Joaquin Phoenix", "Robert De Niro"), LocalDate.parse("2019-08-31")));

        movieInfoRepository.saveAll(movieInfos).blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void addMovieInfo() {
        MovieInfo movieInfo = new MovieInfo(null, "Dark Knight Rises",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    MovieInfo responseBody = movieInfoEntityExchangeResult.getResponseBody();
                    assert responseBody != null;
                    assertNotNull(responseBody.getMovieInfoId());
                });
    }

    @Test
    void getAllMovieInfo() {
        webTestClient
                .get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(2);
    }

    @Test
    void getMovieInfoById() {
        String movieInfoId = "jkr";
        webTestClient
                .get()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name")
                .isEqualTo("Joker");
//                .expectBody(MovieInfo.class)
//                .consumeWith(movieInfoEntityExchangeResult -> {
//                    MovieInfo responseBody = movieInfoEntityExchangeResult.getResponseBody();
//                    assert responseBody != null;
//                    assertEquals(movieInfoId, responseBody.getMovieInfoId());
//                });
    }

    @Test
    void updateMovieInfo() {
        String movieInfoId = "jkr";
        MovieInfo updatedMovieInfo = new MovieInfo(null, "Joker part 2",
                2029, List.of("Joaquin Phoenix", "Robert De Niro"), LocalDate.parse("2029-08-31"));

        webTestClient
                .put()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .bodyValue(updatedMovieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    MovieInfo responseBody = movieInfoEntityExchangeResult.getResponseBody();
                    assert responseBody != null;
                    assertNotNull(responseBody.getMovieInfoId());
                    assertEquals(updatedMovieInfo.getName(), responseBody.getName());
                    assertEquals(updatedMovieInfo.getYear(), responseBody.getYear());
                });
    }

    @Test
    void deleteMovieInfo() {
        String movieInfoId = "jkr";

        MovieInfo movieInfo = movieInfoRepository.findById(movieInfoId).block();
        assert movieInfo != null;
        assertEquals(movieInfo.getMovieInfoId(), movieInfoId);

        webTestClient
                .delete()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNoContent();

        MovieInfo movieInfoDeleted = movieInfoRepository.findById(movieInfoId).block();
        assert movieInfoDeleted == null;
    }
}