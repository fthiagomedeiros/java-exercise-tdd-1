package com.integration.test.tdd.sqsmessaging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class BookSynchronization {

  private String isbn;
  private String author;

}