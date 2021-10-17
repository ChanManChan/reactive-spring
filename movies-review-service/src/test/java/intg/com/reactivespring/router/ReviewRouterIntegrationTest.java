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
                new Review(null, 3L, "Daniel Craig's richly layered performance in No Time to Die just might attract Oscar's attention...", 5.0)
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
}