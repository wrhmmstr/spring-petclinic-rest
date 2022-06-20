package org.springframework.samples.petclinic.restAssured.crash;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class GetOupsTest {
	@BeforeAll
	public static void setUpErrorLogging() {
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
	}

	/**
	 * Expected: 200 OK
	 * Actual:  status": 500, "error": "Internal Server Error"
	 */
	@Test
	@Disabled
	@DisplayName("Проверка ответа при отправке запроса без токена")
	public void should200Ok() {
		given()
			.contentType("application/json")
			.when()
			.get("/oups")
			.then()
			.statusCode(200);
	}
}
