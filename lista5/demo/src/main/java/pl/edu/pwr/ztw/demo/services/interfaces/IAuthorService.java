package pl.edu.pwr.ztw.demo.services.interfaces;

import pl.edu.pwr.ztw.demo.model.Author;
import pl.edu.pwr.ztw.demo.model.AuthorRequest;
import pl.edu.pwr.ztw.demo.model.PageResponse;

import java.util.Collection;

public interface IAuthorService {
    PageResponse<Author> getAuthors(int page, int size);
    Author getAuthor(String id);
    void addAuthor(AuthorRequest author);
    void updateAuthor(String id, AuthorRequest author);
    void deleteAuthor(String id);
}
