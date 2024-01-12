package com.integration.test.tdd.base;


import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
public abstract class BaseSqsIntegrationTest {

  private static final Logger logger = LoggerFactory.getLogger(BaseSqsIntegrationTest.class);

  private static final String LOCAL_STACK_VERSION = "localstack/localstack:3.0.2";

  static LocalStackContainer localstack =
      new LocalStackContainer(DockerImageName.parse(LOCAL_STACK_VERSION))
          .withServices(Service.SQS)
          .withReuse(true);

  static {
    getLocalStackContainer().start();
  }

  protected static final String QUEUE_NAME = UUID.randomUUID().toString();
  protected static final String ISBN = "9780596004651";

  @DynamicPropertySource
  static void registerSqsProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.cloud.aws.endpoint", () -> localstack.getEndpoint());
    registry.add("sqs.book-synchronization-queue", () -> QUEUE_NAME);
  }

  public static LocalStackContainer getLocalStackContainer() {
    return localstack;
  }
  
}
