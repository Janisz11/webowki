package pl.edu.pwr.ztw.demo.model;

import jakarta.validation.constraints.NotNull;

public class LoanRequest {
    @NotNull
    private String bookId;
    @NotNull
    private String readerId;

    public String getBookId() { return bookId; }
    public String getReaderId() { return readerId; }
}
