package com.ps.demo;

import java.util.Date;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class ReactiveSpringClient {

@Bean
WebClient client() {
	return WebClient.builder().filter(ExchangeFilterFunctions.basicAuthentication("krish", "p"))
			.baseUrl("http://localhost:8080/musics").build();
}
@Bean
CommandLineRunner demo(WebClient client) {
	return args ->{
		 client.get()
			  .retrieve()
			  .bodyToFlux(Music.class)
			  .filter(music -> music.getTitle().equalsIgnoreCase("Ruk jana nahin"))
			  .flatMap(music -> client.get()
					  	.uri("/{id}/event",music.getId())
					  	.retrieve()
					  	.bodyToFlux(MusicEvent.class))
					  	.subscribe(System.out::println);
					   
	 
};
}

	public static void main(String[] args) {
		SpringApplication.run(ReactiveSpringClient.class, args);
	}

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

class Music {
	private String id;
	private String title;

	public Music() {
		super();
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