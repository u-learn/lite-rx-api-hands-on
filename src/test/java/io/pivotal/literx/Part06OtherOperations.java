package io.pivotal.literx;

import io.pivotal.literx.domain.User;
import io.pivotal.literx.repository.ReactiveRepository;
import io.pivotal.literx.repository.ReactiveUserRepository;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.test.TestSubscriber;

/**
 * Learn how to use various other operators.
 *
 * @author Sebastien Deleuze
 */
public class Part06OtherOperations {

	final static User MARIE = new User("mschrader", "Marie", "Schrader");
	final static User MIKE = new User("mehrmantraut", "Mike", "Ehrmantraut");

//========================================================================================

	@Test
	public void zipFirstNameAndLastName() {
		Flux<String> usernameFlux = Flux.just(User.SKYLER.getUsername(), User.JESSE.getUsername(), User.WALTER.getUsername(), User.SAUL.getUsername());
		Flux<String> firstnameFlux = Flux.just(User.SKYLER.getFirstname(), User.JESSE.getFirstname(), User.WALTER.getFirstname(), User.SAUL.getFirstname());
		Flux<String> lastnameFlux = Flux.just(User.SKYLER.getLastname(), User.JESSE.getLastname(), User.WALTER.getLastname(), User.SAUL.getLastname());
		Flux<User> userFlux = userFluxFromStringFlux(usernameFlux, firstnameFlux, lastnameFlux);
		TestSubscriber<User> ts = new TestSubscriber<>();
		ts.bindTo(userFlux).assertValues(User.SKYLER, User.JESSE, User.WALTER, User.SAUL);
	}

	// TODO Create a Flux of user from a Flux of user name, first name and last name.
	Flux<User> userFluxFromStringFlux(Flux<String> usernameFlux, Flux<String> firstnameFlux, Flux<String> lastnameFlux) {
		return Flux.zip(usernameFlux, firstnameFlux, lastnameFlux).map(tuple -> new User(tuple.t1, tuple.t2, tuple.t3)); // TO BE REMOVED
	}

//========================================================================================

	@Test
	public void fastestMono() {
		ReactiveRepository<User> repository1 = new ReactiveUserRepository(MARIE);
		ReactiveRepository<User> repository2 = new ReactiveUserRepository(250, MIKE);
		Mono<User> mono = useFastestMono(repository1.findFirst(), repository2.findFirst());
		TestSubscriber<User> ts = new TestSubscriber<>();
		ts.bindTo(mono).await().assertValues(MARIE).assertComplete();

		repository1 = new ReactiveUserRepository(250, MARIE);
		repository2 = new ReactiveUserRepository(MIKE);
		mono = useFastestMono(repository1.findFirst(), repository2.findFirst());
		ts = new TestSubscriber<>();
		ts.bindTo(mono).await().assertValues(MIKE).assertComplete();
	}

	// TODO return the mono who return faster its value
	Mono<User> useFastestMono(Mono<User> mono1, Mono<User> mono2) {
		return Mono.any(mono1, mono2); // TO BE REMOVED
	}

//========================================================================================

	@Test
	public void fastestFlux() {
		ReactiveRepository<User> repository1 = new ReactiveUserRepository(MARIE, MIKE);
		ReactiveRepository<User> repository2 = new ReactiveUserRepository(250);
		Flux<User> flux = useFastestFlux(repository1.findAll(), repository2.findAll());
		TestSubscriber<User> ts = new TestSubscriber<>();
		ts.bindTo(flux).await().assertValues(MARIE, MIKE).assertComplete();

		repository1 = new ReactiveUserRepository(250, MARIE, MIKE);
		repository2 = new ReactiveUserRepository();
		flux = useFastestFlux(repository1.findAll(), repository2.findAll());
		ts = new TestSubscriber<>();
		ts.bindTo(flux).await().assertValues(User.SKYLER, User.JESSE, User.WALTER, User.SAUL).assertComplete();
	}

	// TODO return the flux who return faster the first value
	Flux<User> useFastestFlux(Flux<User> flux1, Flux<User> flux2) {
		return Flux.amb(flux1, flux2); // TO BE REMOVED
	}

//========================================================================================

	@Test
	public void end() {
		ReactiveRepository<User> repository = new ReactiveUserRepository();
		Mono<Void> end = endOfFlux(repository.findAll());
		TestSubscriber<Void> ts = new TestSubscriber<>();
		ts.bindTo(end).assertNotTerminated().await().assertNoValues().assertComplete();
	}

	// TODO Convert the input Flux<User> to a Mono<Void> that represents the complete signal of the flux
	Mono<Void> endOfFlux(Flux<User> flux) {
		return flux.after(); // TO BE REMOVED
	}


//========================================================================================

	@Test
	public void monoWithValueInsteadOfError() {
		Mono<User> mono = betterCallSaulForBogusMono(Mono.error(new IllegalStateException()));
		TestSubscriber<User> ts = new TestSubscriber<>();
		ts.bindTo(mono).assertValues(User.SAUL).assertComplete();

		mono = betterCallSaulForBogusMono(Mono.just(User.SKYLER));
		ts = new TestSubscriber<>();
		ts.bindTo(mono).assertValues(User.SKYLER).assertComplete();
	}

	// TODO Return a Mono containing Saul when an error occurs in the input Mono, else do not change the input Mono.
	Mono<User> betterCallSaulForBogusMono(Mono<User> mono) {
		return mono.otherwise(e -> Mono.just(User.SAUL)); // TO BE REMOVED
	}

//========================================================================================

	@Test
	public void fluxWithValueInsteadOfError() {
		Flux<User> flux = betterCallSaulForBogusFlux(Flux.error(new IllegalStateException()));
		TestSubscriber<User> ts = new TestSubscriber<>();
		ts.bindTo(flux).assertValues(User.SAUL).assertComplete();

		flux = betterCallSaulForBogusFlux(Flux.just(User.SKYLER, User.WALTER));
		ts = new TestSubscriber<>();
		ts.bindTo(flux).assertValues(User.SKYLER, User.WALTER).assertComplete();
	}

	// TODO Return a Flux containing Saul when an error occurs in the input Flux, else do not change the input Flux.
	Flux<User> betterCallSaulForBogusFlux(Flux<User> flux) {
		return flux.onErrorResumeWith(e -> Mono.just(User.SAUL)); // TO BE REMOVED
	}

}