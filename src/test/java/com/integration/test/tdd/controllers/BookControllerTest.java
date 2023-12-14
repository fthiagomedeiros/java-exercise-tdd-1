package com.integration.test.tdd.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integration.test.tdd.dto.BookDTO;
import com.integration.test.tdd.dto.OpenLibraryBookResponse;
import com.integration.test.tdd.openlibrary.OpenLibraryApiClientFeign;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.jpa.hibernate.ddl-auto = validate", "spring.flyway.enabled = true"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class BookControllerTest {

    private static final String ISBN = "9780321160768";
    @Container
    public static PostgreSQLContainer database =
            new PostgreSQLContainer(DockerImageName.parse("postgres:latest"))
                    .withUsername("springboot")
                    .withPassword("springboot")
                    .withDatabaseName("schema_book");

    @DynamicPropertySource
    static void setDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private OpenLibraryApiClientFeign bookClient;

    @Autowired
    protected ObjectMapper objectMapper;

    @Value("classpath:/stubs/api/requests/books/request-9780321160768.json")
    private Resource request9780321160768;

    @Value("classpath:/stubs/openlibrary/success-9780321160768.json")
    private Resource openLibraryBookResponse;

    @Test
    public void contextLoads() {
        Assertions.assertNotNull(mockMvc);
    }

    @Test
    public void shouldCreateSuccessBook() throws Exception {
        BookDTO body = objectMapper.readValue(request9780321160768.getFile(), BookDTO.class);

        Map<String, OpenLibraryBookResponse> httpBookResponse =
                objectMapper.readValue(openLibraryBookResponse.getFile(), Map.class);
        when(bookClient.fetchBook(eq(ISBN))).thenReturn(httpBookResponse);

        this.mockMvc
                .perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isbn", is("9780321160768")))
                .andExpect(jsonPath("$.title", is("Real time UML - MOCKED BOOK TITLE")));
    }

}