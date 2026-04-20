package pl.edu.pwr.ztw.demo.model;

import jakarta.validation.constraints.NotNull;

public class AuthorRequest {
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
}