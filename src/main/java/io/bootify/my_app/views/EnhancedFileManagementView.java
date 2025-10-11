package io.bootify.my_app.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.bootify.my_app.domain.FileUpload;
import io.bootify.my_app.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "enhanced-files", layout = MainLayout.class)
@PageTitle("Gestione File Avanzata")
public class EnhancedFileManagementView extends VerticalLayout {

    private final FileUploadService fileUploadService;
    private final Grid<FileUpload> grid;
    
    // Search filters
    private TextField searchField;
    private ComboBox<String> categoryFilter;
    private ComboBox<String> statusFilter;
    private TextField ownerFilter;
    
    // Upload components
    private Upload upload;
    private MemoryBuffer buffer;
    private ComboBox<String> uploadCategoryCombo;
    private TextArea uploadDescriptionArea;
    private TextField uploadOwnerField;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    // Predefined categories
    private static final List<String> CATEGORIES = Arrays.asList(
        "Documenti", "Immagini", "Fatture", "Contratti", 
        "Report", "Presentazioni", "Altro"
    );
    
    // Predefined statuses
    private static final List<String> STATUSES = Arrays.asList(
        "PENDING", "APPROVED", "REJECTED", "ARCHIVED"
    );

    @Autowired
    public EnhancedFileManagementView(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;

        // Configure the main layout
        setWidthFull();
        setPadding(true);
        setSpacing(true);
        // Remove setSizeFull to allow natural scrolling
        getStyle()
            .set("max-width", "100%")
            .set("box-sizing", "border-box");

        // Header
        H2 title = new H2("Gestione File Avanzata");
        title.addClassNames(LumoUtility.Margin.Bottom.SMALL);
        title.getStyle().set("margin-top", "0");

        // Create upload section
        Component uploadSection = createUploadSection();
        
        // Create search filters
        Component filterSection = createFilterSection();

        // Grid - with fixed height
        grid = new Grid<>(FileUpload.class, false);
        configureGrid();
        grid.setHeight("500px");
        grid.setWidthFull();

        // Add all components - they will stack vertically and page will scroll naturally
        add(title, uploadSection, filterSection, grid);

        // Load data
        refreshGrid();
    }

    private Component createUploadSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(true);
        section.addClassNames(
            LumoUtility.Border.ALL,
            LumoUtility.BorderRadius.MEDIUM,
            LumoUtility.Background.CONTRAST_5
        );
        section.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        H3 uploadTitle = new H3("📤 Carica Nuovo File");
        uploadTitle.getStyle()
            .set("margin-top", "0")
            .set("margin-bottom", "var(--lumo-space-s)")
            .set("font-size", "var(--lumo-font-size-l)");

        // Step 1: Category selection (always visible)
        uploadCategoryCombo = new ComboBox<>("Tipologia *");
        uploadCategoryCombo.setItems(CATEGORIES);
        uploadCategoryCombo.setPlaceholder("Seleziona la tipologia del file...");
        uploadCategoryCombo.setRequired(true);
        uploadCategoryCombo.setAllowCustomValue(true);
        uploadCategoryCombo.addCustomValueSetListener(e -> {
            uploadCategoryCombo.setValue(e.getDetail());
        });
        uploadCategoryCombo.setWidthFull();

        // Container for the rest of the form (initially hidden)
        VerticalLayout uploadFormContainer = new VerticalLayout();
        uploadFormContainer.setPadding(false);
        uploadFormContainer.setSpacing(true);
        uploadFormContainer.setVisible(false); // Initially hidden

        // Upload form layout
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );

        // Owner field
        uploadOwnerField = new TextField("Proprietario");
        uploadOwnerField.setPlaceholder("Nome proprietario...");
        uploadOwnerField.setValue("Utente Corrente");

        // Description
        uploadDescriptionArea = new TextArea("Descrizione");
        uploadDescriptionArea.setPlaceholder("Descrizione opzionale del file...");
        uploadDescriptionArea.setMaxLength(500);

        formLayout.add(uploadOwnerField, 2);
        formLayout.add(uploadDescriptionArea, 2);

        // File upload component
        buffer = new MemoryBuffer();
        upload = new Upload(buffer);
        upload.setMaxFiles(1);
        upload.setMaxFileSize(10 * 1024 * 1024); // 10MB
        upload.setAcceptedFileTypes(
            "image/*", 
            "application/pdf", 
            ".doc", ".docx", 
            ".xls", ".xlsx",
            ".txt"
        );
        
        // DISABLE auto-upload - user must click upload button
        upload.setAutoUpload(false);

        Icon uploadIcon = VaadinIcon.UPLOAD.create();
        uploadIcon.getStyle().set("color", "var(--lumo-primary-color)");
        
        Div dropLabel = new Div();
        dropLabel.add(uploadIcon);
        dropLabel.add(new Span(" Trascina il file qui o clicca per selezionare"));
        dropLabel.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("gap", "8px");
        
        upload.setDropLabel(dropLabel);
        
        // Custom select button
        Button selectFileButton = new Button("Seleziona File", new Icon(VaadinIcon.FOLDER_OPEN));
        upload.setUploadButton(selectFileButton);
        
        // Custom upload button (to actually upload the file)
        Button uploadButton = new Button("Carica File", new Icon(VaadinIcon.UPLOAD));
        uploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        uploadButton.addClickListener(e -> {
            System.out.println("🚀 Pulsante Carica cliccato");
            upload.getElement().callJsFunction("uploadFiles");
        });

        // Add file rejected listener
        upload.addFileRejectedListener(event -> {
            System.out.println("❌ File rifiutato: " + event.getErrorMessage());
            Notification.show("❌ File rifiutato: " + event.getErrorMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        // Add started listener
        upload.addStartedListener(event -> {
            System.out.println("🚀 Upload iniziato: " + event.getFileName());
            Notification.show("📤 Caricamento di " + event.getFileName() + "...", 2000, Notification.Position.BOTTOM_START);
        });

        upload.addSucceededListener(event -> {
            System.out.println("✅ Upload completato: " + event.getFileName());
            try {
                String fileName = event.getFileName();
                String mimeType = event.getMIMEType();
                
                System.out.println("📋 File: " + fileName + ", Tipo: " + mimeType);

                InputStream inputStream = buffer.getInputStream();
                byte[] fileData = inputStream.readAllBytes();
                
                System.out.println("📦 Dimensione file: " + fileData.length + " bytes");

                FileUpload fileUpload = new FileUpload();
                fileUpload.setFileName(fileName);
                fileUpload.setFileType(mimeType);
                fileUpload.setFileSize((long) fileData.length);
                fileUpload.setFileData(fileData);
                fileUpload.setUploadedBy(uploadOwnerField.getValue());
                fileUpload.setCategory(uploadCategoryCombo.getValue());
                fileUpload.setDescription(uploadDescriptionArea.getValue());
                fileUpload.setStatus("PENDING");

                System.out.println("💾 Salvataggio file nel database...");
                FileUpload saved = fileUploadService.save(fileUpload);
                System.out.println("✅ File salvato con ID: " + saved.getId());
                
                System.out.println("🔄 Aggiornamento griglia...");
                refreshGrid();
                
                System.out.println("🧹 Pulizia form...");
                clearUploadForm();
                
                System.out.println("✅ Operazione completata con successo!");
                Notification.show("✅ File caricato con successo: " + fileName, 5000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } catch (IOException e) {
                System.err.println("❌ Errore I/O: " + e.getMessage());
                e.printStackTrace();
                Notification.show("❌ Errore durante il caricamento: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception e) {
                System.err.println("❌ Errore imprevisto: " + e.getMessage());
                e.printStackTrace();
                Notification.show("❌ Errore imprevisto: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        upload.addFailedListener(event -> {
            System.err.println("❌ Upload fallito: " + event.getReason().getMessage());
            event.getReason().printStackTrace();
            Notification.show("❌ Upload fallito: " + event.getReason().getMessage(), 5000, Notification.Position.MIDDLE)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        // Add upload component and button to the form container
        uploadFormContainer.add(formLayout, upload, uploadButton);

        // Add listener to show/hide form based on category selection
        uploadCategoryCombo.addValueChangeListener(e -> {
            if (e.getValue() != null && !e.getValue().isEmpty()) {
                uploadFormContainer.setVisible(true);
                Notification.show("✅ Tipologia selezionata: " + e.getValue() + ". Ora puoi selezionare e caricare il file.", 
                    3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                uploadFormContainer.setVisible(false);
            }
        });

        section.add(uploadTitle, uploadCategoryCombo, uploadFormContainer);
        return section;
    }

    private Component createFilterSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(false);
        section.addClassNames(
            LumoUtility.Border.ALL,
            LumoUtility.BorderRadius.MEDIUM,
            LumoUtility.Background.CONTRAST_5
        );
        section.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        H3 filterTitle = new H3("🔍 Filtri di Ricerca");
        filterTitle.getStyle()
            .set("margin-top", "0")
            .set("margin-bottom", "var(--lumo-space-s)")
            .set("font-size", "var(--lumo-font-size-l)");

        // Search field
        searchField = new TextField();
        searchField.setPlaceholder("Cerca per nome file...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidth("100%");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> applyFilters());
        searchField.setClearButtonVisible(true);

        // Category filter
        categoryFilter = new ComboBox<>("Tipologia");
        categoryFilter.setItems(CATEGORIES);
        categoryFilter.setPlaceholder("Tutte le tipologie");
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.addValueChangeListener(e -> applyFilters());

        // Status filter
        statusFilter = new ComboBox<>("Status");
        statusFilter.setItems(STATUSES);
        statusFilter.setPlaceholder("Tutti gli status");
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> applyFilters());

        // Owner filter
        ownerFilter = new TextField("Proprietario");
        ownerFilter.setPlaceholder("Cerca per proprietario...");
        ownerFilter.setClearButtonVisible(true);
        ownerFilter.setValueChangeMode(ValueChangeMode.LAZY);
        ownerFilter.addValueChangeListener(e -> applyFilters());

        // Clear all button
        Button clearFiltersButton = new Button("Cancella Filtri", new Icon(VaadinIcon.CLOSE_CIRCLE));
        clearFiltersButton.addClickListener(e -> clearFilters());

        // Refresh button
        Button refreshButton = new Button("Aggiorna", new Icon(VaadinIcon.REFRESH));
        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshButton.addClickListener(e -> refreshGrid());

        // Layout - more compact
        HorizontalLayout filterRow = new HorizontalLayout(
            searchField, categoryFilter, statusFilter, ownerFilter
        );
        filterRow.setWidthFull();
        filterRow.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        filterRow.getStyle().set("gap", "var(--lumo-space-s)");
        
        HorizontalLayout buttonRow = new HorizontalLayout(clearFiltersButton, refreshButton);
        buttonRow.setSpacing(true);
        buttonRow.getStyle().set("margin-top", "var(--lumo-space-s)");

        section.add(filterTitle, filterRow, buttonRow);
        return section;
    }

    private void configureGrid() {
        grid.addThemeVariants(
            GridVariant.LUMO_ROW_STRIPES,
            GridVariant.LUMO_COMPACT,
            GridVariant.LUMO_WRAP_CELL_CONTENT
        );
        grid.setSizeFull();

        // File name column with icon
        grid.addColumn(new ComponentRenderer<>(file -> {
            Icon icon = getFileIcon(file.getFileType());
            icon.getStyle()
                .set("margin-right", "var(--lumo-space-s)")
                .set("flex-shrink", "0");
            
            Span fileName = new Span(file.getFileName());
            fileName.getStyle().set("font-weight", "500");

            HorizontalLayout layout = new HorizontalLayout(icon, fileName);
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            layout.setSpacing(false);
            return layout;
        }))
        .setHeader("Nome File")
        .setAutoWidth(true)
        .setFlexGrow(2)
        .setSortable(true)
        .setKey("fileName");

        // Upload date column
        grid.addColumn(file -> file.getUploadDate().format(DATE_FORMATTER))
            .setHeader("Data Caricamento")
            .setAutoWidth(true)
            .setSortable(true)
            .setKey("uploadDate");

        // Owner column
        grid.addColumn(FileUpload::getUploadedBy)
            .setHeader("Proprietario")
            .setAutoWidth(true)
            .setSortable(true)
            .setKey("uploadedBy");

        // Category column with badge
        grid.addColumn(new ComponentRenderer<>(file -> {
            Span badge = new Span(file.getCategory() != null ? file.getCategory() : "N/A");
            badge.getElement().getThemeList().add("badge");
            badge.getStyle()
                .set("background-color", getCategoryColor(file.getCategory()))
                .set("color", "white")
                .set("padding", "4px 8px")
                .set("border-radius", "4px")
                .set("font-size", "0.875rem");
            return badge;
        }))
        .setHeader("Tipologia")
        .setAutoWidth(true)
        .setSortable(true)
        .setKey("category");

        // Status column with badge
        grid.addColumn(new ComponentRenderer<>(file -> {
            Span badge = new Span(file.getStatus() != null ? file.getStatus() : "PENDING");
            badge.getElement().getThemeList().add("badge");
            badge.getStyle()
                .set("background-color", getStatusColor(file.getStatus()))
                .set("color", "white")
                .set("padding", "4px 8px")
                .set("border-radius", "4px")
                .set("font-size", "0.875rem");
            return badge;
        }))
        .setHeader("Status")
        .setAutoWidth(true)
        .setSortable(true)
        .setKey("status");

        // Actions column
        grid.addColumn(new ComponentRenderer<>(this::createActionsLayout))
            .setHeader("Azioni")
            .setAutoWidth(true)
            .setFlexGrow(0);
    }

    private Component createActionsLayout(FileUpload file) {
        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteButton.setTooltipText("Cancella");
        deleteButton.addClickListener(e -> confirmDelete(file));

        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editButton.setTooltipText("Modifica");
        editButton.addClickListener(e -> openEditDialog(file));

        Button approveButton = new Button(new Icon(VaadinIcon.CHECK_CIRCLE));
        approveButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SUCCESS);
        approveButton.setTooltipText("Approva");
        approveButton.addClickListener(e -> approveFile(file));
        approveButton.setEnabled(!"APPROVED".equals(file.getStatus()));

        HorizontalLayout actions = new HorizontalLayout(deleteButton, editButton, approveButton);
        actions.setSpacing(false);
        return actions;
    }

    private Icon getFileIcon(String mimeType) {
        if (mimeType == null) {
            return VaadinIcon.FILE_O.create();
        }

        Icon icon;
        if (mimeType.startsWith("image/")) {
            icon = VaadinIcon.FILE_PICTURE.create();
        } else if (mimeType.contains("pdf")) {
            icon = VaadinIcon.FILE_TEXT_O.create();
        } else if (mimeType.contains("word") || mimeType.contains("document")) {
            icon = VaadinIcon.FILE_TEXT.create();
        } else if (mimeType.contains("excel") || mimeType.contains("spreadsheet")) {
            icon = VaadinIcon.FILE_TABLE.create();
        } else if (mimeType.contains("text")) {
            icon = VaadinIcon.FILE_CODE.create();
        } else {
            icon = VaadinIcon.FILE_O.create();
        }
        
        icon.setSize("20px");
        icon.getStyle().set("color", "var(--lumo-primary-color)");
        return icon;
    }

    private String getCategoryColor(String category) {
        if (category == null) return "#9E9E9E";
        
        switch (category) {
            case "Documenti": return "#2196F3";
            case "Immagini": return "#4CAF50";
            case "Fatture": return "#FF9800";
            case "Contratti": return "#9C27B0";
            case "Report": return "#00BCD4";
            case "Presentazioni": return "#E91E63";
            default: return "#607D8B";
        }
    }

    private String getStatusColor(String status) {
        if (status == null) return "#9E9E9E";
        
        switch (status) {
            case "PENDING": return "#FF9800";
            case "APPROVED": return "#4CAF50";
            case "REJECTED": return "#F44336";
            case "ARCHIVED": return "#757575";
            default: return "#9E9E9E";
        }
    }

    private void openEditDialog(FileUpload file) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Modifica File");
        dialog.setWidth("500px");

        FormLayout formLayout = new FormLayout();

        TextField fileNameField = new TextField("Nome File");
        fileNameField.setValue(file.getFileName());
        fileNameField.setWidthFull();

        ComboBox<String> categoryCombo = new ComboBox<>("Tipologia");
        categoryCombo.setItems(CATEGORIES);
        categoryCombo.setValue(file.getCategory());
        categoryCombo.setAllowCustomValue(true);
        categoryCombo.addCustomValueSetListener(e -> categoryCombo.setValue(e.getDetail()));
        categoryCombo.setWidthFull();

        ComboBox<String> statusCombo = new ComboBox<>("Status");
        statusCombo.setItems(STATUSES);
        statusCombo.setValue(file.getStatus());
        statusCombo.setWidthFull();

        TextField ownerField = new TextField("Proprietario");
        ownerField.setValue(file.getUploadedBy());
        ownerField.setWidthFull();

        TextArea descriptionArea = new TextArea("Descrizione");
        descriptionArea.setValue(file.getDescription() != null ? file.getDescription() : "");
        descriptionArea.setWidthFull();

        formLayout.add(fileNameField, categoryCombo, statusCombo, ownerField);
        formLayout.add(descriptionArea, 2);

        Button saveButton = new Button("Salva", e -> {
            file.setFileName(fileNameField.getValue());
            file.setCategory(categoryCombo.getValue());
            file.setStatus(statusCombo.getValue());
            file.setUploadedBy(ownerField.getValue());
            file.setDescription(descriptionArea.getValue());
            
            fileUploadService.save(file);
            refreshGrid();
            dialog.close();
            
            Notification.show("File aggiornato con successo", 3000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annulla", e -> dialog.close());

        dialog.add(formLayout);
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void approveFile(FileUpload file) {
        file.setStatus("APPROVED");
        fileUploadService.save(file);
        refreshGrid();
        
        Notification.show("File approvato: " + file.getFileName(), 3000, Notification.Position.BOTTOM_START)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void confirmDelete(FileUpload file) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Conferma Eliminazione");
        dialog.setText("Sei sicuro di voler eliminare il file \"" + file.getFileName() + "\"? Questa operazione non può essere annullata.");
        
        dialog.setCancelable(true);
        dialog.setCancelText("Annulla");
        
        dialog.setConfirmText("Elimina");
        dialog.setConfirmButtonTheme("error primary");
        
        dialog.addConfirmListener(e -> {
            try {
                fileUploadService.delete(file.getId());
                refreshGrid();
                Notification.show("File eliminato con successo", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Errore durante l'eliminazione: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        
        dialog.open();
    }

    private void applyFilters() {
        List<FileUpload> files = fileUploadService.findAll();

        String searchText = searchField.getValue();
        if (searchText != null && !searchText.trim().isEmpty()) {
            files = files.stream()
                .filter(f -> f.getFileName().toLowerCase().contains(searchText.toLowerCase()))
                .collect(Collectors.toList());
        }

        String category = categoryFilter.getValue();
        if (category != null && !category.isEmpty()) {
            files = files.stream()
                .filter(f -> category.equals(f.getCategory()))
                .collect(Collectors.toList());
        }

        String status = statusFilter.getValue();
        if (status != null && !status.isEmpty()) {
            files = files.stream()
                .filter(f -> status.equals(f.getStatus()))
                .collect(Collectors.toList());
        }

        String owner = ownerFilter.getValue();
        if (owner != null && !owner.trim().isEmpty()) {
            files = files.stream()
                .filter(f -> f.getUploadedBy().toLowerCase().contains(owner.toLowerCase()))
                .collect(Collectors.toList());
        }

        grid.setItems(files);
    }

    private void clearFilters() {
        searchField.clear();
        categoryFilter.clear();
        statusFilter.clear();
        ownerFilter.clear();
        refreshGrid();
    }

    private void refreshGrid() {
        System.out.println("🔄 RefreshGrid chiamato");
        List<FileUpload> files = fileUploadService.findAll();
        System.out.println("📊 Trovati " + files.size() + " file nel database");
        
        // Force grid refresh
        grid.getDataProvider().refreshAll();
        grid.setItems(files);
        
        System.out.println("✅ Griglia aggiornata con " + files.size() + " elementi");
    }

    private void clearUploadForm() {
        uploadCategoryCombo.clear();
        uploadDescriptionArea.clear();
        uploadOwnerField.setValue("Utente Corrente");
        // Clear the upload component
        upload.clearFileList();
    }
}
