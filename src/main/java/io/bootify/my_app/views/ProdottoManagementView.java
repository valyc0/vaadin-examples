package io.bootify.my_app.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
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
import io.bootify.my_app.service.PriceFileWatcherService;
import io.bootify.my_app.service.ProductService;
import io.bootify.my_app.service.ProductFileUploadService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

@Route(value = "prodotti", layout = MainLayout.class)
@PageTitle("Gestione Prodotti")
public class ProdottoManagementView extends VerticalLayout {

    private final ProductService productService;
    private final ProductFileUploadService uploadService;
    private final PriceFileWatcherService priceFileWatcherService;
    private final Grid<Product> grid;
    private final TextField searchField;
    private final Span statsLabel;
    private final Span livePriceValueSpan;
    private Consumer<BigDecimal> priceListener;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    public ProdottoManagementView(ProductService productService,
                                  ProductFileUploadService uploadService,
                                  PriceFileWatcherService priceFileWatcherService) {
        this.productService = productService;
        this.uploadService = uploadService;
        this.priceFileWatcherService = priceFileWatcherService;

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

        // --- Pannello prezzo live iPhone 14 Pro (Push) ---
        livePriceValueSpan = new Span("—");
        livePriceValueSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)");

        Span liveBadge = new Span("● LIVE");
        liveBadge.getStyle()
                .set("background-color", "#e53e3e")
                .set("color", "white")
                .set("font-size", "var(--lumo-font-size-xxs)")
                .set("font-weight", "bold")
                .set("padding", "2px 7px")
                .set("border-radius", "12px")
                .set("letter-spacing", "1px");

        Span iphoneLabel = new Span("iPhone 14 Pro – prezzo da file");
        iphoneLabel.getStyle()
                .set("font-weight", "600")
                .set("font-size", "var(--lumo-font-size-s)");

        Span iphoneIcon = new Span("📱");
        iphoneIcon.getStyle().set("font-size", "var(--lumo-font-size-l)");

        HorizontalLayout livePriceLabelRow = new HorizontalLayout(iphoneIcon, iphoneLabel, liveBadge);
        livePriceLabelRow.setAlignItems(FlexComponent.Alignment.CENTER);
        livePriceLabelRow.setSpacing(true);

        Span fileHint = new Span("Modifica data/iphone14pro_price.txt per aggiornare in tempo reale");
        fileHint.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout livePriceInfo = new VerticalLayout(livePriceLabelRow, fileHint);
        livePriceInfo.setPadding(false);
        livePriceInfo.setSpacing(false);

        HorizontalLayout livePricePanel = new HorizontalLayout(livePriceInfo, livePriceValueSpan);
        livePricePanel.setAlignItems(FlexComponent.Alignment.CENTER);
        livePricePanel.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        livePricePanel.setWidthFull();
        livePricePanel.getStyle()
                .set("padding", "var(--lumo-space-m) var(--lumo-space-l)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("border-left", "4px solid var(--lumo-primary-color)");
        // -------------------------------------------------

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
        VerticalLayout headerLayout = new VerticalLayout(title, statsLabel, livePricePanel, toolbar);
        headerLayout.setPadding(false);
        headerLayout.setSpacing(true);

        // Main content
        add(headerLayout, grid);
        setFlexGrow(1, grid);

        // Load data
        refreshGrid();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();
        priceListener = price -> ui.access(() ->
                livePriceValueSpan.setText("\u20ac " + price.toPlainString()));
        priceFileWatcherService.addListener(priceListener);
        // Inizializza subito con il valore corrente dal file
        BigDecimal current = priceFileWatcherService.getCurrentPrice();
        if (current != null) {
            livePriceValueSpan.setText("\u20ac " + current.toPlainString());
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (priceListener != null) {
            priceFileWatcherService.removeListener(priceListener);
        }
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
        // Create hidden upload component
        com.vaadin.flow.component.upload.receivers.MemoryBuffer buffer = new com.vaadin.flow.component.upload.receivers.MemoryBuffer();
        com.vaadin.flow.component.upload.Upload upload = new com.vaadin.flow.component.upload.Upload(buffer);
        upload.setMaxFiles(1);
        upload.setMaxFileSize(10 * 1024 * 1024); // 10MB
        upload.setAcceptedFileTypes(
                "application/pdf",
                "image/jpeg", "image/png", "image/gif",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "text/plain"
        );
        upload.getStyle().set("display", "none"); // Hide the upload component
        upload.setAutoUpload(true); // Auto-upload when file is selected
        
        // Create progress UI components (will be shown during upload)
        com.vaadin.flow.component.progressbar.ProgressBar progressBar = new com.vaadin.flow.component.progressbar.ProgressBar();
        progressBar.setMin(0);
        progressBar.setMax(100);
        progressBar.setValue(0);
        progressBar.setWidth("300px");
        
        com.vaadin.flow.component.html.Span progressText = new com.vaadin.flow.component.html.Span();
        progressText.getStyle()
                .set("font-weight", "500")
                .set("margin-bottom", "var(--lumo-space-xs)");
        
        com.vaadin.flow.component.html.Span fileInfo = new com.vaadin.flow.component.html.Span(
            String.format("Prodotto: %s", product.getName())
        );
        fileInfo.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");
        
        com.vaadin.flow.component.orderedlayout.VerticalLayout progressLayout = 
            new com.vaadin.flow.component.orderedlayout.VerticalLayout(
                progressText, 
                progressBar,
                fileInfo
            );
        progressLayout.setPadding(false);
        progressLayout.setSpacing(true);
        progressLayout.getStyle().set("align-items", "flex-start");
        
        Notification progressNotification = new Notification();
        progressNotification.add(progressLayout);
        progressNotification.setPosition(Notification.Position.TOP_CENTER);
        progressNotification.setDuration(0); // Keep open until closed manually
        
        // Handle upload start
        upload.addStartedListener(event -> {
            String fileName = event.getFileName();
            progressBar.setValue(0);
            progressText.setText("Caricamento: " + fileName + " - 0%%");
            progressNotification.open();
        });
        
        // Handle upload progress
        upload.addProgressListener(event -> {
            long contentLength = event.getContentLength();
            long bytesRead = event.getReadBytes();
            
            if (contentLength > 0) {
                double percentage = (bytesRead * 100.0) / contentLength;
                progressBar.setValue(percentage);
                progressText.setText(String.format("Caricamento: %.0f%%%%", percentage));
            }
        });
        
        // Handle successful upload
        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            
            // Update to 100%
            progressBar.setValue(100);
            progressText.setText("Caricamento: 100%%");
            
            try {
                java.io.InputStream inputStream = buffer.getInputStream();
                String contentType = buffer.getFileData().getMimeType();
                
                byte[] fileBytes = inputStream.readAllBytes();
                
                uploadService.uploadFileForProduct(product.getId(), fileBytes, fileName, 
                                                  contentType, "admin");
                
                // Close progress notification
                progressNotification.close();
                
                // Show success notification
                Notification success = Notification.show(
                    "✓ File caricato con successo: " + fileName, 
                    3000, 
                    Notification.Position.BOTTOM_START
                );
                success.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                
                refreshGrid();
            } catch (Exception ex) {
                // Close progress notification
                progressNotification.close();
                
                Notification.show("Errore durante il caricamento: " + ex.getMessage(),
                        5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                ex.printStackTrace();
            }
        });
        
        upload.addFailedListener(event -> {
            progressNotification.close();
            Notification.show("Errore durante il caricamento: " + event.getReason().getMessage(),
                    5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
        
        upload.addFileRejectedListener(event -> {
            progressNotification.close();
            Notification.show("File rifiutato: " + event.getErrorMessage(),
                    3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        // Upload button that triggers the hidden upload
        Button uploadButton = new Button(new Icon(VaadinIcon.UPLOAD));
        uploadButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SUCCESS);
        uploadButton.setTooltipText("Carica file");
        uploadButton.getElement().addEventListener("click", e -> {
            upload.getElement().executeJs("this.shadowRoot.querySelector('input[type=file]').click()");
        });

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

        HorizontalLayout actions = new HorizontalLayout(upload, uploadButton, downloadButton, editButton, deleteButton);
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
