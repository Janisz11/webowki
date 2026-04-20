package pl.edu.pwr.ztw.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.pwr.ztw.demo.model.Loan;
import pl.edu.pwr.ztw.demo.model.Reader;
import pl.edu.pwr.ztw.demo.services.interfaces.IBooksService;
import pl.edu.pwr.ztw.demo.services.interfaces.ILoanService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public class LoanService implements ILoanService {
    @Autowired
    IBooksService booksService;

    private static List<Reader> readersRepo = new ArrayList<>();
    private static List<Loan> loansRepo = new ArrayList<>();

    static {
        readersRepo.add(new Reader("550e8400-e29b-41d4-a716-446655440020", "Jan", "Kowalski"));
        readersRepo.add(new Reader("550e8400-e29b-41d4-a716-446655440021", "Anna", "Nowak"));
    }

    public Reader getReader(String id) {
        return readersRepo.stream()
                .filter(r -> r.getId().equals(id))
                .findAny()
                .orElse(null);
    }

    @Override
    public Collection<Loan> getLoans() { return loansRepo; }

    @Override
    public Collection<Loan> getLoansByBook(String bookId) {
        return loansRepo.stream()
                .filter(l -> l.getBook().getId().equals(bookId))
                .toList();
    }

    @Override
    public Collection<Loan> getLoansByReader(String readerId) {
        return loansRepo.stream()
                .filter(l -> l.getReader().getId().equals(readerId))
                .toList();
    }

    @Override
    public Loan getLoan(String id) {
        return loansRepo.stream()
                .filter(l -> l.getId().equals(id))
                .findAny()
                .orElse(null);
    }

    @Override
    public Loan loanBook(String bookId, String readerId) {
        var book = booksService.getBook(bookId);
        var reader = getReader(readerId);
        if (book == null || reader == null) throw new IllegalArgumentException("Book or reader not found");

        boolean alreadyLoaned = loansRepo.stream()
                .anyMatch(l -> l.getBook().getId().equals(bookId) && !l.isReturned());
        if (alreadyLoaned) throw new IllegalArgumentException("Book is already loaned");

        Loan loan = new Loan(UUID.randomUUID().toString(), book, reader, Instant.now());
        loansRepo.add(loan);
        return loan;
    }

    @Override
    public void returnBook(String loanId) {
        loansRepo.stream()
                .filter(l -> l.getId().equals(loanId))
                .findFirst()
                .ifPresent(l -> {
                    if (l.isReturned()) throw new IllegalArgumentException("Book is already returned");
                    l.setReturnDate(Instant.now());
                });
    }
}
