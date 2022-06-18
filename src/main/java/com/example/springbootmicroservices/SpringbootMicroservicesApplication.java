package com.example.springbootmicroservices;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;


import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class SpringbootMicroservicesApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootMicroservicesApplication.class, args);
	}

}

@Component
class Router {
	@Bean
	public RouterFunction<ServerResponse> route() {
		return RouterFunctions.route(
				GET("/hello"), Handler::hello);
	}
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Hello {
	private int id;
	private String message;
}

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class HelloException extends RuntimeException {
	public HelloException() {
		super();
	}
	public HelloException(String message) {
		super(message);
	}
}

@Component
@Slf4j
class Handler {
	private static final String SUCCESS = "success";
	private static final String FAILURE = "failure";
	public static Mono<ServerResponse> hello(ServerRequest serverRequest) {
		var status = serverRequest.queryParam("status").orElse(SUCCESS);
		if (SUCCESS.equalsIgnoreCase(status)) {
			return ok()
					.body(
							Mono.just(Hello.builder().id(1).message("Hello to Hello").build())
									.doOnNext(l -> log.info("{} response invoked : {}", SUCCESS, l))
									.doFinally(l -> log.info("I'm done! : {}", l))
							, Hello.class);
		} else {
			return ok()
					//.body(Mono.error(new HelloException("Hello Error")), Hello.class);
					.body(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,"Hello Error")),Hello.class);
		}
	}
}

