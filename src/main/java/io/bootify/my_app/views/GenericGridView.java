package io.bootify.my_app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;


import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.bootify.my_app.component.GenericPaginatedGrid;
import io.bootify.my_app.domain.Product;
import io.bootify.my_app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@Route(value = "generic-grid", layout = MainLayout.class)
@PageTitle("Generic Grid - Prodotti")
public class GenericGridView extends VerticalLayout {

    private final ProductService productService;
    private final GenericPaginatedGrid<Product> productGrid = new GenericPaginatedGrid<>();
    private final ComboBox<String> categoryFilter = new ComboBox<>("Categoria");
    private final TextField searchField = new TextField();

    public GenericGridView(@Autowired ProductService productService) {
        this.productService = productService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        // Titolo
        H2 title = new H2("Gestione Generic Grid Prodotti");

        // Filtri e ricerca
        searchField.setPlaceholder("Cerca per nome, descrizione o categoria...");
        searchField.setWidth("400px");
        searchField.setClearButtonVisible(true);

        categoryFilter.setItems("Tutti", "Elettronica", "Accessori", "Audio", "Componenti",
                "Rete", "Periferiche", "Arredamento", "Smart Home", "Storage");
        categoryFilter.setValue("Tutti");
        categoryFilter.setWidth("200px");

        HorizontalLayout filterLayout = new HorizontalLayout(searchField, categoryFilter);
        filterLayout.setAlignItems(Alignment.END);

        // Configurazione colonne del grid
        productGrid.getGrid().addColumn(Product::getId).setHeader("ID").setSortable(true);
        productGrid.getGrid().addColumn(Product::getName).setHeader("Nome").setSortable(true);
        productGrid.getGrid().addColumn(Product::getCategory).setHeader("Categoria").setSortable(true);
        productGrid.getGrid().addColumn(product -> String.format("€ %.2f", product.getPrice()))
                .setHeader("Prezzo").setSortable(true);
        productGrid.getGrid().addColumn(Product::getQuantity).setHeader("Qtà").setSortable(true);
        productGrid.getGrid().addColumn(Product::getDescription).setHeader("Descrizione");

        // Configura DataProvider con paginazione e filtri
        productGrid.setDataProvider((pageRequest, sortOrders, filterText) -> {
            String searchTerm = searchField.getValue() != null ? searchField.getValue() : "";
            String category = categoryFilter.getValue() != null && !"Tutti".equals(categoryFilter.getValue())
                    ? categoryFilter.getValue() : "";

            PageRequest pageable = PageRequest.of(pageRequest.getPageNumber(),
                    pageRequest.getPageSize(),
                    pageRequest.getSort());

            if (!searchTerm.isEmpty() && !category.isEmpty()) {
                return productService.searchByCategory(category, searchTerm, pageable);
            } else if (!searchTerm.isEmpty()) {
                return productService.search(searchTerm, pageable);
            } else if (!category.isEmpty()) {
                return productService.findByCategory(category, pageable);
            } else {
                return productService.findAll(pageable);
            }
        });

        // Ricarica grid quando cambiano filtri
        searchField.addValueChangeListener(e -> productGrid.refresh());
        categoryFilter.addValueChangeListener(e -> productGrid.refresh());

        // Layout finale
        add(title, filterLayout, productGrid);
        expand(productGrid);
    }
}