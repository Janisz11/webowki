package pl.edu.pwr.ztw.demo.services;

import org.springframework.stereotype.Service;
import pl.edu.pwr.ztw.demo.services.interfaces.IAuthorService;
import pl.edu.pwr.ztw.demo.model.Author;
import pl.edu.pwr.ztw.demo.model.AuthorRequest;
import pl.edu.pwr.ztw.demo.model.PageResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthorService implements IAuthorService {
    private static List<Author> authorsRepo = new ArrayList<>();

    static {
        authorsRepo.add(new Author("550e8400-e29b-41d4-a716-446655440001", "Henryk", "Sienkiewicz"));
        authorsRepo.add(new Author("550e8400-e29b-41d4-a716-446655440002", "Stanisław", "Reymont"));
        authorsRepo.add(new Author("550e8400-e29b-41d4-a716-446655440003", "Adam", "Mickiewicz"));
    }

    @Override
    public PageResponse<Author> getAuthors(int page, int size) {
        long totalElements = authorsRepo.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        List<Author> data = authorsRepo.stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
        return new PageResponse<>(data, page, size, totalElements, totalPages, (page + 1) < totalPages);
    }

    @Override
    public Author getAuthor(String id) {
        return authorsRepo.stream()
                .filter(a -> a.getId().equals(id))
                .findAny()
                .orElse(null);
    }

    @Override
    public void addAuthor(AuthorRequest author) {
        authorsRepo.add(new Author(UUID.randomUUID().toString(), author.getFirstName(), author.getLastName()));
    }

    @Override
    public void updateAuthor(String id, AuthorRequest author) {
        authorsRepo.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .ifPresent(a -> {
                    a.setFirstName(author.getFirstName());
                    a.setLastName(author.getLastName());
                });
    }


    @Override
    public void deleteAuthor(String id) {
        authorsRepo.removeIf(a -> a.getId().equals(id));
    }
}
