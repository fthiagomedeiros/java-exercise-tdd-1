package com.integration.test.tdd.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class OpenLibraryBookResponse {
    private String title;
    List<Author> authors;
    List<Publishers> publishers;
}