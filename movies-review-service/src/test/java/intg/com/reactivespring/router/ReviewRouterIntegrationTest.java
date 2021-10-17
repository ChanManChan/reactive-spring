package com.reactivespring.router;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class ReviewRouterIntegrationTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactiveRepository reviewReactiveRepository;

    private static final String REVIEWS_URL = "/v1/reviews";

    @BeforeEach
    void setUp() {
        List<Review> reviews = List.of(
                new Review(null, 1L, "If you can tolerate some daft fun and a movie full of knowing glances then you'll be just fine. It's not a particularly good movie but I did have a good time with it.", 6.0),
                new Review(null, 2L, "Lamb's plodding and thematically vacant execution leaves little to grasp onto.", 4.0),
                new Review(null, 3L, "Daniel Craig's richly layered performance in No Time to Die just might attract Oscar's attention...", 5.0),
                new Review("jb", 3L, "Sophisticated entertainment, as well as poignant reflexion on bioweapons, fallible heroes, and love.", 8.0)
        );
        reviewReactiveRepository.saveAll(reviews).blockLast();
    }

    @AfterEach
    void tearDown() {
        reviewReactiveRepository.deleteAll().block();
    }

    @Test
    void addReview() {
        Review review = new Review(null, 4L, "...a hit-and-miss endeavor that fares best in its striking, engrossing opening stretch...", 2.5);
        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    Review responseBody = reviewEntityExchangeResult.getResponseBody();
                    assert responseBody != null;
                    assert responseBody.getReviewId() != null;
                    assertEquals(responseBody.getComment(), review.getComment());
                });
    }

    @Test
    void getAllReviews() {
        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .consumeWith(listEntityExchangeResult -> {
                    List<Review> responseBody = listEntityExchangeResult.getResponseBody();
                    assert responseBody != null;
                    assert responseBody.size() == 4;
                });
    }

    @Test
    void getAllReviewsByMovieInfoId() {
        URI uri = UriComponentsBuilder.fromUriString(REVIEWS_URL)
                .queryParam("movieInfoId", 3L)
                .buildAndExpand().toUri();
        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .consumeWith(listEntityExchangeResult -> {
                    List<Review> responseBody = listEntityExchangeResult.getResponseBody();
                    assert responseBody != null;
                    assert responseBody.size() == 2;
                    responseBody.forEach(review -> assertEquals(3L, review.getMovieInfoId()));
                });
    }

    @Test
    void updateReview() {
        String reviewId = "jb";

        Review beforeUpdateReview = reviewReactiveRepository.findById(reviewId).block();
        assert beforeUpdateReview != null;
        assertEquals("Sophisticated entertainment, as well as poignant reflexion on bioweapons, fallible heroes, and love.", beforeUpdateReview.getComment());
        assertEquals(8.0, beforeUpdateReview.getRating());

        Review updatedReview = new Review(null, 3L, "It's hard to forgive No Time to Die for its sins because of the nature of its unevenness.", 4.0);
        webTestClient
                .put()
                .uri(REVIEWS_URL + "/{id}", reviewId)
                .bodyValue(updatedReview)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    Review responseBody = reviewEntityExchangeResult.getResponseBody();
                    assert responseBody != null;
                    assertEquals(responseBody.getReviewId(), reviewId);
                });

        Review afterUpdateReview = reviewReactiveRepository.findById(reviewId).block();
        assert afterUpdateReview != null;
        assertEquals(afterUpdateReview.getComment(), updatedReview.getComment());
        assertEquals(afterUpdateReview.getRating(), updatedReview.getRating());
    }

    @Test
    void deleteReview() {
        String reviewId = "jb";

        Review review = reviewReactiveRepository.findById(reviewId).block();
        assert review != null;
        assertEquals(review.getReviewId(), reviewId);

        webTestClient
                .delete()
                .uri(REVIEWS_URL + "/{id}", reviewId)
                .exchange()
                .expectStatus()
                .isNoContent();

        Review deletedReview = reviewReactiveRepository.findById(reviewId).block();
        assert deletedReview == null;
    }
}