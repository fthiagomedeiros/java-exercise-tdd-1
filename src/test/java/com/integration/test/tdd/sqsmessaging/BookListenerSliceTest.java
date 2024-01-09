package com.integration.test.tdd.sqsmessaging;

import static com.amazonaws.regions.Regions.US_EAST_1;
import static org.awaitility.Awaitility.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.integration.test.tdd.entities.Book;
import com.integration.test.tdd.openlibrary.OpenLibraryApiWebClient;
import com.integration.test.tdd.repositories.BookRepository;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.aws.autoconfigure.messaging.MessagingAutoConfiguration;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(SpringExtension.class)
@Import(BookListener.class)
@ImportAutoConfiguration(MessagingAutoConfiguration.class)
class BookListenerSliceTest {

  static LocalStackContainer localStack = new LocalStackContainer(
      DockerImageName.parse("localstack/localstack:3.0.2"))
      .withServices(Service.SQS)
      .withEnv("DEFAULT_REGION", US_EAST_1.getName())
      .withEnv("HOSTNAME_EXTERNAL", "awslocal")
      .withReuse(true);

  static {
    localStack.start();
  }

  @TestConfiguration
  public static class TestConfig {

    @Primary
    @Bean
    public AmazonSQSAsync amazonSQSAsync() {
      AmazonSQSAsync sqs =
          AmazonSQSAsyncClientBuilder.standard()
              .withCredentials(
                  new AWSStaticCredentialsProvider(
                      new BasicAWSCredentials(
                          localStack.getAccessKey(), localStack.getSecretKey())))
              .withEndpointConfiguration(new EndpointConfiguration(localStack.getEndpointOverride(Service.SQS).toString(),
                  US_EAST_1.getName()))
              .build();

      sqs.createQueue("book-queue");
      return sqs;
    }

    @Bean
    public QueueMessagingTemplate queueMessagingTemplate(AmazonSQSAsync amazonSQSAsync) {
      return new QueueMessagingTemplate(amazonSQSAsync);
    }
  }

  @Autowired
  private QueueMessagingTemplate queueMessagingTemplate;

  @Autowired
  private ApplicationContext context;

  @Autowired
  private BookListener cut;

  @MockBean
  private BookRepository repository;

  @MockBean
  private OpenLibraryApiWebClient client;

  @Test
  public void shouldStartSqs() {
    assertNotNull(cut);
  }

  @Test
  public void shouldConsumerPayloadCorrectly() {
    queueMessagingTemplate.convertAndSend("book-queue", BookSynchronization.builder()
            .isbn("isbn")
            .author("author")
        .build());

    when(repository.findByIsbn("isbn")).thenReturn(new Book());

    given()
        .await()
        .atMost(5, TimeUnit.SECONDS)
        .untilAsserted(() -> verify(repository).findByIsbn("isbn"));
  }
}