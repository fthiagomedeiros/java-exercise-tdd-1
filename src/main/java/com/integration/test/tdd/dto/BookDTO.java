package com.integration.test.tdd.dto;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BookDTO {
    private UUID id;
    private String isbn;

    @Override
    public String toString() {
        return new GsonBuilder()
                .create()
                .toJson(this);
    }

}
