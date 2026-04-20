package pl.edu.pwr.ztw.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pwr.ztw.demo.services.interfaces.ILoanService;

@RestController
public class LoanController {
    @Autowired
    ILoanService loanService;

    @GetMapping("/loans")
    public ResponseEntity<Object> getLoans() {
        return new ResponseEntity<>(loanService.getLoans(), HttpStatus.OK);
    }

    @PostMapping("/books/{bookId}/loans")
    public ResponseEntity<Object> loanBook(@PathVariable String bookId, @RequestParam String readerId) {
        var loan = loanService.loanBook(bookId, readerId);
        if (loan == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(loan, HttpStatus.CREATED);
    }

    @GetMapping("/books/{bookId}/loans")
    public ResponseEntity<Object> getLoansByBook(@PathVariable String bookId) {
        return new ResponseEntity<>(loanService.getLoansByBook(bookId), HttpStatus.OK);
    }

    @GetMapping("/books/{bookId}/loans/{loanId}")
    public ResponseEntity<Object> getLoan(@PathVariable String bookId, @PathVariable String loanId) {
        var loan = loanService.getLoan(loanId);
        if (loan == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(loan, HttpStatus.OK);
    }

    @GetMapping("/readers/{readerId}/loans")
    public ResponseEntity<Object> getLoansByReader(@PathVariable String readerId) {
        return new ResponseEntity<>(loanService.getLoansByReader(readerId), HttpStatus.OK);
    }

    @PutMapping("/books/{bookId}/loans/{loanId}/return")
    public ResponseEntity<Object> returnBook(@PathVariable String bookId, @PathVariable String loanId) {
        loanService.returnBook(loanId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
