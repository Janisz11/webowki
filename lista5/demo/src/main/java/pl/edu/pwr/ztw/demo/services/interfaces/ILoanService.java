package pl.edu.pwr.ztw.demo.services.interfaces;

import pl.edu.pwr.ztw.demo.model.Loan;

import java.util.Collection;

public interface ILoanService {
    Collection<Loan> getLoans();
    Collection<Loan> getLoansByBook(String bookId);
    Collection<Loan> getLoansByReader(String readerId);
    Loan getLoan(String id);
    Loan loanBook(String bookId, String readerId);
    void returnBook(String loanId);
}
