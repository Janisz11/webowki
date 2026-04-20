package pl.edu.pwr.ztw.demo.model;

import java.util.List;

public class PageResponse<T> {
    private List<T> data;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;

    public PageResponse(List<T> data, int page, int size, long totalElements, int totalPages, boolean hasNext) {
        this.data = data;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
    }

    public List<T> getData() { return data; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isHasNext() { return hasNext; }
}
