package com.integration.test.tdd.sqsmessaging;

import static org.awaitility.Awaitility.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.integration.test.tdd.entities.Book;
import com.integration.test.tdd.openlibrary.OpenLibraryApiWebClient;
import com.integration.test.tdd.repositories.BookRepository;
import io.awspring.cloud.autoconfigure.core.AwsAutoConfiguration;
import io.awspring.cloud.autoconfigure.core.CredentialsProviderAutoConfiguration;
import io.awspring.cloud.autoconfigure.core.RegionProviderAutoConfiguration;
import io.awspring.cloud.autoconfigure.sqs.SqsAutoConfiguration;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(SpringExtension.class)
@Import(BookListener.class)
@ImportAutoConfiguration({
  JacksonAutoConfiguration.class,
  CredentialsProviderAutoConfiguration.class,
  RegionProviderAutoConfiguration.class,
  AwsAutoConfiguration.class,
  SqsAutoConfiguration.class
})
class BookListenerSliceTest {

  static LocalStackContainer localStack = new LocalStackContainer(
      DockerImageName.parse("localstack/localstack:3.0.2"))
      .withServices(Service.SQS)
      .withReuse(true);

  static {
    localStack.start();
  }

  @Autowired
  private SqsTemplate template;

  @Autowired
  private BookListener cut;

  @MockBean
  private BookRepository repository;

  @MockBean
  private OpenLibraryApiWebClient client;

  private static final String QUEUE_NAME = "book-queue";
  private static final String ISBN = "9780596004651";

  @BeforeAll
  static void beforeAll() throws IOException, InterruptedException {
    localStack.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", QUEUE_NAME);
  }

  @Test
  void shouldStartSQS() {
    assertNotNull(cut);
    assertNotNull(template);
  }

  @Test
  void shouldConsumeMessageWhenPayloadIsCorrect() {
    template.send(QUEUE_NAME, new BookSynchronization(ISBN, "author"));

    when(repository.findByIsbn(ISBN)).thenReturn(new Book());

    given()
        .await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(() -> verify(repository).findByIsbn(ISBN));
  }
}
