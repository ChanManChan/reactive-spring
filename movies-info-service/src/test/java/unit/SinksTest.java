import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class SinksTest {
    @Test
    void sink() {
        Sinks.Many<Integer> replaySink = Sinks.many().replay().all();

        replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        Flux<Integer> integerFlux1 = replaySink.asFlux();
        integerFlux1.subscribe(integer -> System.out.println("Subscriber 1: " + integer));

        Flux<Integer> integerFlux2 = replaySink.asFlux();
        integerFlux2.subscribe(integer -> System.out.println("Subscriber 2: " + integer));

        replaySink.tryEmitNext(3);
        // as long as you are publishing events, the subscribers are going to receive those events

        Flux<Integer> integerFlux3 = replaySink.asFlux();
        integerFlux3.subscribe(integer -> System.out.println("Subscriber 3:  " + integer)); // anytime you add a new subscriber, it's going to replay all the events that happened with this sink
    }

    @Test
    void sinkMultiCast() {
        Sinks.Many<Integer> multicast = Sinks.many().multicast().onBackpressureBuffer();

        multicast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multicast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);
        multicast.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);

        Flux<Integer> integerFlux1 = multicast.asFlux();
        integerFlux1.subscribe(integer -> System.out.println("Subscriber 1: " + integer)); // first subscriber will receive all the event's before its subscription plus every event emitted later on

        multicast.emitNext(4, Sinks.EmitFailureHandler.FAIL_FAST);

        Flux<Integer> integerFlux2 = multicast.asFlux();
        integerFlux2.subscribe(integer -> System.out.println("Subscriber 2: " + integer)); // second subscriber onwards till nth subscriber will receive only the events emitted after its subscription

        Flux<Integer> integerFlux3 = multicast.asFlux();
        integerFlux3.subscribe(integer -> System.out.println("Subscriber 3: " + integer));

        multicast.emitNext(5, Sinks.EmitFailureHandler.FAIL_FAST);

        Flux<Integer> integerFlux4 = multicast.asFlux();
        integerFlux4.subscribe(integer -> System.out.println("Subscriber 4: " + integer));

        multicast.emitNext(6, Sinks.EmitFailureHandler.FAIL_FAST);
        multicast.emitNext(7, Sinks.EmitFailureHandler.FAIL_FAST);

        Flux<Integer> integerFlux5 = multicast.asFlux();
        integerFlux5.subscribe(integer -> System.out.println("Subscriber 5: " + integer));

        multicast.emitNext(8, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    @Test
    void sinkUniCast() {
        Sinks.Many<Integer> uniCast = Sinks.many().unicast().onBackpressureBuffer(); // cannot have multiple subscribers attached to a uniCast sink

        uniCast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        uniCast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);
        uniCast.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);

        Flux<Integer> integerFlux1 = uniCast.asFlux();
        integerFlux1.subscribe(integer -> System.out.println("Subscriber 1: " + integer));

        uniCast.emitNext(4, Sinks.EmitFailureHandler.FAIL_FAST);
    }
}
