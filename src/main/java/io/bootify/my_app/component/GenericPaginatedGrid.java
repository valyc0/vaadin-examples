package io.bootify.my_app.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class GenericPaginatedGrid<T> extends VerticalLayout {

    private final Grid<T> grid = new Grid<>();
    private final HorizontalLayout paginationLayout = new HorizontalLayout();
    private final HorizontalLayout filterLayout = new HorizontalLayout();

    private int currentPage = 0;
    private int pageSize = 5;
    private int totalPages = 0;
    private List<Sort.Order> currentSortOrders = new ArrayList<>();

    // Funzione per caricare dati: (PageRequest, SortOrders, filterText) -> Page<T>
    private TriFunction<PageRequest, List<Sort.Order>, String, org.springframework.data.domain.Page<T>> dataProvider;

    private String filterText = "";

    public GenericPaginatedGrid() {
        setWidthFull();
        setSpacing(true);
        setPadding(false);

        // Configura grid
        grid.setSizeFull();
        grid.setMultiSort(true);
        grid.addSortListener(event -> {
            currentSortOrders.clear();
            event.getSortOrder().forEach(sortOrder -> {
                String property = sortOrder.getSorted().getKey();
                if (property != null) {
                    Sort.Direction direction = sortOrder.getDirection() == SortDirection.ASCENDING
                            ? Sort.Direction.ASC
                            : Sort.Direction.DESC;
                    currentSortOrders.add(new Sort.Order(direction, property));
                }
            });
            currentPage = 0;
            loadData();
        });

        // Layout filtri
        TextField searchField = new TextField();
        searchField.setPlaceholder("Cerca...");
        searchField.addValueChangeListener(e -> {
            filterText = e.getValue();
            currentPage = 0;
            loadData();
        });
        filterLayout.add(searchField);

        paginationLayout.setSpacing(true);
        add(filterLayout, grid, paginationLayout);
        expand(grid);
    }

    public void setDataProvider(TriFunction<PageRequest, List<Sort.Order>, String, org.springframework.data.domain.Page<T>> provider) {
        this.dataProvider = provider;
        loadData();
    }

    public Grid<T> getGrid() {
        return grid;
    }

    public void refresh() {
        loadData();
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    private void loadData() {
        if (dataProvider == null) return;

        Sort sort = currentSortOrders.isEmpty() ? Sort.by("id") : Sort.by(currentSortOrders);
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize, sort);

        org.springframework.data.domain.Page<T> resultPage = dataProvider.apply(pageRequest, currentSortOrders, filterText);

        grid.setItems(resultPage.getContent());
        totalPages = resultPage.getTotalPages();
        updatePaginationControls();
    }

    private void updatePaginationControls() {
        paginationLayout.removeAll();
        if (totalPages <= 1) return;

        Button prev = new Button("‹ Prev", e -> {
            if (currentPage > 0) {
                currentPage--;
                loadData();
            }
        });
        prev.setEnabled(currentPage > 0);
        paginationLayout.add(prev);

        int start = Math.max(0, currentPage - 2);
        int end = Math.min(totalPages - 1, currentPage + 2);
        for (int i = start; i <= end; i++) {
            Button pageBtn = new Button(String.valueOf(i + 1));
            if (i == currentPage) pageBtn.getElement().getThemeList().add("primary");
            int pageIndex = i;
            pageBtn.addClickListener(e -> {
                currentPage = pageIndex;
                loadData();
            });
            paginationLayout.add(pageBtn);
        }

        Button next = new Button("Next ›", e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadData();
            }
        });
        next.setEnabled(currentPage < totalPages - 1);
        paginationLayout.add(next);

        Span info = new Span(String.format("Page %d of %d", currentPage + 1, totalPages));
        paginationLayout.add(info);
    }

    @FunctionalInterface
    public interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }
}

