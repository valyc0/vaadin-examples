package io.bootify.my_app.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.bootify.my_app.domain.Product;
import io.bootify.my_app.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "prodotti", layout = MainLayout.class)
@PageTitle("Gestione Prodotti")
public class ProdottoManagementView extends VerticalLayout {

    private final ProductService productService;
    private final Grid<Product> grid;
    private final TextField searchField;
    private final Span statsLabel;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    public ProdottoManagementView(ProductService productService) {
        this.productService = productService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Header
        H1 title = new H1("Gestione Prodotti");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        // Stats
        statsLabel = new Span();
        updateStats();
        statsLabel.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        // Search field
        searchField = new TextField();
        searchField.setPlaceholder("Cerca prodotto per nome...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidth("300px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> filterProducts(e.getValue()));
        searchField.setClearButtonVisible(true);

        // Add button
        Button addButton = new Button("Aggiungi Prodotto", new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openAddDialog());

        // Refresh button
        Button refreshButton = new Button("Aggiorna", new Icon(VaadinIcon.REFRESH));
        refreshButton.addClickListener(e -> refreshGrid());

        // Toolbar
        HorizontalLayout toolbar = new HorizontalLayout(searchField, addButton, refreshButton);
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        toolbar.getStyle().set("flex-wrap", "wrap");

        // Grid
        grid = new Grid<>(Product.class, false);
        configureGrid();

        // Header layout
        VerticalLayout headerLayout = new VerticalLayout(title, statsLabel, toolbar);
        headerLayout.setPadding(false);
        headerLayout.setSpacing(false);

        // Main content
        add(headerLayout, grid);
        setFlexGrow(1, grid);

        // Load data
        refreshGrid();
    }

    private void configureGrid() {
        grid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_COMPACT,
                GridVariant.LUMO_WRAP_CELL_CONTENT
        );
        grid.setSizeFull();

        // Product name column with icon for file
        grid.addColumn(new ComponentRenderer<>(product -> {
            Icon icon = product.hasFile() ? new Icon(VaadinIcon.PAPERCLIP) : new Icon(VaadinIcon.PACKAGE);
            icon.getStyle().set("margin-right", "var(--lumo-space-s)");
            if (product.hasFile()) {
                icon.setColor("var(--lumo-primary-color)");
            }
            
            Span name = new Span(product.getName());
            name.getStyle().set("font-weight", "500");

            HorizontalLayout layout = new HorizontalLayout(icon, name);
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            layout.setSpacing(false);
            return layout;
        }))
        .setHeader("Nome Prodotto")
        .setAutoWidth(true)
        .setFlexGrow(2)
        .setSortable(true);

        // Category
        grid.addColumn(Product::getCategory)
                .setHeader("Categoria")
                .setAutoWidth(true)
                .setSortable(true);

        // Price
        grid.addColumn(product -> "€ " + product.getPrice())
                .setHeader("Prezzo")
                .setAutoWidth(true)
                .setSortable(true);

        // Quantity
        grid.addColumn(Product::getQuantity)
                .setHeader("Quantità")
                .setAutoWidth(true)
                .setSortable(true);

        // Uploaded by
        grid.addColumn(new ComponentRenderer<>(product -> {
            String uploadedBy = product.getUploadedBy();
            if (uploadedBy != null && !uploadedBy.isEmpty()) {
                return new Span(uploadedBy);
            }
            Span empty = new Span("—");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)");
            return empty;
        }))
        .setHeader("Caricato da")
        .setAutoWidth(true);

        // Date created
        grid.addColumn(product -> product.getDateCreated() != null ? 
                product.getDateCreated().format(DATE_FORMATTER) : "—")
                .setHeader("Data Creazione")
                .setAutoWidth(true)
                .setSortable(true);

        // File info
        grid.addColumn(new ComponentRenderer<>(product -> {
            if (product.hasFile()) {
                Span fileInfo = new Span(product.getFileName());
                fileInfo.getStyle()
                        .set("font-size", "var(--lumo-font-size-s)")
                        .set("color", "var(--lumo-secondary-text-color)");
                return fileInfo;
            }
            Span noFile = new Span("—");
            noFile.getStyle().set("color", "var(--lumo-secondary-text-color)");
            return noFile;
        }))
        .setHeader("File Allegato")
        .setAutoWidth(true)
        .setFlexGrow(1);

        // Metadata indicator
        grid.addColumn(new ComponentRenderer<>(product -> {
            if (product.getMetadata() != null && !product.getMetadata().isEmpty()) {
                Icon icon = new Icon(VaadinIcon.INFO_CIRCLE);
                icon.setColor("var(--lumo-primary-color)");
                icon.setSize("20px");
                icon.setTooltipText("Contiene metadati");
                return icon;
            }
            return new Span();
        }))
        .setHeader("Metadati")
        .setAutoWidth(true)
        .setFlexGrow(0);

        // Actions column
        grid.addColumn(new ComponentRenderer<>(this::createActionsLayout))
                .setHeader("Azioni")
                .setAutoWidth(true)
                .setFlexGrow(0);
    }

    private Component createActionsLayout(Product product) {
        Button downloadButton = new Button(new Icon(VaadinIcon.DOWNLOAD));
        downloadButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        downloadButton.setTooltipText("Scarica file");
        downloadButton.setEnabled(product.hasFile());
        downloadButton.addClickListener(e -> downloadFile(product));

        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editButton.setTooltipText("Modifica");
        editButton.addClickListener(e -> editProduct(product));

        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteButton.setTooltipText("Elimina");
        deleteButton.addClickListener(e -> confirmDelete(product));

        HorizontalLayout actions = new HorizontalLayout(downloadButton, editButton, deleteButton);
        actions.setSpacing(false);
        return actions;
    }

    private void openAddDialog() {
        ProdottoUploadDialog dialog = new ProdottoUploadDialog(productService, product -> {
            refreshGrid();
            Notification.show("Prodotto creato: " + product.getName(), 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        dialog.open();
    }

    private void editProduct(Product product) {
        ProdottoEditDialog dialog = new ProdottoEditDialog(product, productService, updatedProduct -> {
            refreshGrid();
        });
        dialog.open();
    }

    private void downloadFile(Product product) {
        if (!product.hasFile()) {
            Notification.show("Nessun file allegato a questo prodotto", 2000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        // Use REST endpoint for download - more reliable than StreamResource
        String downloadUrl = "/api/prodotti/download/" + product.getId();
        
        getUI().ifPresent(ui -> {
            ui.getPage().executeJs(
                "const link = document.createElement('a');" +
                "link.href = $0;" +
                "link.download = $1;" +
                "document.body.appendChild(link);" +
                "link.click();" +
                "document.body.removeChild(link);",
                downloadUrl,
                product.getFileName()
            );
        });

        Notification.show("Download avviato: " + product.getFileName(), 2000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void confirmDelete(Product product) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Conferma eliminazione");
        dialog.setText("Sei sicuro di voler eliminare il prodotto \"" + product.getName() + "\"? Questa operazione non può essere annullata.");
        
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
            refreshGrid();
            Notification.show("Prodotto eliminato: " + product.getName(), 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Errore durante l'eliminazione: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void filterProducts(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            refreshGrid();
        } else {
            List<Product> products = productService.findAll().stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                               (p.getCategory() != null && p.getCategory().toLowerCase().contains(searchTerm.toLowerCase())) ||
                               (p.getDescription() != null && p.getDescription().toLowerCase().contains(searchTerm.toLowerCase())))
                    .toList();
            grid.setItems(products);
        }
    }

    private void refreshGrid() {
        List<Product> products = productService.findAll();
        grid.setItems(products);
        updateStats();
        searchField.clear();
    }

    private void updateStats() {
        long count = productService.count();
        statsLabel.setText(count + " prodott" + (count != 1 ? "i totali" : "o totale"));
    }
}
