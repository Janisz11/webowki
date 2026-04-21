package pl.edu.pwr.ztw.demo.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.pwr.ztw.demo.model.LoanRequest;
import pl.edu.pwr.ztw.demo.services.interfaces.ILoanService;

import java.util.Map;

@RestController
@RequestMapping("/loans")
public class LoanController {
    @Autowired
    ILoanService loanService;

    @GetMapping
    public ResponseEntity<Object> getLoans(
            @RequestParam(required = false) String bookId,
            @RequestParam(required = false) String readerId) {
        if (bookId != null) return new ResponseEntity<>(loanService.getLoansByBook(bookId), HttpStatus.OK);
        if (readerId != null) return new ResponseEntity<>(loanService.getLoansByReader(readerId), HttpStatus.OK);
        return new ResponseEntity<>(loanService.getLoans(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Object> loanBook(@Valid @RequestBody LoanRequest request) {
        var loan = loanService.loanBook(request.getBookId(), request.getReaderId());
        if (loan == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(loan, HttpStatus.CREATED);
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<Object> getLoan(@PathVariable String loanId) {
        var loan = loanService.getLoan(loanId);
        if (loan == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(loan, HttpStatus.OK);
    }

    @PatchMapping("/{loanId}")
    public ResponseEntity<Object> patchLoan(@PathVariable String loanId,
                                            @RequestBody Map<String, Object> patch) {
        Boolean returned = (Boolean) patch.get("returned");
        if (Boolean.TRUE.equals(returned)) {
            loanService.returnBook(loanId);
        }
        var loan = loanService.getLoan(loanId);
        if (loan == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(loan, HttpStatus.OK);
    }
}
