package pl.edu.pwr.ztw.demo.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Object> health() {
        return new ResponseEntity<>(Map.of(
                "status", "UP",
                "timestamp", java.time.Instant.now().toString()
        ), HttpStatus.OK);
    }
}