package com.ps.demo;

import java.time.Duration;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class ReactiveSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveSpringApplication.class, args);
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

	Flux<MusicEvent> getMusicEvent (String id){
		return Flux.<MusicEvent>generate(sink -> sink.next(new MusicEvent(findMusicById(id),new Date())))
				.delayElements(Duration.ofSeconds(2));
	}
}

interface MusicRepository extends ReactiveMongoRepository<Music, String> {

}

class MusicEvent {
	private Mono<Music> music;
	private Date date;

	public MusicEvent() {
		super();
	}

	public MusicEvent(Mono<Music> music, Date date) {
		super();
		this.music = music;
		this.date = date;
	}

	public Mono<Music> getMusic() {
		return music;
	}

	public void setMusic(Mono<Music> music) {
		this.music = music;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "MusicEvent [music=" + music + ", date=" + date + "]";
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