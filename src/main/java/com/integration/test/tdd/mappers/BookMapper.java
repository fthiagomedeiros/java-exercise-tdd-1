package com.integration.test.tdd.mappers;

import com.integration.test.tdd.dto.BookDTO;
import com.integration.test.tdd.dto.OpenLibraryBookResponse;
import com.integration.test.tdd.entities.Book;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookMapper {

  BookDTO toBookDto(Book book);
  Book toBook(BookDTO book);
  BookDTO toBookDto(OpenLibraryBookResponse bookApi);

}