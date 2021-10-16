package com.u4.controller;

import com.u4.domain.MovieInfo;
import com.u4.service.MoviesInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
class MoviesInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MoviesInfoService moviesInfoServiceMock;

    private static final String MOVIES_INFO_URL = "/v1/movies-info";

    @Test
    void getAllMoviesInfo() {
        List<MovieInfo> movieInfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("jkr", "Joker",
                        2019, List.of("Joaquin Phoenix", "Robert De Niro"), LocalDate.parse("2019-08-31")));

        when(moviesInfoServiceMock.getAllMoviesInfo()).thenReturn(Flux.fromIterable(movieInfos));

        webTestClient
                .get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById() {
        String movieInfoId = "jkr";
        MovieInfo movieInfo = new MovieInfo("jkr", "Joker",
                2019, List.of("Joaquin Phoenix", "Robert De Niro"), LocalDate.parse("2019-08-31"));
        when(moviesInfoServiceMock.getMovieInfoById(movieInfoId)).thenReturn(Mono.just(movieInfo));

        webTestClient
                .get()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Joker");
//                .expectBody(MovieInfo.class)
//                .consumeWith(movieInfoEntityExchangeResult -> {
//                    MovieInfo responseBody = movieInfoEntityExchangeResult.getResponseBody();
//                    assert responseBody != null;
//                    assertEquals(movieInfo.getName(), responseBody.getName());
//                });
    }

    @Test
    void addMoviesInfo() {
        MovieInfo movieInfo = new MovieInfo(null, "Dark Knight Rises",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        when(moviesInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(
                new MovieInfo("mockId", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))
        ));

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
                    assertEquals("mockId", responseBody.getMovieInfoId());
                    assertEquals(movieInfo.getName(), responseBody.getName());
                });
    }

    @Test
    void updateMovieInfo() {
        String movieInfoId = "jkr";
        MovieInfo updatedMovieInfo = new MovieInfo(null, "Joker part 2",
                2029, List.of("Joaquin Phoenix", "Robert De Niro"), LocalDate.parse("2029-08-31"));

        when(moviesInfoServiceMock.updateMovieInfo(anyString(), isA(MovieInfo.class))).thenReturn(Mono.just(
                new MovieInfo(movieInfoId, "Joker part 2",
                        2029, List.of("Joaquin Phoenix", "Robert De Niro"), LocalDate.parse("2029-08-31"))
        ));

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
        when(moviesInfoServiceMock.deleteMovieInfo(anyString())).thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void addMoviesInfoValidation() {
        MovieInfo movieInfo = new MovieInfo(null, "", -2012, List.of(""), LocalDate.parse("2012-07-20"));

        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    String responseBody = stringEntityExchangeResult.getResponseBody();
                    String expectedErrorMessage = "moveInfo.name must be present,movieInfo.cast must be present,movieInfo.year must be a positive value";
                    assert responseBody != null;
                    assertEquals(expectedErrorMessage, responseBody);
                });
    }
}
