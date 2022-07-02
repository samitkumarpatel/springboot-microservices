package com.example.springbootmicroservices;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

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
	public RouterFunction<ServerResponse> route(Handler handler) {
		return RouterFunctions
				.route(GET("/hello"), handler::hello)
				.andRoute(GET("/notification"), handler::sendNotification)
				.andRoute(GET("/numbers"),handler::numbers);
	}
}

@Component
@Slf4j
@RequiredArgsConstructor
class Handler {
	private static final String SUCCESS = "success";
	private static final String FAILURE = "failure";
	private final Notification notification;
	private final Validate validate;
	public Mono<ServerResponse> hello(ServerRequest request) {
		var status = request.queryParam("status").orElse(SUCCESS);
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

	public Mono<ServerResponse> sendNotification(ServerRequest request) {
		var to = "Samit";
		return ok().body(
					notification.send(to)
							.map(m -> m)
							.doOnNext(m -> log.info("Message sent Successfully to {}",to))
							.doOnError(e -> Mono.error(e))
				, Message.class);
	}

	public Mono<ServerResponse> numbers(ServerRequest request) {
		return ok().body(
				validate.validate(request.queryParam("user").orElse(""),Integer.valueOf(request.queryParam("age").orElse("10")))
						.flatMap(b -> {
							var x = 10;
							if(b) {
								log.info("TRUE: Just for debug purpose");
								++x;
							} else {
								log.info("FALSE: Just for debug purpose");
								--x;
							}
							return Mono.just(Integer.valueOf(x));
						})
				, Integer.class);
	}

}

interface Notification {
	Mono<Message> send(String to);
}

@Service
@ConditionalOnProperty(prefix = "notification", name = "service", havingValue = "email")
class EmailNotification implements Notification {
	@Override
	public Mono<Message> send(String to) {
		return Mono.just(
				Message.builder().msgId(0).message("EMAIL message").source(NotificationType.EMAIL).build()
		);
	}
}

@Service
@ConditionalOnProperty(prefix = "notification", name = "service", havingValue = "sms")
class SmsNotification implements Notification {
	@Override
	public Mono<Message> send(String to) {
		return Mono.just(
				Message.builder().msgId(1).message("SMS message").source(NotificationType.SMS).build()
		);
	}
}

@Service
class UserValidator {
	public Mono<String> validateUser(String userName) {
		if(userName.length() > 0 ) {
			return Mono.just("SUCCESS");
		} else {
			return Mono.error(new ForbiddenException("User Is not valid"));
		}
	}
}

@Service
@RequiredArgsConstructor
class Validate {
	private final UserValidator userValidator;
	public Mono<Boolean> validate(String userName, int age) {
		return userValidator.validateUser(userName)
				.flatMap(res -> ageEvaluation(age))
				.doOnNext(bool -> ageValidateAgain(bool));
	}

	private Mono<Boolean> ageEvaluation(int age) {
		return age > 10 ? Mono.just(Boolean.TRUE) : Mono.just(Boolean.FALSE);
	}

	private void ageValidateAgain(Boolean bool) {
		if(!bool) throw new ValidationException("not valid");
	}
}

@ResponseStatus(HttpStatus.FORBIDDEN)
class ForbiddenException extends RuntimeException {
	ForbiddenException(String message) {
		super(message);
	}
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
class ValidationException extends RuntimeException {
	ValidationException(String message) {
		super(message);
	}
}

@Data @Builder @NoArgsConstructor @AllArgsConstructor
class Hello {
	private int id;
	private String message;
}

enum NotificationType {
	EMAIL,
	SMS
}
@Data @Builder @NoArgsConstructor @AllArgsConstructor
class Message {
	private int msgId;
	private String message;
	private NotificationType source;
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



