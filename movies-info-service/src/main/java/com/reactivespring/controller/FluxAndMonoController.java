package com.reactivespring.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class FluxAndMonoController {

    @GetMapping("/flux")
    public Flux<Integer> flux() {
        return Flux.just(1, 2, 3).log();
    }

    @GetMapping("/mono")
    public Mono<String> mono() {
        return Mono.just("Hello nandu").log();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Long> stream() {
        // streaming endpoints continuously sends updates to the clients as the new data arrives (similar to Server Sent Events[SSE]) eg:- Stock Tickers, Realtime updates of sports events
        return Flux.interval(Duration.ofSeconds(1)).log(); // every second it will continuously send the value to the client.
    }
}
