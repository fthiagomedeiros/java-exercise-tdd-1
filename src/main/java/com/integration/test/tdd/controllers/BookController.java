package com.integration.test.tdd.controllers;

import com.integration.test.tdd.dto.BookDTO;
import com.integration.test.tdd.services.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/book")
public class BookController {

    private final Logger logger = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<BookDTO> createBook(@RequestBody BookDTO bookDTO) throws Exception {
        BookDTO response = bookService.createBook(bookDTO);
        logger.info(String.format("Book created with ISBN %s", bookDTO.getIsbn()));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
