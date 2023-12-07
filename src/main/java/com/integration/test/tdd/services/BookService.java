package com.integration.test.tdd.services;

import com.integration.test.tdd.dto.BookDTO;
import com.integration.test.tdd.entities.Book;
import com.integration.test.tdd.mappers.BookMapper;
import com.integration.test.tdd.repositories.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    private final Logger logger = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;

    private final BookMapper bookMapper;

    public BookService(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    public BookDTO createBook(BookDTO bookDTO) {
        Book book = bookMapper.toBook(bookDTO);
        Book result = bookRepository.save(book);
        return bookMapper.toBookDto(result);
    }
}
