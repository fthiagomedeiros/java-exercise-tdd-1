package com.integration.test.tdd.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FOUND, reason = "This book already exists.")
public class BookAlreadyExistsException extends RuntimeException {}
