package com.integration.test.tdd.mappers;

import com.integration.test.tdd.dto.BookDTO;
import com.integration.test.tdd.dto.OpenLibraryBookResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OpenLibraryToBookMapper {

  BookDTO toBookDto(OpenLibraryBookResponse bookApi);

}