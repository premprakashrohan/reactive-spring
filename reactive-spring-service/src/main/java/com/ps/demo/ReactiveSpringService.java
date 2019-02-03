package com.ps.demo;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.time.Duration;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class ReactiveSpringService {
	@Bean
	RouterFunction<ServerResponse> routerFunction(MusicHandler handler) {
		return route(GET("/musics"),  handler::all)
				.andRoute(GET("/musics/{id}"), handler::getById)
				.andRoute(GET("/musics/{id}/event"), handler::getEventById);
	}

	public static void main(String[] args) {
		SpringApplication.run(ReactiveSpringService.class, args);
	}

}
/*
@EnableWebFluxSecurity
@Configuration
class SecurityConfiguration{
	//@Bean
	ReactiveUserDetailsService authentication() {
		return new MapReactiveUserDetailsService(
				User.withUsername("prem").password("p").roles("USERS").build(),
				User.withUsername("krish").password("p").roles("ADMIN").build());
	}
	
	
	//@Bean
	SecurityWebFilterChain autherization (HttpSecurity security) {
		return null;
		//return security.httpBasic().and().build();
	}
}
*/



@Component
class MusicHandler {
	@Autowired
	MusicService musicService;

	public Mono<ServerResponse> getById(ServerRequest serverRequest) {
		return ServerResponse.ok().body(musicService.findMusicById(serverRequest.pathVariable("id")), Music.class);
	}

	public Mono<ServerResponse> all(ServerRequest serverRequest) {
		return ServerResponse.ok().body(musicService.findAllMusic(), Music.class);
	}

	public Mono<ServerResponse> getEventById(ServerRequest serverRequest) {
		return ServerResponse.ok()
				.contentType(MediaType.TEXT_EVENT_STREAM).body(musicService.getMusicEvent(serverRequest.pathVariable("id")), MusicEvent.class);
	}
}

@Component
class ApplicationInitializer implements ApplicationRunner {
	@Autowired
	MusicRepository repo;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		Flux<Music> musicFlux = Flux
				.just("Main Tera hero", "Jadu teri nazar", "Mere hathon mein nau nau", "Ruk jana nahin")
				.map(m -> new Music(null, m)).flatMap(repo::save);
		repo.deleteAll().thenMany(musicFlux).thenMany(repo.findAll()).subscribe(System.out::println);
	}

}

/*
 * @RestController
 * 
 * @RequestMapping("/musics") class MusicController {
 * 
 * @Autowired MusicService musicService;
 * 
 * @GetMapping("/{id}") public Mono<Music> getById(@PathVariable String id) {
 * return this.musicService.findMusicById(id); }
 * 
 * @GetMapping public Flux<Music> all() { return
 * this.musicService.findAllMusic(); }
 * 
 * @GetMapping(value="/{id}/event",produces=MediaType.TEXT_EVENT_STREAM_VALUE )
 * public Flux<MusicEvent> getEventById(@PathVariable String id) { return
 * this.musicService.getMusicEvent(id); }
 * 
 * }
 */
@Service
class MusicService {
	@Autowired
	MusicRepository musicRepository;

	Mono<Music> findMusicById(String id) {
		return musicRepository.findById(id);
	}

	Flux<Music> findAllMusic() {
		return musicRepository.findAll();
	}

	Flux<MusicEvent> getMusicEvent(String id) {
		return Flux.<MusicEvent>generate(sink -> sink.next(new MusicEvent(id, new Date())))
				.delayElements(Duration.ofSeconds(2));
	}
}

interface MusicRepository extends ReactiveMongoRepository<Music, String> {

}

class MusicEvent {
	private String id;
	private Date date;

	public MusicEvent() {
		super();
	}

	public MusicEvent(String id, Date date) {
		super();
		this.id = id;
		this.date = date;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "MusicEvent [id=" + id + ", date=" + date + "]";
	}

}

@Document
class Music {
	@Id
	private String id;
	private String title;

	public Music() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Music(String id, String title) {
		super();
		this.id = id;
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return "Music [id=" + id + ", title=" + title + "]";
	}

}