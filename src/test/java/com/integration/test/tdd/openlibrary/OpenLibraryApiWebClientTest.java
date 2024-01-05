package com.integration.test.tdd.openlibrary;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integration.test.tdd.dto.OpenLibraryBookResponse;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

public class OpenLibraryApiWebClientTest {

  private MockWebServer mockWebServer;
  private OpenLibraryApiWebClient cut;
  private ObjectMapper mapper = new ObjectMapper();

  private static final String ISBN = "9780321160768";
  private static String VALID_RESPONSE;

  static {
    try {
      VALID_RESPONSE =
          new String(
              Objects.requireNonNull(OpenLibraryApiWebClient.class
                      .getClassLoader()
                      .getResourceAsStream("stubs/openlibrary/success-" + ISBN + ".json"))
                  .readAllBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @BeforeEach
  public void setup() throws IOException {
    this.mockWebServer = new MockWebServer();
    this.mockWebServer.start();

    HttpClient httpClient =
        HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1_000)
            .doOnConnected(
                connection ->
                    connection
                        .addHandlerLast(new ReadTimeoutHandler(1))
                        .addHandlerLast(new WriteTimeoutHandler(1)));

    this.cut = new OpenLibraryApiWebClient(
            WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(mockWebServer.url("/").toString())
                .build(),
            mapper);
  }

  @Test
  void shouldReturnBookWhenResultIsSuccess() {

    MockResponse response =
        new MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setResponseCode(200)
            .setBody(VALID_RESPONSE);
    this.mockWebServer.enqueue(response);

    Map<String, OpenLibraryBookResponse> result = cut.fetchBook(ISBN);
    OpenLibraryBookResponse data = result.get(ISBN);

    assertEquals("9780321160768", data.getTitle());
  }

  @AfterEach
  void shutdown() throws IOException {
    this.mockWebServer.shutdown();
  }
}
