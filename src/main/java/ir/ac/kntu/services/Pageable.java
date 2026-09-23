package ir.ac.kntu.services;

import java.util.List;

public interface Pageable<T> {
    List<T> getPage(List<T> items, int pageNumber, int pageSize);
}