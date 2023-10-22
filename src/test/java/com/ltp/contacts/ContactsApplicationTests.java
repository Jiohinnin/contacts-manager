package com.ltp.contacts;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ltp.contacts.pojo.Contact;
import com.ltp.contacts.repository.ContactRepository;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@SpringBootTest
@AutoConfigureMockMvc
class ContactsApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
    private MockMvc mockMvc;

	@Autowired
	private ContactRepository contactRepository;


	private Contact[] contacts = new Contact[] {
		new Contact("1", "Jon Snow", "6135342524"),
		new Contact("2", "Tyrion Lannister", "4145433332"),
		new Contact("3", "The Hound", "3452125631"),
	};

	@BeforeEach
    void setup(){
		for (int i = 0; i < contacts.length; i++) {
			contactRepository.saveContact(contacts[i]);
		}
	}

	@AfterEach
	void clear(){
		contactRepository.getContacts().clear();
    }


	@Test
	public void getContactByIdTest() throws Exception {
		RequestBuilder goodRequest = MockMvcRequestBuilders.get("/contact/1");
		mockMvc.perform(goodRequest)
		.andExpect(status().is2xxSuccessful())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON));
		
		RequestBuilder badRequest = MockMvcRequestBuilders.get("/contact/1212");
		mockMvc.perform(badRequest)
		.andExpect(status().is4xxClientError());
	}
	
	@Test
	public void getAllContactsTest() throws Exception {
		RequestBuilder request = MockMvcRequestBuilders.get("/contact/all");
		mockMvc.perform(request).andExpectAll(
			status().is2xxSuccessful(),
			content().contentType(MediaType.APPLICATION_JSON),
			jsonPath("$.[?(@.phoneNumber == 4145433332 && @.name == \"Tyrion Lannister\")]").exists(),
			jsonPath("$.[?(@.phoneNumber == 3452125631 && @.name == \"The Hound\")]").exists()
			);
	}

	@Test
	public void validContactCreation() throws Exception {
		RequestBuilder request = MockMvcRequestBuilders.post("/contact")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(new Contact("Rayan Slim", "123456789")));

		mockMvc.perform(request).andExpect(status().isCreated());
	}

	@Test
	public void invalidContactCreation() throws Exception {
		RequestBuilder request = MockMvcRequestBuilders.post("/contact")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(new Contact("    ", "    ")));

		mockMvc.perform(request).andExpect(status().isBadRequest());
	}

	@Test
	public void contactNotFoundTest() throws Exception {
		RequestBuilder request = MockMvcRequestBuilders.get("/contact/4");
		mockMvc.perform(request).andExpect(status().isNotFound());
	}


}
