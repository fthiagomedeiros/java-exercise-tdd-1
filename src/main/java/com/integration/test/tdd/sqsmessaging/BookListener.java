package com.integration.test.tdd.sqsmessaging;

import com.integration.test.tdd.entities.Book;
import com.integration.test.tdd.openlibrary.OpenLibraryApiWebClient;
import com.integration.test.tdd.repositories.BookRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import java.security.InvalidParameterException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class BookListener {

  private final Logger logger = LoggerFactory.getLogger(BookListener.class);

  private final BookRepository repository;
  private final OpenLibraryApiWebClient client;

  @SqsListener("${sqs.book-synchronization-queue}")
  public void consumeBookUpdates(BookSynchronization bookSynchronization) {
    logger.info(bookSynchronization.toString());

    if (bookSynchronization.getIsbn() == null || (bookSynchronization.getAuthor() == null)) {
      logger.warn("Invalid payload cannot be processed");
      return;
    }

    Book bookToUpdate = repository.findByIsbn(bookSynchronization.getIsbn());

    if (bookToUpdate == null) {
      logger.warn(
          String.format("The book with ISBN %s has not been found", bookSynchronization.getIsbn()));
      return;
    }

    bookToUpdate.setAuthor(bookSynchronization.getAuthor());
    repository.save(bookToUpdate);
  }
}
