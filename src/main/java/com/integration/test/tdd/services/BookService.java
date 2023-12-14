package com.integration.test.tdd.services;

import com.integration.test.tdd.dto.BookDTO;
import com.integration.test.tdd.dto.OpenLibraryBookResponse;
import com.integration.test.tdd.dto.Author;
import com.integration.test.tdd.dto.Publishers;
import com.integration.test.tdd.entities.Book;
import com.integration.test.tdd.mappers.BookMapper;
import com.integration.test.tdd.openlibrary.OpenLibraryApiClientFeign;
import com.integration.test.tdd.repositories.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookService {

  private final Logger logger = LoggerFactory.getLogger(BookService.class);

  private final BookRepository bookRepository;

  private final BookMapper bookMapper;

  private final OpenLibraryApiClientFeign bookClient;

  public BookService(
      BookRepository bookRepository, BookMapper bookMapper, OpenLibraryApiClientFeign bookClient) {
    this.bookRepository = bookRepository;
    this.bookMapper = bookMapper;
    this.bookClient = bookClient;
  }

  public BookDTO createBook(BookDTO bookDTO) {
    Book book = bookMapper.toBook(bookDTO);
    Map<String, OpenLibraryBookResponse> bookFetched = bookClient.fetchBook(bookDTO.getIsbn());

    String bookTitle = bookFetched.get(bookDTO.getIsbn())
            .getTitle();
    book.setTitle(bookTitle);

    List<Author> authors = bookFetched.get(bookDTO.getIsbn())
            .getAuthors();
    book.setAuthor(authors.stream()
            .map(Author::getName)
            .collect(Collectors.joining(", ")));

    List<Publishers> publishers = bookFetched.get(bookDTO.getIsbn())
            .getPublishers();
    book.setEditor(publishers.stream()
            .map(Publishers::getName)
            .collect(Collectors.joining(", ")));

    Book result = bookRepository.save(book);
    return bookMapper.toBookDto(result);
  }
}
