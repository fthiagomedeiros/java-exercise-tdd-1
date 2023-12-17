package com.integration.test.tdd.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integration.test.tdd.dto.BookDTO;
import com.integration.test.tdd.dto.OpenLibraryBookResponse;
import com.integration.test.tdd.dto.Author;
import com.integration.test.tdd.dto.Publishers;
import com.integration.test.tdd.entities.Book;
import com.integration.test.tdd.mappers.BookMapper;
import com.integration.test.tdd.mappers.OpenLibraryToBookMapper;
import com.integration.test.tdd.openlibrary.OpenLibraryApiClientFeign;
import com.integration.test.tdd.openlibrary.OpenLibraryApiClientRestTemplate;
import com.integration.test.tdd.repositories.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookService {

  private static final String TECH_BOOK = "IT";
  private final Logger logger = LoggerFactory.getLogger(BookService.class);

  private final BookRepository bookRepository;

  private final BookMapper bookMapper;

  private final OpenLibraryApiClientFeign bookClient;

  private final OpenLibraryApiClientRestTemplate bookClient2;

  private final OpenLibraryToBookMapper openLibraryToBookMapper;

  private final ObjectMapper objectMapper;

  public BookService(
      BookRepository bookRepository,
      BookMapper bookMapper,
      OpenLibraryApiClientFeign bookClient,
      OpenLibraryApiClientRestTemplate bookClient2,
      ObjectMapper objectMapper,
      OpenLibraryToBookMapper openLibraryToBookMapper) {
    this.bookRepository = bookRepository;
    this.bookMapper = bookMapper;
    this.bookClient = bookClient;
    this.bookClient2 = bookClient2;
    this.objectMapper = objectMapper;
    this.openLibraryToBookMapper = openLibraryToBookMapper;
  }

  public BookDTO createBook(BookDTO bookDTO) {
    Book book = bookMapper.toBook(bookDTO);
    Map<String, OpenLibraryBookResponse> bookFetched = bookClient.fetchBook(bookDTO.getIsbn());

    OpenLibraryBookResponse response = objectMapper
        .convertValue(bookFetched.get(bookDTO.getIsbn()), OpenLibraryBookResponse.class);

    String bookTitle = response.getTitle();
    book.setTitle(bookTitle);

    List<Author> authors = response.getAuthors();
    book.setAuthor(authors.stream()
            .map(Author::getName)
            .collect(Collectors.joining(", ")));

    List<Publishers> publishers = response.getPublishers();
    book.setEditor(publishers.stream()
            .map(Publishers::getName)
            .collect(Collectors.joining(", ")));

    book.setGenre(TECH_BOOK);

    Book result = bookRepository.save(book);
    return bookMapper.toBookDto(result);
//    return openLibraryToBookMapper.toBookDto(response);
  }
}
