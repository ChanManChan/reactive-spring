package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

class FluxAndMonoGeneratorServiceTest {
    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

    @Test
    void namesFlux() {
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFlux();
        StepVerifier.create(namesFlux)
                //.expectNext("nandu", "gopal", "chan")
                //.expectNextCount(3)
                .expectNext("nandu")
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void namesFluxMap() {
        int stringLength = 4;
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFluxMap(stringLength);
        StepVerifier.create(namesFlux)
                .expectNext("5-NANDU", "5-GOPAL")
                .verifyComplete();
    }

    @Test
    void namesFluxImmutability() {
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFluxImmutability();
        StepVerifier.create(namesFlux)
                .expectNext("nandu", "gopal", "chan")
                .verifyComplete();
    }

    @Test
    void namesFluxFlatMap() {
        int stringLength = 4;
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFluxFlatMap(stringLength);
        StepVerifier.create(namesFlux)
                .expectNext("N", "A", "N", "D", "A", "G", "O", "P", "A", "L")
                .verifyComplete();
    }

    @Test
    void namesFluxFlatMapAsync() {
        int stringLength = 4;
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFluxFlatMapAsync(stringLength);  // returns random order but faster process
        StepVerifier.create(namesFlux)
                //.expectNext("N", "A", "N", "D", "A", "G", "O", "P", "A", "L")
                .expectNextCount(10)
                .verifyComplete();
    }

    @Test
    void namesFluxConcatMapAsync() {
        int stringLength = 4;
        Flux<String> namesFlux = fluxAndMonoGeneratorService.namesFluxConcatMapAsync(stringLength);  // order is preserved but slower process
        StepVerifier.create(namesFlux)
                .expectNext("N", "A", "N", "D", "A", "G", "O", "P", "A", "L")
                //.expectNextCount(10)
                .verifyComplete();
    }

    @Test
    void namesMonoFlatMap() {
        int stringLength = 4;
        Mono<List<String>> listMono = fluxAndMonoGeneratorService.namesMonoFlatMap(stringLength);
        StepVerifier.create(listMono)
                .expectNext(List.of("N", "A", "N", "D", "A"))
                .verifyComplete();
    }

    @Test
    void namesMonoFlatMapMany() {
        int stringLength = 4;
        Flux<String> stringFlux = fluxAndMonoGeneratorService.namesMonoFlatMapMany(stringLength);
        StepVerifier.create(stringFlux)
                .expectNext("N", "A", "N", "D", "A")
                .verifyComplete();
    }

    @Test
    void namesFluxTransform() {
        int stringLength = 4;
        Flux<String> stringFlux = fluxAndMonoGeneratorService.namesFluxTransform(stringLength);
        StepVerifier.create(stringFlux)
                .expectNext("N", "A", "N", "D", "A", "G", "O", "P", "A", "L")
                .verifyComplete();
    }

    @Test
    void namesFluxTransformDefaultValues() {
        int stringLength = 6;
        Flux<String> stringFlux = fluxAndMonoGeneratorService.namesFluxTransform(stringLength);
        StepVerifier.create(stringFlux)
                .expectNext("default")
                .verifyComplete();
    }

    @Test
    void namesFluxTransformSwitchIfEmpty() {
        int stringLength = 6;
        Flux<String> stringFlux = fluxAndMonoGeneratorService.namesFluxTransformSwitchIfEmpty(stringLength);
        StepVerifier.create(stringFlux)
                .expectNext("D", "E", "F", "A", "U", "L", "T")
                .verifyComplete();
    }

    @Test
    void exploreConcat() {
        Flux<String> concatFlux = fluxAndMonoGeneratorService.exploreConcat();
        StepVerifier.create(concatFlux)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void exploreConcatWith() {
        Flux<String> concatFlux = fluxAndMonoGeneratorService.exploreConcatWith();
        StepVerifier.create(concatFlux)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void exploreConcatWithMono() {
        Flux<String> concatFlux = fluxAndMonoGeneratorService.exploreConcatWithMono();
        StepVerifier.create(concatFlux)
                .expectNext("A", "B")
                .verifyComplete();
    }

    @Test
    void exploreMerge() {
        Flux<String> mergeFlux = fluxAndMonoGeneratorService.exploreMerge();
        StepVerifier.create(mergeFlux)
                .expectNext("A", "D", "B", "E", "C", "F")
                .verifyComplete();
    }

    @Test
    void exploreMergeWith() {
        Flux<String> mergeFlux = fluxAndMonoGeneratorService.exploreMergeWith();
        StepVerifier.create(mergeFlux)
                .expectNext("A", "D", "B", "E", "C", "F")
                .verifyComplete();
    }

    @Test
    void exploreMergeWithMono() {
        Flux<String> mergeFlux = fluxAndMonoGeneratorService.exploreMergeWithMono();
        StepVerifier.create(mergeFlux)
                .expectNext("A", "B")
                .verifyComplete();
    }

    @Test
    void exploreMergeSequential() {
        Flux<String> mergeFlux = fluxAndMonoGeneratorService.exploreMergeSequential();
        StepVerifier.create(mergeFlux)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void exploreZip() {
        Flux<String> mergeFlux = fluxAndMonoGeneratorService.exploreZip();
        StepVerifier.create(mergeFlux)
                .expectNext("AD", "BE", "CF")
                .verifyComplete();
    }

    @Test
    void exploreZip2() {
        Flux<String> mergeFlux = fluxAndMonoGeneratorService.exploreZip2();
        StepVerifier.create(mergeFlux)
                .expectNext("AD14", "BE25", "CF36")
                .verifyComplete();
    }

    @Test
    void exploreZipWith() {
        Flux<String> mergeFlux = fluxAndMonoGeneratorService.exploreZipWith();
        StepVerifier.create(mergeFlux)
                .expectNext("AD", "BE", "CF")
                .verifyComplete();
    }

    @Test
    void exploreZipWithMono() {
        Mono<String> mergeMono = fluxAndMonoGeneratorService.exploreZipWithMono();
        StepVerifier.create(mergeMono)
                .expectNext("AB")
                .verifyComplete();
    }
}