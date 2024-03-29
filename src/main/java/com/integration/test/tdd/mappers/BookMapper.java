package com.integration.test.tdd.mappers;

import com.integration.test.tdd.dto.BookDTO;
import com.integration.test.tdd.dto.OpenLibraryBookResponse;
import com.integration.test.tdd.entities.Book;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookMapper {

  BookDTO toBookDto(Book book);
  Book toBook(BookDTO book);
  List<BookDTO> map(List<Book> books);

}