package pl.edu.pwr.ztw.demo.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pwr.ztw.demo.model.BookRequest;
import pl.edu.pwr.ztw.demo.services.interfaces.IBooksService;

@RestController
@RequestMapping("/books")
public class BooksController {
    @Autowired
    IBooksService booksService;

    @GetMapping
    public ResponseEntity<Object> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0) throw new IllegalArgumentException("Page cannot be negative");
        if (size <= 0) throw new IllegalArgumentException("Size must be greater than 0");
        if (size > 100) throw new IllegalArgumentException("Size cannot exceed 100");
        return new ResponseEntity<>(booksService.getBooks(page, size), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getBook(@PathVariable String id) {
        var book = booksService.getBook(id);
        if (book == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(book, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Object> addBook(@Valid @RequestBody BookRequest book) {
        booksService.addBook(book);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateBook(@PathVariable String id, @Valid @RequestBody BookRequest book) {
        booksService.updateBook(id, book);
        var updated = booksService.getBook(id);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteBook(@PathVariable String id) {
        booksService.deleteBook(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
