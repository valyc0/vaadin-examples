package io.bootify.my_app.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class GenericPaginatedGrid<T> extends VerticalLayout {

    private final Grid<T> grid = new Grid<>();
    private final HorizontalLayout paginationLayout = new HorizontalLayout();
    private final HorizontalLayout filterLayout = new HorizontalLayout();
    private final Select<Integer> pageSizeSelect = new Select<>();

    private int currentPage = 0;
    private int pageSize = 10;
    private int totalPages = 0;
    private List<Sort.Order> currentSortOrders = new ArrayList<>();

    // Funzione per caricare dati: (PageRequest, SortOrders, filterText) -> Page<T>
    private TriFunction<PageRequest, List<Sort.Order>, String, org.springframework.data.domain.Page<T>> dataProvider;

    private String filterText = "";

    // Multi-select support (opt-in via enableMultiSelect())
    private final Set<T> crossPageSelection = new LinkedHashSet<>();
    private Consumer<Set<T>> selectionChangeListener;
    private boolean multiSelectEnabled = false;
    private boolean restoringSelection = false;

    public GenericPaginatedGrid() {
        setWidthFull();
        setSpacing(true);
        setPadding(false);

        // Configura grid
        grid.setSizeFull();
        grid.setMinHeight("450px");
        grid.setMultiSort(true);
        grid.addSortListener(event -> {
            try {
                currentSortOrders.clear();
                if (event.getSortOrder() != null && !event.getSortOrder().isEmpty()) {
                    event.getSortOrder().forEach(sortOrder -> {
                        if (sortOrder != null && sortOrder.getSorted() != null) {
                            String property = sortOrder.getSorted().getKey();
                            if (property != null && !property.isEmpty()) {
                                Sort.Direction direction = sortOrder.getDirection() == SortDirection.ASCENDING
                                        ? Sort.Direction.ASC
                                        : Sort.Direction.DESC;
                                currentSortOrders.add(new Sort.Order(direction, property));
                            }
                        }
                    });
                }
                currentPage = 0;
                loadData();
            } catch (Exception e) {
                // Log error but don't crash
                System.err.println("Error handling sort event: " + e.getMessage());
            }
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

        pageSizeSelect.setItems(10, 20, 100);
        pageSizeSelect.setValue(pageSize);
        pageSizeSelect.setLabel("Righe per pagina");
        pageSizeSelect.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                pageSize = e.getValue();
                currentPage = 0;
                loadData();
            }
        });

        paginationLayout.setSpacing(true);
        paginationLayout.setAlignItems(Alignment.BASELINE);
        add(filterLayout, grid, paginationLayout);
        expand(grid);
    }

    /**
     * Enables multi-select mode with cross-page selection tracking.
     * Call once after construction to opt-in; other views remain unaffected.
     */
    public void enableMultiSelect() {
        multiSelectEnabled = true;
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.asMultiSelect().addSelectionListener(event -> {
            if (restoringSelection) return;
            crossPageSelection.addAll(event.getAddedSelection());
            crossPageSelection.removeAll(event.getRemovedSelection());
            if (selectionChangeListener != null) {
                selectionChangeListener.accept(Collections.unmodifiableSet(crossPageSelection));
            }
        });
    }

    /** Returns the cross-page accumulated selection (unmodifiable view). */
    public Set<T> getSelectedItems() {
        return Collections.unmodifiableSet(crossPageSelection);
    }

    /** Clears all selections across all pages and fires the listener. */
    public void clearSelection() {
        crossPageSelection.clear();
        grid.deselectAll();
        if (selectionChangeListener != null) {
            selectionChangeListener.accept(Collections.unmodifiableSet(crossPageSelection));
        }
    }

    /** Registers a listener called whenever the selection changes. */
    public void setSelectionChangeListener(Consumer<Set<T>> listener) {
        this.selectionChangeListener = listener;
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
    
    public void setDefaultSort(String property, Sort.Direction direction) {
        currentSortOrders.clear();
        currentSortOrders.add(new Sort.Order(direction, property));
    }

    private void loadData() {
        if (dataProvider == null) return;

        Sort sort = currentSortOrders.isEmpty() ? Sort.unsorted() : Sort.by(currentSortOrders);
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize, sort);

        org.springframework.data.domain.Page<T> resultPage = dataProvider.apply(pageRequest, currentSortOrders, filterText);

        if (multiSelectEnabled) restoringSelection = true;
        grid.setItems(resultPage.getContent());
        if (multiSelectEnabled && !crossPageSelection.isEmpty()) {
            for (T item : resultPage.getContent()) {
                if (crossPageSelection.contains(item)) {
                    grid.asMultiSelect().select(item);
                }
            }
        }
        if (multiSelectEnabled) restoringSelection = false;
        totalPages = resultPage.getTotalPages();
        updatePaginationControls();
    }

    private void updatePaginationControls() {
        paginationLayout.removeAll();
        paginationLayout.add(pageSizeSelect);
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

