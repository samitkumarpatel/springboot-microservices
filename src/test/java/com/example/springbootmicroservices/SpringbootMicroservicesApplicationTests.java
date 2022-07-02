package com.example.springbootmicroservices;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.RouteMatcher;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
class SpringbootMicroservicesApplicationTests {

	@Test
	void contextLoads() {
	}

}

@ExtendWith(SpringExtension.class)
class HandlerTest {
	@MockBean
	ServerRequest request;
	private Handler handler;

	private Validate validate;
	@MockBean
	private UserValidator userValidator;

	@BeforeEach
	void setUp(){

	}
	@Test
	@DisplayName("Check Test Integration Test")
	void test(){
		assertEquals(1,1);
	}
	@Test
	@DisplayName("EMAIL Notification Test")
	void emailNotificationTest() {
		handler = new Handler(new EmailNotification(), new Validate(userValidator));
		when(request.queryParam("to")).thenReturn(Optional.of("samit"));

		StepVerifier.create(handler.sendNotification(request)).consumeNextWith(msg -> {
			assertEquals(HttpStatus.OK, msg.statusCode());
		}).verifyComplete();
	}

	@Test
	@DisplayName("SMS Notification Test")
	void smsNotificationTest() {
		handler = new Handler(new SmsNotification(), new Validate(userValidator));
		when(request.queryParam("to")).thenReturn(Optional.of("samit"));

		StepVerifier.create(handler.sendNotification(request)).consumeNextWith(msg -> {
			assertEquals(HttpStatus.OK, msg.statusCode());
		}).verifyComplete();
	}

	@Test
	@DisplayName("Smooth Numbers Test")
	void validateNumberTest() {
		when(userValidator.validateUser(anyString())).thenReturn(Mono.just("SUCCESS"));
		handler = new Handler(new SmsNotification(), new Validate(userValidator));
		when(request.queryParam("user")).thenReturn(Optional.of("samit"));
		when(request.queryParam("age")).thenReturn(Optional.of("15"));
		StepVerifier.create(
			handler.numbers(request)
		).consumeNextWith(number -> {
			assertEquals(HttpStatus.OK, number.statusCode());
		}).verifyComplete();
	}

	//TODO fix this
	@Test
	@DisplayName("Bumps Numbers Test")
	@Disabled
	void validateNumberErrorTest() {
		when(userValidator.validateUser(anyString())).thenReturn(Mono.just("SUCCESS"));
		handler = new Handler(new SmsNotification(), new Validate(userValidator));
		when(request.queryParam("user")).thenReturn(Optional.of("samit"));
		when(request.queryParam("age")).thenReturn(Optional.of("9"));
		/*
		Not sure ,this will work or not?
		assertThrows(ValidationException.class,()->handler.numbers(request));
		 */

		StepVerifier.create(
				handler.numbers(request)
		).consumeNextWith(number -> {
			assertEquals(HttpStatus.BAD_REQUEST, number.statusCode());
		}).verifyComplete();
	}
}

@ExtendWith(SpringExtension.class)
class UserValidatorServiceTest {
	private UserValidator userValidator;
	@BeforeEach
	void setup() {
		userValidator = new UserValidator();
	}
	@Test
	@DisplayName("User Validate Test")
	void userValidateTest() {
		StepVerifier.create(
			userValidator.validateUser("samit")
		).consumeNextWith(status -> {
			assertEquals("SUCCESS", status);
		}).verifyComplete();
	}
}

@ExtendWith(SpringExtension.class)
class ValidateServiceTest {
	@MockBean
	private UserValidator userValidator;
	private Validate validate;
	@BeforeEach
	void setup() {
		when(userValidator.validateUser(anyString())).thenReturn(Mono.just("SUCCESS"));
		validate = new Validate(userValidator);
	}
	@Test
	@DisplayName("Validate Positive Test")
	void validatePositiveTest() {
		StepVerifier.create(
				validate.validate("samit", 15)
		).consumeNextWith(bool -> {
			assertEquals(Boolean.TRUE, bool);
		}).verifyComplete();
	}

	@Test
	@DisplayName("Validate Negative Test")
	void validateNegativeTest() {
		/*
			This assertion will not work - as this is reactive Flow
			assertThrows(ValidationException.class,()->validate.validate("samit", 9));
		 */

		StepVerifier.create(
				validate.validate("samit", 9)
		).expectError(ValidationException.class);
	}
}

@SpringBootTest
class RouterTest {
	private WebTestClient webTestClient;
	@Autowired
	private ApplicationContext context;

	@BeforeEach
	void setUp() {
		webTestClient = WebTestClient.bindToApplicationContext(context).build();
	}
	@Test
	@DisplayName("/numbers success test")
	void numberRouteTest() {
		webTestClient.get().uri(uriBuilder -> uriBuilder
						.path("/numbers")
						.queryParam("user", "samit")
						.queryParam("age","11").build())
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.json("11");
	}

	@Test
	@DisplayName("/numbers failure test")
	void numberRouteTest01() {
		webTestClient.get().uri(uriBuilder -> uriBuilder
						.path("/numbers")
						.queryParam("user", "samit")
						.queryParam("age","8").build())
				.exchange()
				.expectStatus().is4xxClientError()
				.expectBody()
				.jsonPath("$.message").isEqualTo("not valid");
	}
}

//@SpringBootTest(properties = {"notification.service=email"})
//@SpringBootConfiguration
//@TestPropertySource(properties = "notification.service=email")
//@TestPropertySource(properties = {"notification.service=email", "name=ABC"})
class IntegrationTest {
	// 1.) To set the @value field you can use Springs ReflectionTestUtils - it has a method setField to set private fields

	/* 2.)
	@BeforeClass
	public static void beforeClass() {
		System.setProperty("some.property", "<value>");
	}
	 */
}