package com.integration.test.tdd.sqsmessaging;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.utility.DockerImageName;

public class BookListenerSliceTest {

  static LocalStackContainer localStack = new LocalStackContainer(
      DockerImageName.parse("localstack/localstack:3.0.2"))
      .withServices(Service.SQS)
      .withEnv("DEFAULT_REGION", "us-east-1")
      .withReuse(true);

  static {
    localStack.start();
  }

  @Test
  public void shouldStartSqs() {

  }
}
