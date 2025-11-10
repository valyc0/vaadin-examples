package io.bootify.my_app.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.bootify.my_app.component.GenericPaginatedGrid;
import io.bootify.my_app.component.ProductFormDialog;
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
        H2 title = new H2("Generic Grid con CRUD");

        // Toolbar con pulsanti CRUD
        HorizontalLayout toolbar = createToolbar();

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
        configureGrid();

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
        add(title, toolbar, filterLayout, productGrid);
        expand(productGrid);
    }
    
    private HorizontalLayout createToolbar() {
        Button addButton = new Button("Aggiungi", new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openAddDialog());
        
        Button refreshButton = new Button("Aggiorna", new Icon(VaadinIcon.REFRESH));
        refreshButton.addClickListener(e -> productGrid.refresh());
        
        HorizontalLayout toolbar = new HorizontalLayout(addButton, refreshButton);
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        toolbar.setSpacing(true);
        
        return toolbar;
    }
    
    private void configureGrid() {
        productGrid.getGrid().addColumn(Product::getId).setHeader("ID").setSortable(true);
        productGrid.getGrid().addColumn(Product::getName).setHeader("Nome").setSortable(true);
        productGrid.getGrid().addColumn(Product::getCategory).setHeader("Categoria").setSortable(true);
        productGrid.getGrid().addColumn(product -> String.format("€ %.2f", product.getPrice()))
                .setHeader("Prezzo").setSortable(true);
        productGrid.getGrid().addColumn(Product::getQuantity).setHeader("Qtà").setSortable(true);
        productGrid.getGrid().addColumn(Product::getDescription).setHeader("Descrizione");
        
        // Colonna azioni
        productGrid.getGrid().addColumn(new ComponentRenderer<>(this::createActionsLayout))
                .setHeader("Azioni")
                .setAutoWidth(true)
                .setFlexGrow(0);
    }
    
    private Component createActionsLayout(Product product) {
        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editButton.setTooltipText("Modifica");
        editButton.addClickListener(e -> openEditDialog(product));
        
        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteButton.setTooltipText("Elimina");
        deleteButton.addClickListener(e -> confirmDelete(product));
        
        HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
        actions.setSpacing(false);
        return actions;
    }
    
    private void openAddDialog() {
        ProductFormDialog dialog = new ProductFormDialog(null, this::saveProduct);
        dialog.open();
    }
    
    private void openEditDialog(Product product) {
        ProductFormDialog dialog = new ProductFormDialog(product, this::saveProduct);
        dialog.open();
    }
    
    private void saveProduct(Product product) {
        productService.save(product);
        productGrid.refresh();
    }
    
    private void confirmDelete(Product product) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Conferma eliminazione");
        dialog.setText("Sei sicuro di voler eliminare \"" + product.getName() + "\"?");
        
        dialog.setCancelable(true);
        dialog.setCancelText("Annulla");
        
        dialog.setConfirmText("Elimina");
        dialog.setConfirmButtonTheme("error primary");
        
        dialog.addConfirmListener(e -> deleteProduct(product));
        
        dialog.open();
    }
    
    private void deleteProduct(Product product) {
        try {
            productService.delete(product.getId());
            productGrid.refresh();
            Notification.show("Prodotto eliminato con successo", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Errore durante l'eliminazione: " + e.getMessage(), 
                    3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}