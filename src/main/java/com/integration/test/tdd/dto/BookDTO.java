package com.integration.test.tdd.dto;

import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class BookDTO {
    private UUID id;
    private String isbn;
    private String title;
    private String author;
    private String genre;

    @Override
    public String toString() {
        return new GsonBuilder()
                .create()
                .toJson(this);
    }
}
