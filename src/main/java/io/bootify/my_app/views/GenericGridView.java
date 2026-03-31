package io.bootify.my_app.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.bootify.my_app.component.GenericFormDialog;
import io.bootify.my_app.component.GenericPaginatedGrid;
import io.bootify.my_app.component.StructuredTree;
import io.bootify.my_app.domain.Product;
import io.bootify.my_app.model.TreeResponse;
import io.bootify.my_app.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import com.vaadin.flow.shared.Registration;

import java.util.List;
import java.util.Set;

import java.math.BigDecimal;

@Route(value = "generic-grid", layout = MainLayout.class)
@PageTitle("Generic Grid - Prodotti")
public class GenericGridView extends VerticalLayout {

    private static final int POLL_INTERVAL_MS = 10_000;
    private static final Logger log = LoggerFactory.getLogger(GenericGridView.class);

    private final ProductService productService;
    private final GenericPaginatedGrid<Product> productGrid = new GenericPaginatedGrid<>();
    private final ComboBox<String> categoryFilter = new ComboBox<>("Categoria");
    private final TextField searchField = new TextField();
    private final Span totalLabel = new Span();
    private final StructuredTree structuredTree = new StructuredTree();
    private long cachedTotal = 0;

    // Bulk action bar (multi-select)
    private final Span selectionLabel = new Span();
    private final HorizontalLayout bulkActionBar = new HorizontalLayout();
    private Registration pollRegistration;

    public GenericGridView(@Autowired ProductService productService) {
        this.productService = productService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        // Titolo
        H2 title = new H2("Generic Grid con CRUD");

        // Totale elementi
        totalLabel.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("margin-bottom", "var(--lumo-space-m)");
        updateTotalLabel();

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

            org.springframework.data.domain.Page<Product> page;
            if (!searchTerm.isEmpty() && !category.isEmpty()) {
                page = productService.searchByCategory(category, searchTerm, pageable);
            } else if (!searchTerm.isEmpty()) {
                page = productService.search(searchTerm, pageable);
            } else if (!category.isEmpty()) {
                page = productService.findByCategory(category, pageable);
            } else {
                page = productService.findAll(pageable);
            }
            
            // Cache il totale per evitare query duplicate
            cachedTotal = page.getTotalElements();
            updateTotalLabel();
            
            return page;
        });
        
        // Imposta ordinamento di default lato server per ID ascendente
        productGrid.setDefaultSort("id", org.springframework.data.domain.Sort.Direction.ASC);

        // Ricarica grid quando cambiano filtri (azzera selezione al cambio filtro)
        searchField.addValueChangeListener(e -> { productGrid.clearSelection(); refreshGridData("search-filter-change"); });
        categoryFilter.addValueChangeListener(e -> { productGrid.clearSelection(); refreshGridData("category-filter-change"); });

        // Filtro Strutturato
        Details strutturaDet = new Details();
        strutturaDet.setSummaryText("Filtro Strutturato");
        strutturaDet.setContent(structuredTree);
        strutturaDet.setOpened(false);
        strutturaDet.setWidthFull();
        structuredTree.setSelectionListener(item -> refreshGridData("structured-filter-change"));

        // Pulsante reset globale per tutti i filtri
        Button resetAllButton = new Button("Pulisci Filtri", new Icon(VaadinIcon.ERASER));
        resetAllButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetAllButton.addClickListener(e -> {
            searchField.clear();
            categoryFilter.setValue("Tutti");
            structuredTree.clearSelection();
            productGrid.clearSelection();
            refreshGridData("reset-filters");
            Notification.show("Filtri azzerati", 2000, Notification.Position.BOTTOM_CENTER);
        });

        // Abilita selezione multipla (opt-in — altre view che usano GenericPaginatedGrid rimangono invariate)
        productGrid.enableMultiSelect();
        productGrid.setSelectionChangeListener(selected -> {
            int count = selected.size();
            selectionLabel.setText(count + (count == 1 ? " elemento selezionato" : " elementi selezionati"));
            bulkActionBar.setVisible(count > 0);
        });

        // Bulk action bar — visibile solo quando c'è almeno un elemento selezionato
        Button deleteSelectedBtn = new Button("Elimina selezionati", new Icon(VaadinIcon.TRASH));
        deleteSelectedBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteSelectedBtn.addClickListener(e -> confirmDeleteSelected());

        Button clearSelectionBtn = new Button("Deseleziona tutti", new Icon(VaadinIcon.CLOSE_SMALL));
        clearSelectionBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearSelectionBtn.addClickListener(e -> productGrid.clearSelection());

        selectionLabel.getStyle()
                .set("font-weight", "600")
                .set("color", "var(--lumo-error-text-color)");

        bulkActionBar.setAlignItems(Alignment.CENTER);
        bulkActionBar.setSpacing(true);
        bulkActionBar.setWidthFull();
        bulkActionBar.getStyle()
                .set("padding", "8px 12px")
                .set("background-color", "var(--lumo-error-color-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)");
        bulkActionBar.add(selectionLabel, deleteSelectedBtn, clearSelectionBtn);
        bulkActionBar.setVisible(false);

        // Layout finale
        add(title, totalLabel, toolbar, filterLayout, strutturaDet, resetAllButton, bulkActionBar, productGrid);
        expand(productGrid);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        startPolling();
        registerBeforeUnloadHandler();
        refreshGridData("attach");
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        unregisterBeforeUnloadHandler();
        stopPolling();
        super.onDetach(detachEvent);
    }
    
    private HorizontalLayout createToolbar() {
        Button addButton = new Button("Aggiungi", new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openAddDialog());
        
        Button refreshButton = new Button("Aggiorna", new Icon(VaadinIcon.REFRESH));
        refreshButton.addClickListener(e -> refreshGridData("toolbar-button"));
        
        HorizontalLayout toolbar = new HorizontalLayout(addButton, refreshButton);
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        toolbar.setSpacing(true);
        
        return toolbar;
    }

    private void startPolling() {
        getUI().ifPresent(ui -> {
            ui.setPollInterval(POLL_INTERVAL_MS);
            if (pollRegistration == null) {
                pollRegistration = ui.addPollListener(event -> refreshGridData("polling-10s"));
            }
        });
    }

    private void stopPolling() {
        if (pollRegistration != null) {
            pollRegistration.remove();
            pollRegistration = null;
        }
        getUI().ifPresent(ui -> ui.setPollInterval(-1));
    }

    private void refreshGridData(String source) {
        log.info("GenericGrid refresh triggered: source={}, search='{}', category='{}'",
                source,
                searchField.getValue(),
                categoryFilter.getValue());
        productGrid.refresh();
    }

    private void registerBeforeUnloadHandler() {
        getElement().executeJs(
                "if (!$0.__genericGridBeforeUnloadHandler) {"
                        + "  $0.__genericGridBeforeUnloadHandler = () => $0.$server.onBeforeUnload();"
                        + "  window.addEventListener('beforeunload', $0.__genericGridBeforeUnloadHandler);"
                        + "}",
                getElement());
    }

    private void unregisterBeforeUnloadHandler() {
        getElement().executeJs(
                "if ($0.__genericGridBeforeUnloadHandler) {"
                        + "  window.removeEventListener('beforeunload', $0.__genericGridBeforeUnloadHandler);"
                        + "  delete $0.__genericGridBeforeUnloadHandler;"
                        + "}",
                getElement());
    }

    @ClientCallable
    private void onBeforeUnload() {
        log.info("GenericGrid beforeunload received, stopping polling");
        stopPolling();
    }
    
    private void configureGrid() {
        productGrid.getGrid().addColumn(Product::getId).setHeader("ID").setSortable(true).setKey("id");
        productGrid.getGrid().addColumn(Product::getName).setHeader("Nome").setSortable(true).setKey("name");
        productGrid.getGrid().addColumn(Product::getCategory).setHeader("Categoria").setSortable(true).setKey("category");
        productGrid.getGrid().addColumn(product -> String.format("€ %.2f", product.getPrice()))
                .setHeader("Prezzo").setSortable(true).setKey("price");
        productGrid.getGrid().addColumn(Product::getQuantity).setHeader("Qtà").setSortable(true).setKey("quantity");
        productGrid.getGrid().addColumn(Product::getDescription).setHeader("Descrizione").setKey("description");

        // Colonna Struttura: mostra il nodo selezionato nel Filtro Strutturato
        productGrid.getGrid().addColumn(new ComponentRenderer<>(product -> {
            TreeResponse selected = structuredTree.getSelectedItem();
            if (selected != null) {
                Span badge = new Span(selected.getType() + " › " + selected.getDescrizione());
                badge.getElement().getThemeList().add("badge");
                badge.getStyle()
                        .set("background-color", "var(--lumo-primary-color-10pct)")
                        .set("color", "var(--lumo-primary-text-color)")
                        .set("font-size", "var(--lumo-font-size-xs)")
                        .set("white-space", "nowrap");
                return badge;
            }
            Span empty = new Span("—");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)");
            return empty;
        })).setHeader("Struttura").setKey("struttura").setWidth("220px").setFlexGrow(0);

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
        Product newProduct = new Product();
        Binder<Product> binder = new Binder<>(Product.class);
        
        GenericFormDialog<Product> dialog = new GenericFormDialog<>(
                "Prodotto",
                newProduct,
                binder,
                this::configureProductForm,
                this::saveProduct
        );
        dialog.open();
    }
    
    private void openEditDialog(Product product) {
        Binder<Product> binder = new Binder<>(Product.class);
        
        GenericFormDialog<Product> dialog = new GenericFormDialog<>(
                "Prodotto",
                product,
                binder,
                this::configureProductForm,
                this::saveProduct
        );
        dialog.open();
    }
    
    private void configureProductForm(FormLayout formLayout, Binder<Product> binder) {
        // Campi del form
        TextField nameField = new TextField("Nome");
        nameField.setWidthFull();
        nameField.setRequiredIndicatorVisible(true);
        
        ComboBox<String> categoryField = new ComboBox<>("Categoria");
        categoryField.setItems("Elettronica", "Accessori", "Audio", "Componenti",
                "Rete", "Periferiche", "Arredamento", "Smart Home", "Storage");
        categoryField.setAllowCustomValue(true);
        categoryField.addCustomValueSetListener(e -> categoryField.setValue(e.getDetail()));
        categoryField.setWidthFull();
        categoryField.setRequiredIndicatorVisible(true);
        
        NumberField priceField = new NumberField("Prezzo");
        priceField.setPrefixComponent(new com.vaadin.flow.component.html.Span("€"));
        priceField.setMin(0);
        priceField.setWidthFull();
        priceField.setRequiredIndicatorVisible(true);
        
        IntegerField quantityField = new IntegerField("Quantità");
        quantityField.setMin(0);
        quantityField.setWidthFull();
        quantityField.setRequiredIndicatorVisible(true);
        
        TextArea descriptionField = new TextArea("Descrizione");
        descriptionField.setWidthFull();
        descriptionField.setMinHeight("100px");
        
        // Binding
        binder.forField(nameField)
                .asRequired("Nome è obbligatorio")
                .bind(Product::getName, Product::setName);
        
        binder.forField(categoryField)
                .asRequired("Categoria è obbligatoria")
                .bind(Product::getCategory, Product::setCategory);
        
        binder.forField(priceField)
                .asRequired("Prezzo è obbligatorio")
                .withConverter(
                        value -> value != null ? BigDecimal.valueOf(value) : null,
                        value -> value != null ? value.doubleValue() : null
                )
                .bind(Product::getPrice, Product::setPrice);
        
        binder.forField(quantityField)
                .asRequired("Quantità è obbligatoria")
                .bind(Product::getQuantity, Product::setQuantity);
        
        binder.forField(descriptionField)
                .bind(Product::getDescription, Product::setDescription);
        
        // Layout
        formLayout.add(nameField, categoryField, priceField, quantityField, descriptionField);
        formLayout.setColspan(descriptionField, 2);
    }
    
    private void saveProduct(Product product) {
        productService.save(product);
        refreshGridData("save-product");
    }
    
    private void updateTotalLabel() {
        String searchTerm = searchField.getValue() != null ? searchField.getValue() : "";
        String category = categoryFilter.getValue() != null && !"Tutti".equals(categoryFilter.getValue())
                ? categoryFilter.getValue() : "";
        
        String filterText = (!searchTerm.isEmpty() || !category.isEmpty()) ? " (filtrati)" : " totali";
        totalLabel.setText(cachedTotal + " prodott" + (cachedTotal != 1 ? "i" : "o") + filterText);
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
            refreshGridData("delete-product");
            Notification.show("Prodotto eliminato con successo", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Errore durante l'eliminazione: " + e.getMessage(),
                    3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmDeleteSelected() {
        Set<Product> selected = productGrid.getSelectedItems();
        if (selected.isEmpty()) return;
        int count = selected.size();

        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Conferma eliminazione massiva");
        dialog.setText("Stai per eliminare " + count + (count == 1 ? " prodotto" : " prodotti") +
                ". L'operazione è irreversibile. Continuare?");
        dialog.setCancelable(true);
        dialog.setCancelText("Annulla");
        dialog.setConfirmText("Elimina " + count);
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            try {
                List<Long> ids = selected.stream().map(Product::getId).toList();
                productService.deleteByIds(ids);
                productGrid.clearSelection();
                refreshGridData("bulk-delete");
                Notification.show(count + (count == 1 ? " prodotto eliminato" : " prodotti eliminati"),
                        3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Errore durante l'eliminazione: " + ex.getMessage(),
                        3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        dialog.open();
    }
}