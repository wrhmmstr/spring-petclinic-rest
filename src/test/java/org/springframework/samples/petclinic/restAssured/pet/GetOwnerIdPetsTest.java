package org.springframework.samples.petclinic.restAssured.pet;

import io.restassured.RestAssured;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class GetOwnerIdPetsTest {
	private static Connection connection;
	static List<Integer> ownerIds = new ArrayList<>();
	static List<Integer> petIds = new ArrayList<>();

	@BeforeAll
	public static void connect() throws SQLException {
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
		connection = DriverManager.getConnection(
			"jdbc:postgresql://localhost:5432/petclinic",
			"petclinic",
			"petclinic"
		);
	}

	@AfterAll
	public static void deleteTestData() throws SQLException {
		deleteTestOwnersFromDb(ownerIds);
		deleteTestPetsFromDb(petIds);
		connection.close();
	}

	private static void deleteTestOwnersFromDb(List<Integer> ownerIds) throws SQLException {
		for (int id : ownerIds) {
			PreparedStatement sql = connection.prepareStatement("delete from owners where id = ?");
			sql.setInt(1, id);
			System.out.println(sql);
			sql.executeUpdate();
		}
	}

	private static void deleteTestPetsFromDb(List<Integer> ownerIds) throws SQLException {
		for (int id : ownerIds) {
			PreparedStatement sql = connection.prepareStatement("delete from pets where id = ?");
			sql.setInt(1, id);
			System.out.println(sql);
			sql.executeUpdate();
		}
	}

	static Integer createPet() throws SQLException {
		Integer ownerId = createOwner();
		PreparedStatement sql = connection.prepareStatement(
			"insert into pets (name, birth_date, owner_id) values (?, ?, ?);",
			Statement.RETURN_GENERATED_KEYS);
		sql.setString(1, "testPetName" + RandomStringUtils.randomNumeric(5));
		sql.setDate(2, Date.valueOf(LocalDate.now()));
		sql.setInt(3, ownerId);
		sql.executeUpdate();

		ResultSet keys = sql.getGeneratedKeys();
		keys.next();
		int id = keys.getInt(1);
		petIds.add(id);
		return id;
	}

	static Integer createOwner() throws SQLException {
		PreparedStatement sql = connection.prepareStatement(
			"insert into owners (first_name, last_name, address, city, telephone) values (?, ?, ?, ?, ?)",
			Statement.RETURN_GENERATED_KEYS);
		sql.setString(1, "testFName" + RandomStringUtils.randomNumeric(5));
		sql.setString(2, "testLName" + RandomStringUtils.randomNumeric(5));
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

	@Test
	@DisplayName("Проверка ответа при отправке запроса с несуществующим ownerId")
	public void shouldErrorWithNonExistOwnerId() {
		given()
			.contentType("*/*")
			.pathParam("ownerId", Integer.parseInt(RandomStringUtils.randomNumeric(4)))
			.when()
			.get("/owners/{ownerId}/pets")
			.then()
			.statusCode(400);
	}

	@Test
	@DisplayName("Проверка ответа при отправке запроса с существующим ownerId")
	public void shouldOkWithExistOwnerId() throws SQLException {
		Integer ownerId = createOwner();
		Integer petId1 = createPet();
//		Integer petId2 = createPet();
		given()
				.contentType("*/*")
				.pathParam("ownerId", ownerId+1)
				.when()
				.get("/owners/{ownerId}/pets")
				.then()
				.statusCode(200)
				.body("[0].id", Matchers.is(petId1),
						"[0].name");
	}
}
