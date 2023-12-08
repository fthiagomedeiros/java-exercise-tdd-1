package com.integration.test.tdd.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.GsonBuilder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table
@NoArgsConstructor
@Getter
@Setter
public class Book {

    @Id
    @JsonIgnore
    @Column(name = "id", columnDefinition = "uuid")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String isbn;
    private String title;
    private String author;
    private String genre;
    private String editor;

    @Override
    public String toString() {
        return new GsonBuilder()
                .create()
                .toJson(this);
    }
}