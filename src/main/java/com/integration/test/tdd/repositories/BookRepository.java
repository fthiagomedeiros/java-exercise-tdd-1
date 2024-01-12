package com.integration.test.tdd.repositories;

import com.integration.test.tdd.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
  Book findByIsbn(String isbn);
}
