package com.integration.test.tdd.openlibrary;

import com.fasterxml.jackson.databind.JsonNode;
import com.integration.test.tdd.dto.BookDTO;
import com.integration.test.tdd.dto.OpenLibraryBookResponse;
import com.integration.test.tdd.mappers.OpenLibraryToBookMapper;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OpenLibraryApiClientRestTemplate {

  private final OpenLibraryToBookMapper mapper;

  private final RestTemplate restTemplate;

  public OpenLibraryApiClientRestTemplate(
      OpenLibraryToBookMapper mapper, RestTemplateBuilder restTemplateBuilder) {
    this.mapper = mapper;
    this.restTemplate =
        restTemplateBuilder
            .rootUri("https://openlibrary.org")
            .setConnectTimeout(Duration.ofSeconds(2))
            .setReadTimeout(Duration.ofSeconds(2))
            .build();
  }

  public Map<String, OpenLibraryBookResponse> fetchBook(String isbn) {

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    HttpEntity<Void> entity = new HttpEntity<>(headers);

    Map result =
        restTemplate
            .exchange(
                "/api/books?jscmd=data&format=json&bibkeys={isbn}",
                HttpMethod.GET,
                entity,
                Map.class,
                isbn)
            .getBody();

    return result;
  }

  private BookDTO convertToBook(String isbn, JsonNode content) {
    BookDTO book = new BookDTO();
    book.setIsbn(isbn);
    book.setTitle(content.get("title").asText());
    book.setAuthor(content.get("authors").get(0).get("name").asText());
    book.setGenre(
        content.get("subjects") == null
            ? "n.A"
            : content.get("subjects").get(0).get("name").asText("n.A."));
    return book;
  }
}
