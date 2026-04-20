package pl.edu.pwr.ztw.demo.model;

import java.time.Instant;

public class Loan {
    private String id;
    private Book book;
    private Reader reader;
    private Instant loanDate;
    private Instant returnDate;

    public Loan(String id, Book book, Reader reader, Instant loanDate) {
        this.id = id;
        this.book = book;
        this.reader = reader;
        this.loanDate = loanDate;
        this.returnDate = null;
    }

    public String getId() { return id; }
    public Book getBook() { return book; }
    public Reader getReader() { return reader; }
    public Instant getLoanDate() { return loanDate; }
    public Instant getReturnDate() { return returnDate; }
    public void setReturnDate(Instant returnDate) { this.returnDate = returnDate; }
    public boolean isReturned() { return returnDate != null; }
}
