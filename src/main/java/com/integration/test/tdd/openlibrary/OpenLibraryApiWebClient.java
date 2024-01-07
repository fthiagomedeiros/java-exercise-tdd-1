package com.integration.test.tdd.openlibrary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.integration.test.tdd.dto.Author;
import com.integration.test.tdd.dto.OpenLibraryBookResponse;
import com.integration.test.tdd.dto.Publishers;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

@Component
@AllArgsConstructor
public class OpenLibraryApiWebClient {

  private final WebClient webClient;

  private final ObjectMapper mapper;

  @SneakyThrows
  public Map<String, OpenLibraryBookResponse> fetchBook(String isbn) {

    ObjectNode result =
        webClient
            .get()
            .uri(
                "/api/books",
                uriBuilder ->
                    uriBuilder
                        .queryParam("jscmd", "data")
                        .queryParam("format", "json")
                        .queryParam("bibkeys", isbn)
                        .build())
            .retrieve()
            .bodyToMono(ObjectNode.class)
            .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(200)))
            .block();

    OpenLibraryBookResponse openLibraryBookResponse = new OpenLibraryBookResponse();
    JsonNode content = result.get(isbn);

    String title = content.get("title").asText();

    String authorValue = content.get("authors").toPrettyString();
    List<Author> authors = Arrays.stream(mapper.readValue(authorValue, Author[].class))
        .toList();

    String publishersValue = content.get("publishers").toPrettyString();
    List<Publishers> publishers = Arrays.stream(mapper.readValue(publishersValue, Publishers[].class))
        .toList();

    openLibraryBookResponse.setTitle(title);
    openLibraryBookResponse.setAuthors(authors);
    openLibraryBookResponse.setPublishers(publishers);

    Map<String, OpenLibraryBookResponse> finalResponse = new HashMap<>();
    finalResponse.put(isbn, openLibraryBookResponse);
    return finalResponse;
  }
}
