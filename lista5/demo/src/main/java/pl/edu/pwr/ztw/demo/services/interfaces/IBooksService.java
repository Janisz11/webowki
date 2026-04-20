package pl.edu.pwr.ztw.demo.services.interfaces;

import pl.edu.pwr.ztw.demo.model.Book;
import pl.edu.pwr.ztw.demo.model.BookRequest;
import pl.edu.pwr.ztw.demo.model.PageResponse;

import java.util.Collection;

public interface IBooksService {
    PageResponse<Book> getBooks(int page, int size);
    Collection<Book> getBooksByAuthor(String authorId);
    Book getBook(String id);
    void addBook(BookRequest book);
    void updateBook(String id, BookRequest book);
    void deleteBook(String id);
}
