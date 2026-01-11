package ru.yandex.practicum.mymarket.items.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.yandex.practicum.mymarket.common.dto.ItemAction;
import ru.yandex.practicum.mymarket.common.dto.SortMode;

public class ItemsActionForm {

    @NotNull
    @Positive
    private Long id;

    @NotNull
    private ItemAction action;

    private String search = "";
    private SortMode sort = SortMode.NO;
    private int pageNumber = 1;
    private int pageSize = 5;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ItemAction getAction() {
        return action;
    }

    public void setAction(ItemAction action) {
        this.action = action;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = (search == null) ? "" : search;
    }

    public SortMode getSort() {
        return (sort == null) ? SortMode.NO : sort;
    }

    public void setSort(SortMode sort) {
        this.sort = (sort == null) ? SortMode.NO : sort;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
