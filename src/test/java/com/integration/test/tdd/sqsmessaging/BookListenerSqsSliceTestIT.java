package com.integration.test.tdd.sqsmessaging;

import static org.awaitility.Awaitility.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.integration.test.tdd.base.BaseSqsIntegrationTest;
import com.integration.test.tdd.entities.Book;
import com.integration.test.tdd.openlibrary.OpenLibraryApiWebClient;
import com.integration.test.tdd.repositories.BookRepository;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import io.awspring.cloud.test.sqs.SqsTest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

@SqsTest(BookListener.class)
public class BookListenerSqsSliceTestIT extends BaseSqsIntegrationTest {

  private static final Logger logger = LoggerFactory.getLogger(BookListenerSqsSliceTestIT.class);

  @Autowired
  private SqsTemplate template;

  @InjectMocks
  private BookListener cut;

  @MockBean
  private BookRepository repository;

  @MockBean
  private OpenLibraryApiWebClient client;

  @BeforeAll
  static void beforeAll() throws IOException, InterruptedException {
    getLocalStackContainer().execInContainer("awslocal", "sqs", "create-queue", "--queue-name", QUEUE_NAME);
    logger.info(String.format("SQS Queue: %s has been created successfully", QUEUE_NAME));
  }

  @Test
  void shouldStartSQS() {
    logger.info("Validating contexts");
    assertNotNull(cut);
    assertNotNull(getLocalStackContainer());
  }

  @Test
  void shouldConsumeMessageWhenPayloadIsCorrect() {
    template.send(QUEUE_NAME, new BookSynchronization(ISBN, "MOCKED_AUTHOR"));

    when(repository.findByIsbn(ISBN)).thenReturn(new Book());

    given()
        .await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(() -> verify(repository).findByIsbn(ISBN));
  }

  @Test
  void shouldNotConsumeMessageWhenPayloadIsInvalid() {
    template.send(QUEUE_NAME, new BookSynchronization(ISBN, null));

    when(repository.findByIsbn(ISBN)).thenReturn(new Book());

    given()
        .await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(() -> verify(repository, times(0)).findByIsbn(ISBN));
  }

}
