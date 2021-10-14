package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    // publisher
    public Flux<String> namesFlux() { // flux deals with multiple elements
        // db or remote service call
        return Flux.fromIterable(List.of("nandu", "gopal", "chan"))
                .log(); //logs every event between publisher and subscriber communication
    }

    public Mono<String> nameMono() { // mono deals with one element
        return Mono.just("nandu")
                .log();
    }

    public Flux<String> namesFluxMap(int stringLength) {
        // filter the string whose length is greater than 3
        return Flux.fromIterable(List.of("nandu", "gopal", "chan"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .map(s -> s.length() + "-" + s)
                .log();
    }

    public Mono<List<String>> namesMonoFlatMap(int stringLength) {
        return Mono.just("nanda")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(this::splitStringMono) // Mono<List of N, A, N, D, A>
                .log();
    }

    private Mono<List<String>> splitStringMono(String s) {
        String[] charArray = s.split("");
        List<String> charList = List.of(charArray);
        return Mono.just(charList);
    }

    public Flux<String> namesMonoFlatMapMany(int stringLength) {
        return Mono.just("nanda")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMapMany(this::splitString) // should return Flux<T>
                .log();
    }

    public Flux<String> namesFluxFlatMap(int stringLength) {
        return Flux.fromIterable(List.of("nanda", "gopal", "chan"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                // NANDA, GOPAL -> N, A, N, D, A, G, O, P, A, L
                .flatMap(this::splitString)
                .log();
    }

    // NANDA -> Flux(N,A,N,D,A)
    public Flux<String> splitString(String name) {
        String[] charArray = name.split("");
        return Flux.fromArray(charArray);
    }

    public Flux<String> namesFluxFlatMapAsync(int stringLength) {
        return Flux.fromIterable(List.of("nanda", "gopal", "chan"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(this::splitStringWithDelay) // flatMap() used for asynchronous transformation.
                // if ordering matters then don't use flatMap() in this case. Use it with transformations that returns Publisher.
                .log();
    }

    public Flux<String> splitStringWithDelay(String name) {
        String[] charArray = name.split("");
        int delay = new Random().nextInt(1000);
        return Flux.fromArray(charArray).delayElements(Duration.ofMillis(delay)); // introduces a delay for each and every element that is being emitted
    }

    public Flux<String> namesFluxConcatMapAsync(int stringLength) {
        return Flux.fromIterable(List.of("nanda", "gopal", "chan"))
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .concatMap(this::splitStringWithDelay) // preserves the order when compared to flatMap()
                .log();
    }

    public Flux<String> namesFluxTransform(int stringLength) {
        Function<Flux<String>, Flux<String>> filterMap = name -> name.map(String::toUpperCase)
                .filter(s -> s.length() > stringLength);

        return Flux.fromIterable(List.of("nanda", "gopal", "chan"))
                .transform(filterMap)
                // NANDA, GOPAL -> N, A, N, D, A, G, O, P, A, L
                .flatMap(this::splitString)
                .defaultIfEmpty("default")
                .log();
    }

    public Flux<String> namesFluxTransformSwitchIfEmpty(int stringLength) {
        Function<Flux<String>, Flux<String>> filterMap = name -> name.map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(this::splitString);

        Flux<String> defaultFlux = Flux.just("default").transform(filterMap);

        return Flux.fromIterable(List.of("nanda", "gopal", "chan"))
                .transform(filterMap)
                // NANDA, GOPAL -> N, A, N, D, A, G, O, P, A, L
                .switchIfEmpty(defaultFlux) // "D", "E", "F", "A", "U", "L", "T"
                .log();
    }

    public Flux<String> namesFluxImmutability() {
        Flux<String> namesFlux = Flux.fromIterable(List.of("nandu", "gopal", "chan"));
        namesFlux.map(String::toUpperCase); // won't work
        return namesFlux;
    }

    public Flux<String> exploreConcat() {
        // these values will be coming from DBs or remote APIs in the real world
        Flux<String> abcFlux = Flux.just("A", "B", "C");
        Flux<String> defFlux = Flux.just("D", "E", "F");
        return Flux.concat(abcFlux, defFlux).log(); // concat() subscribes to the Publisher in a sequence
    }

    public Flux<String> exploreConcatWith() {
        Flux<String> abcFlux = Flux.just("A", "B", "C");
        Flux<String> defFlux = Flux.just("D", "E", "F");
        return abcFlux.concatWith(defFlux).log();
    }

    public Flux<String> exploreConcatWithMono() {
        // use case:- single element from two different services and then use and create a Flux with concatWith()
        Mono<String> aMono = Mono.just("A");
        Mono<String> bMono = Mono.just("B");
        return aMono.concatWith(bMono).log(); // creates a Flux<T> = Flux of A, B
    }

    public Flux<String> exploreMerge() {
        // in reality we would be getting these Fluxes from a remote service call or a DB.
        Flux<String> abcFlux = Flux.just("A", "B", "C").delayElements(Duration.ofMillis(100));
        Flux<String> defFlux = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(125));
        return Flux.merge(abcFlux, defFlux).log(); // Publishers are subscribed eagerly and the merge happens in an interleaved fashion
    }

    public Flux<String> exploreMergeWith() {
        Flux<String> abcFlux = Flux.just("A", "B", "C").delayElements(Duration.ofMillis(100));
        Flux<String> defFlux = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(125));
        return abcFlux.mergeWith(defFlux).log();
    }

    public Flux<String> exploreMergeWithMono() {
        Mono<String> aMono = Mono.just("A");
        Mono<String> bMono = Mono.just("B");
        return aMono.mergeWith(bMono).log(); // A, B
    }

    public Flux<String> exploreMergeSequential() {
        Flux<String> abcFlux = Flux.just("A", "B", "C").delayElements(Duration.ofMillis(100));
        Flux<String> defFlux = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(125));
        return Flux.mergeSequential(abcFlux, defFlux).log(); // Publishers are subscribed eagerly but the merge happens in a sequence
    }

    public Flux<String> exploreZip() {
        Flux<String> abcFlux = Flux.just("A", "B", "C");
        Flux<String> defFlux = Flux.just("D", "E", "F");
        return Flux.zip(abcFlux, defFlux, (first, second) -> first + second).log(); // Publishers are subscribed eagerly, but it waits for all the publishers involved in the transformation to emit one element
    }

    public Flux<String> exploreZip2() {
        Flux<String> abcFlux = Flux.just("A", "B", "C");
        Flux<String> defFlux = Flux.just("D", "E", "F");
        Flux<String> _123Flux = Flux.just("1", "2", "3");
        Flux<String> _456Flux = Flux.just("4", "5", "6");
        return Flux.zip(abcFlux, defFlux, _123Flux, _456Flux)
                .map(t4 -> t4.getT1() + t4.getT2() + t4.getT3() + t4.getT4())
                .log(); // AD14, BE25, CF36
    }

    public Flux<String> exploreZipWith() {
        Flux<String> abcFlux = Flux.just("A", "B", "C");
        Flux<String> defFlux = Flux.just("D", "E", "F");
        return abcFlux.zipWith(defFlux, (first, second) -> first + second).log(); // AD, BE, CF
    }

    public Mono<String> exploreZipWithMono() {
        Mono<String> aMono = Mono.just("A");
        Mono<String> bMono = Mono.just("B");
        return aMono.zipWith(bMono)
                .map(t2 -> t2.getT1() + t2.getT2())
                .log(); // AB
    }

    public static void main(String[] args) {
        FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();
        fluxAndMonoGeneratorService.namesFlux()
                .subscribe(name -> System.out.println("Flux name is: " + name));

        fluxAndMonoGeneratorService.nameMono()
                .subscribe(name -> System.out.println("Mono name is: " + name));
    }
}
