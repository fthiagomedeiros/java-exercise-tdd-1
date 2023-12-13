package com.integration.test.tdd.services;

import com.integration.test.tdd.dto.BookDTO;
import com.integration.test.tdd.dto.OpenLibraryBookResponse;
import com.integration.test.tdd.entities.Book;
import com.integration.test.tdd.mappers.BookMapper;
import com.integration.test.tdd.openlibrary.OpenLibraryApiClientFeign;
import com.integration.test.tdd.repositories.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BookService {

    private final Logger logger = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;

    private final BookMapper bookMapper;

    private final OpenLibraryApiClientFeign bookClient;

    public BookService(BookRepository bookRepository,
                       BookMapper bookMapper,
                       OpenLibraryApiClientFeign bookClient) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
        this.bookClient = bookClient;
    }


    public BookDTO createBook(BookDTO bookDTO) {
        Book book = bookMapper.toBook(bookDTO);
        Map<String, OpenLibraryBookResponse> bookFetched = bookClient
                .fetchBook(bookDTO.getIsbn());

        String bookTitle = bookFetched.get(bookDTO.getIsbn()).getTitle();
        book.setTitle(bookTitle);

        Book result = bookRepository.save(book);
        return bookMapper.toBookDto(result);
    }
}
