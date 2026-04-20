package pl.edu.pwr.ztw.demo.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pwr.ztw.demo.model.Author;
import pl.edu.pwr.ztw.demo.model.AuthorRequest;
import pl.edu.pwr.ztw.demo.services.interfaces.IAuthorService;
import pl.edu.pwr.ztw.demo.services.interfaces.IBooksService;

@RestController
@RequestMapping("/authors")
public class AuthorController {
    @Autowired
    IAuthorService authorService;

    @Autowired
    IBooksService booksService;


    @GetMapping
    public ResponseEntity<Object> getAuthors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0) throw new IllegalArgumentException("Page cannot be negative");
        if (size <= 0) throw new IllegalArgumentException("Size must be greater than 0");

        return new ResponseEntity<>(authorService.getAuthors(page, size), HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Object> getAuthor(@PathVariable String id) {
        var author = authorService.getAuthor(id);
        if (author == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(author, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Object> addAuthor(@Valid @RequestBody AuthorRequest author) {
        authorService.addAuthor(author);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateAuthor(@PathVariable String id, @Valid @RequestBody AuthorRequest author) {
        authorService.updateAuthor(id, author);
        var updated = authorService.getAuthor(id);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteAuthor(@PathVariable String id) {
        authorService.deleteAuthor(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}/books")
    public ResponseEntity<Object> getBooksByAuthor(@PathVariable String id) {
        var author = authorService.getAuthor(id);
        if (author == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(booksService.getBooksByAuthor(id), HttpStatus.OK);
    }
}
