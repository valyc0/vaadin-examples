package io.bootify.my_app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.bootify.my_app.domain.Product;
import io.bootify.my_app.service.ProductService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "dashboard", layout = MainLayout.class)
@PageTitle("Dashboard - Prodotti")
public class DashboardView extends VerticalLayout {

    private final ProductService productService;
    private final Grid<Product> grid = new Grid<>(Product.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<String> categoryFilter = new ComboBox<>("Categoria");
    private final HorizontalLayout paginationLayout = new HorizontalLayout();
    
    private String currentSearchTerm = "";
    private String currentCategory = "";
    private int currentPage = 0;
    private int pageSize = 10;
    private int totalPages = 0;
    private List<Sort.Order> currentSortOrders = new ArrayList<>();

    public DashboardView(ProductService productService) {
        this.productService = productService;
        
        setSpacing(true);
        setPadding(true);
        setSizeFull();
        
        H2 title = new H2("Gestione Prodotti");
        
        // Add new product button
        Button addButton = new Button("Nuovo Prodotto", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openEditDialog(null));
        
        HorizontalLayout titleLayout = new HorizontalLayout(title, addButton);
        titleLayout.setWidthFull();
        titleLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        titleLayout.setAlignItems(Alignment.CENTER);
        
        // Search and filter components
        searchField.setPlaceholder("Cerca per nome, descrizione o categoria...");
        searchField.setWidth("400px");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> {
            currentSearchTerm = e.getValue();
            currentPage = 0; // Reset to first page on search
            loadProducts();
        });
        
        categoryFilter.setItems("Tutti", "Elettronica", "Accessori", "Audio", "Componenti", 
                                "Rete", "Periferiche", "Arredamento", "Smart Home", "Storage");
        categoryFilter.setValue("Tutti");
        categoryFilter.setWidth("200px");
        categoryFilter.addValueChangeListener(e -> {
            currentCategory = "Tutti".equals(e.getValue()) ? "" : e.getValue();
            currentPage = 0; // Reset to first page on filter change
            loadProducts();
        });
        
        HorizontalLayout filterLayout = new HorizontalLayout(searchField, categoryFilter);
        filterLayout.setAlignItems(Alignment.END);
        
        // Grid configuration - Auto-adattamento colonne al contenuto
        grid.addColumn(Product::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0).setSortable(true).setKey("id");
        grid.addColumn(Product::getName).setHeader("Nome").setAutoWidth(true).setFlexGrow(0).setSortable(true).setKey("name");
        grid.addColumn(Product::getCategory).setHeader("Categoria").setAutoWidth(true).setFlexGrow(0).setSortable(true).setKey("category");
        grid.addColumn(product -> String.format("€ %.2f", product.getPrice()))
            .setHeader("Prezzo").setAutoWidth(true).setFlexGrow(0).setSortable(true).setKey("price").setSortProperty("price");
        grid.addColumn(Product::getQuantity).setHeader("Qtà").setAutoWidth(true).setFlexGrow(0).setSortable(true).setKey("quantity");
        grid.addColumn(Product::getDescription).setHeader("Descrizione").setAutoWidth(true).setFlexGrow(1);
        
        // Actions column - Icone compatte senza testo
        grid.addComponentColumn(product -> {
            Button editButton = new Button(VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ICON);
            editButton.getElement().setAttribute("title", "Modifica");
            editButton.addClickListener(e -> openEditDialog(product));
            
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);
            deleteButton.getElement().setAttribute("title", "Elimina");
            deleteButton.addClickListener(e -> confirmDelete(product));
            
            HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
            actions.setSpacing(false);
            actions.getStyle().set("gap", "4px");
            return actions;
        }).setHeader("Azioni").setAutoWidth(true).setFlexGrow(0);
        
        grid.setSizeFull();
        grid.setMultiSort(true); // Enable multi-column sorting
        
        // Add sort listener for server-side sorting
        grid.addSortListener(event -> {
            currentSortOrders.clear();
            
            // Convert Vaadin sort orders to Spring Data sort orders
            event.getSortOrder().forEach(sortOrder -> {
                String sortProperty = sortOrder.getSorted().getKey();
                if (sortProperty != null) {
                    Sort.Direction direction = sortOrder.getDirection() == SortDirection.ASCENDING 
                        ? Sort.Direction.ASC 
                        : Sort.Direction.DESC;
                    currentSortOrders.add(new Sort.Order(direction, sortProperty));
                }
            });
            
            // If no sort specified, default to ID ascending
            if (currentSortOrders.isEmpty()) {
                currentSortOrders.add(new Sort.Order(Sort.Direction.ASC, "id"));
            }
            
            currentPage = 0; // Reset to first page on sort change
            loadProducts();
        });
        
        // Setup pagination
        setupPagination();
        loadProducts();
        
        add(titleLayout, filterLayout, grid, paginationLayout);
        expand(grid);
    }
    
    private void setupPagination() {
        paginationLayout.setSpacing(true);
        paginationLayout.setAlignItems(Alignment.CENTER);
        paginationLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        paginationLayout.getStyle().set("margin-top", "10px");
    }
    
    private void loadProducts() {
        // Create sort from current sort orders, default to ID if none specified
        Sort sort = currentSortOrders.isEmpty() 
            ? Sort.by(Sort.Direction.ASC, "id")
            : Sort.by(currentSortOrders);
            
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize, s1ort);
        
        org.springframework.data.domain.Page<Product> resultPage;
        
        if (!currentSearchTerm.isEmpty() && !currentCategory.isEmpty()) {
            resultPage = productService.searchByCategory(currentCategory, currentSearchTerm, pageRequest);
        } else if (!currentSearchTerm.isEmpty()) {
            resultPage = productService.search(currentSearchTerm, pageRequest);
        } else if (!currentCategory.isEmpty()) {
            resultPage = productService.findByCategory(currentCategory, pageRequest);
        } else {
            resultPage = productService.findAll(pageRequest);
        }
        
        grid.setItems(resultPage.getContent());
        totalPages = resultPage.getTotalPages();
        updatePaginationControls();
    }
    
    private void updatePaginationControls() {
        paginationLayout.removeAll();
        
        if (totalPages <= 1) {
            return;
        }
        
        // First page button
        Button firstButton = new Button("« Prima");
        firstButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        firstButton.setEnabled(currentPage > 0);
        firstButton.addClickListener(e -> {
            currentPage = 0;
            loadProducts();
        });
        paginationLayout.add(firstButton);
        
        // Previous button
        Button prevButton = new Button("‹ Precedente");
        prevButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        prevButton.setEnabled(currentPage > 0);
        prevButton.addClickListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadProducts();
            }
        });
        paginationLayout.add(prevButton);
        
        // Page numbers
        int startPage = Math.max(0, currentPage - 2);
        int endPage = Math.min(totalPages - 1, currentPage + 2);
        
        // First page if not visible
        if (startPage > 0) {
            Button firstPageButton = createPageButton(0);
            paginationLayout.add(firstPageButton);
            
            if (startPage > 1) {
                paginationLayout.add(new Span("..."));
            }
        }
        
        // Page numbers around current page
        for (int i = startPage; i <= endPage; i++) {
            Button pageButton = createPageButton(i);
            paginationLayout.add(pageButton);
        }
        
        // Last page if not visible
        if (endPage < totalPages - 1) {
            if (endPage < totalPages - 2) {
                paginationLayout.add(new Span("..."));
            }
            
            Button lastPageButton = createPageButton(totalPages - 1);
            paginationLayout.add(lastPageButton);
        }
        
        // Next button
        Button nextButton = new Button("Successivo ›");
        nextButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        nextButton.setEnabled(currentPage < totalPages - 1);
        nextButton.addClickListener(e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadProducts();
            }
        });
        paginationLayout.add(nextButton);
        
        // Last page button
        Button lastButton = new Button("Ultima »");
        lastButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        lastButton.setEnabled(currentPage < totalPages - 1);
        lastButton.addClickListener(e -> {
            currentPage = totalPages - 1;
            loadProducts();
        });
        paginationLayout.add(lastButton);
        
        // Page info
        Span pageInfo = new Span(String.format("Pagina %d di %d", currentPage + 1, totalPages));
        pageInfo.getStyle().set("margin-left", "15px");
        pageInfo.getStyle().set("font-size", "14px");
        paginationLayout.add(pageInfo);
    }
    
    private Button createPageButton(int pageNumber) {
        Button button = new Button(String.valueOf(pageNumber + 1));
        button.addThemeVariants(ButtonVariant.LUMO_SMALL);
        
        if (pageNumber == currentPage) {
            button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        }
        
        button.addClickListener(e -> {
            currentPage = pageNumber;
            loadProducts();
        });
        
        return button;
    }
    
    private int countProducts() {
        // Count based on current filters
        if (!currentSearchTerm.isEmpty() && !currentCategory.isEmpty()) {
            return (int) productService.searchByCategory(currentCategory, currentSearchTerm, 
                PageRequest.of(0, 1)).getTotalElements();
        } else if (!currentSearchTerm.isEmpty()) {
            return (int) productService.search(currentSearchTerm, 
                PageRequest.of(0, 1)).getTotalElements();
        } else if (!currentCategory.isEmpty()) {
            return (int) productService.findByCategory(currentCategory, 
                PageRequest.of(0, 1)).getTotalElements();
        } else {
            return (int) productService.count();
        }
    }
    
    private void openEditDialog(Product product) {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        
        boolean isNew = product == null;
        Product editProduct = isNew ? new Product() : product;
        
        H3 dialogTitle = new H3(isNew ? "Nuovo Prodotto" : "Modifica Prodotto");
        
        // Form fields
        TextField nameField = new TextField("Nome");
        nameField.setWidthFull();
        nameField.setRequired(true);
        
        ComboBox<String> categoryField = new ComboBox<>("Categoria");
        categoryField.setItems("Elettronica", "Accessori", "Audio", "Componenti", 
                              "Rete", "Periferiche", "Arredamento", "Smart Home", "Storage");
        categoryField.setWidthFull();
        categoryField.setRequired(true);
        
        BigDecimalField priceField = new BigDecimalField("Prezzo (€)");
        priceField.setWidthFull();
        priceField.setRequired(true);
        priceField.setPrefixComponent(new Span("€"));
        
        IntegerField quantityField = new IntegerField("Quantità");
        quantityField.setWidthFull();
        quantityField.setRequired(true);
        quantityField.setMin(0);
        quantityField.setStepButtonsVisible(true);
        
        TextArea descriptionField = new TextArea("Descrizione");
        descriptionField.setWidthFull();
        descriptionField.setMaxLength(500);
        descriptionField.setHelperText("Massimo 500 caratteri");
        
        // Binder for validation
        Binder<Product> binder = new Binder<>(Product.class);
        binder.forField(nameField)
            .asRequired("Il nome è obbligatorio")
            .bind(Product::getName, Product::setName);
        binder.forField(categoryField)
            .asRequired("La categoria è obbligatoria")
            .bind(Product::getCategory, Product::setCategory);
        binder.forField(priceField)
            .asRequired("Il prezzo è obbligatorio")
            .bind(Product::getPrice, Product::setPrice);
        binder.forField(quantityField)
            .asRequired("La quantità è obbligatoria")
            .bind(Product::getQuantity, Product::setQuantity);
        binder.forField(descriptionField)
            .bind(Product::getDescription, Product::setDescription);
        
        binder.readBean(editProduct);
        
        // Form layout
        FormLayout formLayout = new FormLayout();
        formLayout.add(nameField, categoryField, priceField, quantityField, descriptionField);
        formLayout.setColspan(descriptionField, 2);
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        
        // Buttons
        Button saveButton = new Button("Salva", e -> {
            try {
                binder.writeBean(editProduct);
                productService.save(editProduct);
                
                Notification notification = Notification.show(
                    isNew ? "Prodotto creato con successo!" : "Prodotto aggiornato con successo!",
                    3000,
                    Notification.Position.TOP_CENTER
                );
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                
                loadProducts();
                dialog.close();
            } catch (ValidationException ex) {
                Notification notification = Notification.show(
                    "Compila tutti i campi obbligatori",
                    3000,
                    Notification.Position.TOP_CENTER
                );
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Button cancelButton = new Button("Annulla", e -> dialog.close());
        
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);
        buttonLayout.setWidthFull();
        buttonLayout.getStyle().set("margin-top", "20px");
        
        VerticalLayout dialogLayout = new VerticalLayout(dialogTitle, formLayout, buttonLayout);
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        
        dialog.add(dialogLayout);
        dialog.open();
    }
    
    private void confirmDelete(Product product) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Conferma eliminazione");
        confirmDialog.setText(
            String.format("Sei sicuro di voler eliminare il prodotto '%s'? Questa azione non può essere annullata.",
                product.getName())
        );
        
        confirmDialog.setCancelable(true);
        confirmDialog.setCancelText("Annulla");
        
        confirmDialog.setConfirmText("Elimina");
        confirmDialog.setConfirmButtonTheme("error primary");
        
        confirmDialog.addConfirmListener(e -> {
            try {
                productService.delete(product.getId());
                
                Notification notification = Notification.show(
                    "Prodotto eliminato con successo!",
                    3000,
                    Notification.Position.TOP_CENTER
                );
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                
                // Se la pagina corrente diventa vuota, torna alla pagina precedente
                if (grid.getListDataView().getItemCount() == 1 && currentPage > 0) {
                    currentPage--;
                }
                
                loadProducts();
            } catch (Exception ex) {
                Notification notification = Notification.show(
                    "Errore durante l'eliminazione: " + ex.getMessage(),
                    5000,
                    Notification.Position.TOP_CENTER
                );
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        
        confirmDialog.open();
    }
}
