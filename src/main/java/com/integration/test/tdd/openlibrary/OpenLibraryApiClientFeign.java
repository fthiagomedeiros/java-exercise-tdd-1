package com.integration.test.tdd.openlibrary;

import com.integration.test.tdd.dto.OpenLibraryBookResponse;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(url = "https://openlibrary.org", name = "bookClient")
public interface OpenLibraryApiClientFeign {

    @GetMapping("/api/books?jscmd=data&format=json&bibkeys={isbn}")
    @Headers("Content-Type: application/json")
    Map<String, OpenLibraryBookResponse> fetchBook(@RequestParam String isbn);
}
