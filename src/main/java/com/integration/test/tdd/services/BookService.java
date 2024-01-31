package com.integration.test.tdd.services;

import static org.hibernate.sql.ast.spi.SqlAppender.COMMA_SEPARATOR;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integration.test.tdd.dto.BookDTO;
import com.integration.test.tdd.dto.OpenLibraryBookResponse;
import com.integration.test.tdd.dto.Author;
import com.integration.test.tdd.dto.Publishers;
import com.integration.test.tdd.entities.Book;
import com.integration.test.tdd.exceptions.BookAlreadyExistsException;
import com.integration.test.tdd.mappers.BookMapper;
import com.integration.test.tdd.openlibrary.OpenLibraryApiWebClient;
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

  private final OpenLibraryApiWebClient bookClient;

  private final ObjectMapper objectMapper;

  public BookService(
      BookRepository bookRepository,
      BookMapper bookMapper,
      OpenLibraryApiWebClient bookClient,
      ObjectMapper objectMapper) {
    this.bookRepository = bookRepository;
    this.bookMapper = bookMapper;
    this.bookClient = bookClient;
    this.objectMapper = objectMapper;
  }

  public BookDTO createBook(BookDTO bookDTO) throws Exception {

    Book mBook = bookRepository.findByIsbn(bookDTO.getIsbn());
    if (mBook != null) {
      throw new BookAlreadyExistsException();
    }

    Book book = bookMapper.toBook(bookDTO);
    Map<String, OpenLibraryBookResponse> bookFetched = bookClient.fetchBook(bookDTO.getIsbn());

    OpenLibraryBookResponse response = objectMapper
        .convertValue(bookFetched.get(bookDTO.getIsbn()), OpenLibraryBookResponse.class);

    String bookTitle = response.getTitle();
    book.setTitle(bookTitle);

    List<Author> authors = response.getAuthors();
    book.setAuthor(authors.stream()
            .map(Author::getName)
            .collect(Collectors.joining(COMMA_SEPARATOR)));

    List<Publishers> publishers = response.getPublishers();
    book.setEditor(publishers.stream()
            .map(Publishers::getName)
            .collect(Collectors.joining(COMMA_SEPARATOR)));

    book.setGenre(TECH_BOOK);

    Book result = bookRepository.save(book);
    return bookMapper.toBookDto(result);
  }

  public List<BookDTO> fetchAllBooks() {
    List<Book> books = bookRepository.findAll();
    return bookMapper.map(books);
  }
}
