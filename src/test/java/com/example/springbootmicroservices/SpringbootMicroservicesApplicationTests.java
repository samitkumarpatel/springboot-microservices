package com.example.springbootmicroservices;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.RouteMatcher;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
	Handler handler;
	@Test
	@DisplayName("Check Test Integration Test")
	void test(){
		assertEquals(1,1);
	}

	@Test
	@DisplayName("EMAIL Notification Test")
	void emailNotificationTest() {
		handler = new Handler(new EmailNotification());
		when(request.queryParam("to")).thenReturn(Optional.of("samit"));

		StepVerifier.create(handler.sendNotification(request)).consumeNextWith(msg -> {
			assertEquals(HttpStatus.OK, msg.statusCode());
		}).verifyComplete();
	}

	@Test
	@DisplayName("SMS Notification Test")
	void smsNotificationTest() {
		handler = new Handler(new SmsNotification());
		when(request.queryParam("to")).thenReturn(Optional.of("samit"));

		StepVerifier.create(handler.sendNotification(request)).consumeNextWith(msg -> {
			assertEquals(HttpStatus.OK, msg.statusCode());
		}).verifyComplete();
	}
}

//@SpringBootTest(properties = {"notification.service=email"})

//@SpringBootConfiguration
//@TestPropertySource(properties = "notification.service=email")
//@TestPropertySource(properties = {"notification.service=email", "name=ABC"})
class HandlerIntegrationTest {
	// 1.) To set the @value field you can use Springs ReflectionTestUtils - it has a method setField to set private fields

	/* 2.)
	@BeforeClass
	public static void beforeClass() {
		System.setProperty("some.property", "<value>");
	}
	 */
}