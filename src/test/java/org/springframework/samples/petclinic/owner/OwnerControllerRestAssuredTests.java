package org.springframework.samples.petclinic.owner;

import io.restassured.RestAssured;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;

import java.sql.*;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("Owner Control Rest Assured Tests")
public class OwnerControllerRestAssuredTests {
	Integer ownerId;
	static Collection<Integer> ownerIds;
	String endpoint;
	static Connection connection;
	static Random random;

	@BeforeAll
	public static void connect() throws SQLException {
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
		connection = DriverManager.getConnection(
			"jdbc:postgresql://localhost:5432/petclinic",
			"petclinic",
			"petclinic"
		);
	}

	@BeforeEach
	public void SetUp() throws SQLException {
		ownerId = createOwner();
		endpoint = "/owners/{ownerId}";
		random = new Random();
	}

	@AfterAll
	public static void deleteTestData() throws SQLException {
		if (ownerIds != null) {
		deleteTestOwnersFromDb(ownerIds);
		}
		if (connection != null) {
			connection.close();
		}
	}


	static Integer createOwner() throws SQLException {
		PreparedStatement sql = connection.prepareStatement(
			"insert into owners as o (first_name, last_name, address, city, telephone) values (?, ?, ?, ?, ?)",
			Statement.RETURN_GENERATED_KEYS);
		sql.setString(1, "testFirstName" + RandomStringUtils.randomNumeric(5));
		sql.setString(2, "testLastName" + RandomStringUtils.randomNumeric(5));
		sql.setString(3, "testAddress" + RandomStringUtils.randomNumeric(5));
		sql.setString(4, "testCity" + RandomStringUtils.randomNumeric(5));
		sql.setString(5, "testTelephone" + RandomStringUtils.randomNumeric(5));
		sql.executeUpdate();
		ResultSet keys = sql.getGeneratedKeys();
		keys.next();
		int id = keys.getInt(1);
		ownerIds.add(id);
		return id;
	}

	private static void deleteTestOwnersFromDb(Collection<Integer> ownerIds) throws SQLException {
		for (int id : ownerIds) {
			PreparedStatement sql = connection.prepareStatement("delete from owners where id = ?");
			sql.setInt(1, id);
			System.out.println(sql);
			sql.executeUpdate();
		}
	}


	@Test
	@DisplayName("Should Find Owner By Id When Is Exist")
	public void ShouldFindOwnerByIdWhenIsExist() {
		given()
			.pathParam("ownerId", ownerId)
			.when()
			.get(endpoint)
			.then()
			.assertThat()
			.statusCode(200)
			.body("firstName", notNullValue())
			.body("firstName", containsString("testFirstName"))
			.body("lastName", notNullValue())
			.body("lastName", containsString("testLastName"))
			.body("address", notNullValue())
			.body("address", containsString("testAddress"))
			.body("city", notNullValue())
			.body("city", containsString("testCity"))
			.body("telephone", notNullValue())
			.body("telephone", containsString("testTelephone"))
			.body("id", notNullValue())
			.body("id", equalTo(ownerId));

	}
}
