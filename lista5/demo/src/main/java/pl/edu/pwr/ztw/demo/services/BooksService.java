package pl.edu.pwr.ztw.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.pwr.ztw.demo.model.Author;
import pl.edu.pwr.ztw.demo.model.Book;
import pl.edu.pwr.ztw.demo.model.BookRequest;
import pl.edu.pwr.ztw.demo.model.PageResponse;
import pl.edu.pwr.ztw.demo.services.interfaces.IAuthorService;
import pl.edu.pwr.ztw.demo.services.interfaces.IBooksService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BooksService implements IBooksService {
    @Autowired
    private IAuthorService authorService;

    private static List<Book> booksRepo = new ArrayList<>();

    static {
        Author sienkiewicz = new Author("550e8400-e29b-41d4-a716-446655440001", "Henryk", "Sienkiewicz");
        Author reymont = new Author("550e8400-e29b-41d4-a716-446655440002", "Stanisław", "Reymont");
        Author mickiewicz = new Author("550e8400-e29b-41d4-a716-446655440003", "Adam", "Mickiewicz");

        booksRepo.add(new Book("550e8400-e29b-41d4-a716-446655440010", "Potop", sienkiewicz, 936));
        booksRepo.add(new Book("550e8400-e29b-41d4-a716-446655440011", "Wesele", reymont, 150));
        booksRepo.add(new Book("550e8400-e29b-41d4-a716-446655440012", "Dziady", mickiewicz, 292));
    }

    @Override
    public PageResponse<Book> getBooks(int page, int size) {
        long totalElements = booksRepo.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        List<Book> data = booksRepo.stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
        return new PageResponse<>(data, page, size, totalElements, totalPages, (page + 1) < totalPages);
    }

    @Override
    public Book getBook(String id) {
        return booksRepo.stream()
                .filter(b -> b.getId().equals(id))
                .findAny()
                .orElse(null);
    }

    @Override
    public void addBook(BookRequest book) {
        Author author = authorService.getAuthor(book.getAuthorId());
        if (author == null) throw new IllegalArgumentException("Author not found");
        booksRepo.add(new Book(UUID.randomUUID().toString(), book.getTitle(), author, book.getPages()));
    }

    @Override
    public void updateBook(String id, BookRequest book) {
        Author author = authorService.getAuthor(book.getAuthorId());
        booksRepo.stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .ifPresent(b -> {
                    b.setTitle(book.getTitle());
                    b.setAuthor(author);
                    b.setPages(book.getPages());
                });
    }

    @Override
    public void deleteBook(String id) {
        booksRepo.removeIf(b -> b.getId().equals(id));
    }

    @Override
    public Collection<Book> getBooksByAuthor(String authorId) {
        return booksRepo.stream()
                .filter(b -> b.getAuthor().getId().equals(authorId))
                .collect(Collectors.toList());
    }
}
