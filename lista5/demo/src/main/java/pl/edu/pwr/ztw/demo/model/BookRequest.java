package pl.edu.pwr.ztw.demo.model;

import jakarta.validation.constraints.NotNull;

public class BookRequest {
    @NotNull
    private String title;
    @NotNull
    private String authorId;
    @NotNull
    private Integer pages;

    public String getTitle() { return title; }
    public String getAuthorId() { return authorId; }
    public Integer getPages() { return pages; }
}
