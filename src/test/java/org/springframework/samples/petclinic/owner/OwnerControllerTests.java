/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.owner;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test class for {@link OwnerController}
 *
 * @author Colin But
 */
@WebMvcTest(OwnerController.class)
class OwnerControllerTests {

	private static final int TEST_OWNER_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper jsonMapper;

	@MockBean
	private OwnerRepository owners;

	private Owner george() {
		Owner george = new Owner();
		george.setId(TEST_OWNER_ID);
		george.setFirstName("George");
		george.setLastName("Franklin");
		george.setAddress("110 W. Liberty St.");
		george.setCity("Madison");
		george.setTelephone("6085551023");
		Pet max = new Pet();
		PetType dog = new PetType();
		dog.setName("dog");
		max.setType(dog);
		max.setName("Max");
		max.setBirthDate(LocalDate.now());
		george.addPet(max);
		max.setId(1);
		return george;
	};

	@BeforeEach
	void setup() {

		Owner george = george();
		given(this.owners.findByLastName(eq("Franklin"), any(Pageable.class)))
				.willReturn(new PageImpl<Owner>(Lists.newArrayList(george)));

		given(this.owners.findAll(any(Pageable.class))).willReturn(new PageImpl<Owner>(Lists.newArrayList(george)));

		given(this.owners.findById(TEST_OWNER_ID)).willReturn(george);
		Visit visit = new Visit();
		visit.setDate(LocalDate.now());
		george.getPet("Max").getVisits().add(visit);

	}

	@Test
	@Disabled("Inapplicable for REST API")
	void testInitCreationForm() throws Exception {
		mockMvc.perform(get("/owners/new")).andExpect(status().isOk()).andExpect(model().attributeExists("owner"))
				.andExpect(view().name("owners/createOrUpdateOwnerForm"));
	}

	@Test
	void testProcessCreationFormSuccess() throws Exception {
		Owner george = this.george();
		george.setId(null);

		mockMvc.perform(
				post("/owners").contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(george)))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.id", not(empty())))
				.andExpect(jsonPath("$.firstName", is(george.getFirstName())))
				.andExpect(jsonPath("$.lastName", is(george.getLastName())))
				.andExpect(jsonPath("$.address", is(george.getAddress())))
				.andExpect(jsonPath("$.city", is(george.getCity())))
				.andExpect(jsonPath("$.telephone", is(george.getTelephone())));
	}

	@Test
	void testProcessCreationFormHasErrors() throws Exception {
		Owner george = this.george();
		george.setAddress(null);
		george.setTelephone(null);

		mockMvc.perform(
				post("/owners").contentType(MediaType.APPLICATION_JSON).content(jsonMapper.writeValueAsString(george)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@Disabled("Inapplicable for REST API")
	void testInitFindForm() throws Exception {
		mockMvc.perform(get("/owners/find")).andExpect(status().isOk()).andExpect(model().attributeExists("owner"))
				.andExpect(view().name("owners/findOwners"));
	}

	@Test
	void testProcessFindFormSuccess() throws Exception {
		Page<Owner> tasks = new PageImpl<>(Lists.newArrayList(george(), new Owner()));
		Mockito.when(this.owners.findByLastName(anyString(), any(Pageable.class))).thenReturn(tasks);

		mockMvc.perform(get("/owners")).andExpect(status().isOk()).andExpect(jsonPath("size()", is(2)));
	}

	@Test
	void testProcessFindFormByLastName() throws Exception {
		Owner george = george();
		Page<Owner> tasks = new PageImpl<Owner>(Lists.newArrayList(george));
		Mockito.when(this.owners.findByLastName(eq(george.getLastName()), any(Pageable.class))).thenReturn(tasks);

		mockMvc.perform(get("/owners").param("lastName", george.getLastName())).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id", not(empty())))
				.andExpect(jsonPath("$[0].firstName", is(george.getFirstName())))
				.andExpect(jsonPath("$[0].lastName", is(george.getLastName())))
				.andExpect(jsonPath("$[0].address", is(george.getAddress())))
				.andExpect(jsonPath("$[0].city", is(george.getCity())))
				.andExpect(jsonPath("$[0].telephone", is(george.getTelephone())));
	}

	@Test
	void testProcessFindFormNoOwnersFound() throws Exception {
		Page<Owner> tasks = new PageImpl<Owner>(Lists.newArrayList());
		Mockito.when(this.owners.findByLastName(eq("Unknown Surname"), any(Pageable.class))).thenReturn(tasks);

		mockMvc.perform(get("/owners").param("lastName", "Unknown Surname")).andExpect(status().isNotFound());
	}

	@Test
	@Disabled("Inapplicable for REST API.")
	void testInitUpdateOwnerForm() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/edit", TEST_OWNER_ID)).andExpect(status().isOk())
				.andExpect(model().attributeExists("owner"))
				.andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
				.andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
				.andExpect(model().attribute("owner", hasProperty("address", is("110 W. Liberty St."))))
				.andExpect(model().attribute("owner", hasProperty("city", is("Madison"))))
				.andExpect(model().attribute("owner", hasProperty("telephone", is("6085551023"))))
				.andExpect(view().name("owners/createOrUpdateOwnerForm"));
	}

	@Test
	void testProcessUpdateOwnerFormSuccess() throws Exception {
		Owner george = this.george();

		mockMvc.perform(post("/owners/{ownerId}", TEST_OWNER_ID).contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsString(george))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", not(empty()))).andExpect(jsonPath("$.firstName", is(george.getFirstName())))
				.andExpect(jsonPath("$.lastName", is(george.getLastName())))
				.andExpect(jsonPath("$.address", is(george.getAddress())))
				.andExpect(jsonPath("$.city", is(george.getCity())))
				.andExpect(jsonPath("$.telephone", is(george.getTelephone())));
	}

	@Test
	@Disabled("Inapplicable for REST API")
	void testProcessUpdateOwnerFormUnchangedSuccess() throws Exception {
		mockMvc.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)).andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void testProcessUpdateOwnerFormHasErrors() throws Exception {
		Owner george = this.george();
		george.setAddress("");

		mockMvc.perform(post("/owners/{ownerId}", TEST_OWNER_ID).contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsString(george))).andExpect(status().isBadRequest());
	}

	@Test
	void testShowOwner() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}", TEST_OWNER_ID)).andExpect(status().isOk())
				.andExpect(jsonPath("$.lastName", is("Franklin"))).andExpect(jsonPath("$.firstName", is("George")))
				.andExpect(jsonPath("$.address", is("110 W. Liberty St."))).andExpect(jsonPath("$.city", is("Madison")))
				.andExpect(jsonPath("$.telephone", is("6085551023"))).andExpect(jsonPath("$.pets", not(empty())))
				.andExpect(jsonPath("$.pets", new BaseMatcher<List<Map>>() {
					@Override
					public void describeTo(Description description) {
						description.appendText("Max did not have any visits but have to.");
					}

					@Override
					public boolean matches(Object petsData) {
						List<Map> pets = (List<Map>) petsData;
						Collection visits = (Collection) pets.get(0).get("visits");
						return !visits.isEmpty();
					}
				}));
	}

}
