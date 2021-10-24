package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
public class MoviesInfoController {

    private final Sinks.Many<MovieInfo> moviesInfoSink = Sinks.many().replay().latest(); // .latest() for the new client
    // publish only the latest data that's being added to this sink

    private final MoviesInfoService moviesInfoService;

    public MoviesInfoController(MoviesInfoService moviesInfoService) {
        this.moviesInfoService = moviesInfoService;
    }

    @PostMapping("/movies-info")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {
        return moviesInfoService
                .addMovieInfo(movieInfo)
                .doOnNext(savedMovieInfo -> moviesInfoSink.tryEmitNext(savedMovieInfo)) // publish this movie to the sink
                .log();
    }

    @GetMapping(value = "/movies-info/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> streamMovieInfo() {
        return moviesInfoSink.asFlux().log(); // here we are attaching the subscriber.
        // When the client connects to this endpoint, it is going to automatically subscribe to this sink.
    }

    @GetMapping("/movies-info")
    public Flux<MovieInfo> getAllMovieInfo(@RequestParam(value = "year", required = false) Integer year) {
        if (year != null) {
            return moviesInfoService.getMoviesInfoByYear(year).log();
        }
        return moviesInfoService.getAllMoviesInfo().log();
    }

    @GetMapping("/movies-info/{id}")
    public Mono<ResponseEntity<MovieInfo>> getMovieInfoById(@PathVariable String id) {
        return moviesInfoService.getMovieInfoById(id)
                .map(movieInfo -> ResponseEntity.ok().body(movieInfo))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @PutMapping("/movies-info/{id}")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@PathVariable String id, @RequestBody MovieInfo updatedMovieInfo) {
        return moviesInfoService.updateMovieInfo(id, updatedMovieInfo)
                .map(movieInfo -> ResponseEntity.ok().body(movieInfo))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @DeleteMapping("/movies-info/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieInfo(@PathVariable String id) {
        return moviesInfoService.deleteMovieInfo(id).log();
    }
}
