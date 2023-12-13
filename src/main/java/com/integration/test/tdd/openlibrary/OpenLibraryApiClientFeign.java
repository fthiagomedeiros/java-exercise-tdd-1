package com.integration.test.tdd.openlibrary;

import com.integration.test.tdd.dto.OpenLibraryBookResponse;
import feign.Headers;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(url = "https://openlibrary.org", name = "bookClient")
public interface OpenLibraryApiClientFeign {

    @GetMapping("/api/books?jscmd=data&format=json&bibkeys={isbn}")
    @Headers("Content-Type: application/json")
    Map<String, OpenLibraryBookResponse> fetchBook(@PathVariable String isbn);
}
