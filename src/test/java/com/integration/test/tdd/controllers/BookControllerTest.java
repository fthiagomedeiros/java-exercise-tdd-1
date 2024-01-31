package com.integration.test.tdd.controllers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integration.test.tdd.base.BaseSqsIntegrationTest;
import com.integration.test.tdd.dto.BookDTO;
import com.integration.test.tdd.exceptions.BookAlreadyExistsException;
import java.io.IOException;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@AutoConfigureMockMvc
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.jpa.hibernate.ddl-auto = validate", "spring.flyway.enabled = true"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class BookControllerTest extends BaseSqsIntegrationTest {

  private static final String ISBN = "9780321160768";

  @Container
  public static PostgreSQLContainer database =
      new PostgreSQLContainer(DockerImageName.parse("postgres:latest"))
          .withUsername("springboot")
          .withPassword("springboot")
          .withDatabaseName("schema_book");

  private MockWebServer mockWebServer;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Value("classpath:/stubs/api/requests/books/request-9780321160768.json")
  private Resource request9780321160768;

  @Value("classpath:/stubs/openlibrary/success-9780321160768.json")
  private Resource openLibraryBookResponse;

  @BeforeAll
  static void beforeAll() throws IOException, InterruptedException {
    getLocalStackContainer()
        .execInContainer("awslocal", "sqs", "create-queue", "--queue-name", QUEUE_NAME);
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", database::getJdbcUrl);
    registry.add("spring.datasource.username", database::getUsername);
    registry.add("spring.datasource.password", database::getPassword);
  }

  @BeforeEach
  public void setup() throws IOException {
    this.mockWebServer = new MockWebServer();
    this.mockWebServer.start();
  }

  @Test
  public void contextLoads() {
    Assertions.assertNotNull(mockMvc);
  }

  @Test
  public void shouldCreateSuccessBook() throws Exception {
    BookDTO body = objectMapper.readValue(request9780321160768.getFile(), BookDTO.class);

    this.mockMvc
        .perform(post("/api/book").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.isbn", is("9780321160768")))
        .andExpect(jsonPath("$.author", is("Bruce Powel Douglass,David Harel")))
        .andExpect(jsonPath("$.title", is("Real time UML")));
  }

  @Test
  public void shouldNotAllowTheSameBookCreation() throws Exception {
    BookDTO body = objectMapper.readValue(request9780321160768.getFile(), BookDTO.class);

    this.mockMvc
        .perform(post("/api/book").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.isbn", is("9780321160768")))
        .andExpect(jsonPath("$.author", is("Bruce Powel Douglass,David Harel")))
        .andExpect(jsonPath("$.title", is("Real time UML")));

    this.mockMvc
        .perform(post("/api/book").contentType(MediaType.APPLICATION_JSON).content(body.toString()))
        .andExpect(status().isFound())
        .andExpect(status().reason("This book already exists."))
        .andDo(print());
  }

  @Test
  @Sql("/scripts/CREATE_BOOK.sql")
  public void shouldFetchAllBooks() throws Exception {
    this.mockMvc
        .perform(get("/api/book")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size()", is(2)));
  }
}
