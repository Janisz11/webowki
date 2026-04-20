package pl.edu.pwr.ztw.demo.model;

public class ErrorResponse {
    private String status;
    private String title;
    private String details;
    private String path;

    public ErrorResponse(String status, String title, String details, String path) {
        this.status = status;
        this.title = title;
        this.details = details;
        this.path = path;
    }

    public String getStatus() { return status; }
    public String getTitle() { return title; }
    public String getDetails() { return details; }
    public String getPath() { return path; }
}
