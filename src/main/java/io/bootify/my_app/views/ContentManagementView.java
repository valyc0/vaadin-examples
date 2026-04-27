package io.bootify.my_app.views;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
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
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.bootify.my_app.component.GenericPaginatedGrid;
import io.bootify.my_app.domain.Content;
import io.bootify.my_app.service.ContentService;
import io.bootify.my_app.service.PreviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Route(value = "content-management", layout = MainLayout.class)
@PageTitle("Content Management")
public class ContentManagementView extends VerticalLayout {

    private final ContentService contentService;
    private final PreviewService previewService;
    private final GenericPaginatedGrid<Content> contentGrid = new GenericPaginatedGrid<>();
    private final ComboBox<String> fileTypeFilter = new ComboBox<>("Tipo File");
    private final TextField searchField = new TextField();
    private final Span totalLabel = new Span();
    private long cachedTotal = 0;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ContentManagementView(@Autowired ContentService contentService,
                                  @Autowired PreviewService previewService) {
        this.contentService = contentService;
        this.previewService = previewService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        // Titolo
        H2 title = new H2("Gestione Contenuti");

        // Totale elementi
        totalLabel.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("margin-bottom", "var(--lumo-space-m)");
        updateTotalLabel();

        // Toolbar con pulsanti CRUD
        HorizontalLayout toolbar = createToolbar();

        // Filtri e ricerca
        searchField.setPlaceholder("Cerca per nome file, descrizione o categoria...");
        searchField.setWidth("400px");
        searchField.setClearButtonVisible(true);

        fileTypeFilter.setItems("Tutti", "PDF", "IMAGE", "DOCUMENT", "VIDEO", "AUDIO", "ARCHIVE", "OTHER");
        fileTypeFilter.setValue("Tutti");
        fileTypeFilter.setWidth("200px");

        HorizontalLayout filterLayout = new HorizontalLayout(searchField, fileTypeFilter);
        filterLayout.setAlignItems(Alignment.END);

        // Configurazione colonne del grid
        configureGrid();

        // Configura DataProvider con paginazione e filtri
        contentGrid.setDataProvider((pageRequest, sortOrders, filterText) -> {
            String searchTerm = searchField.getValue() != null ? searchField.getValue() : "";
            String fileType = fileTypeFilter.getValue() != null && !"Tutti".equals(fileTypeFilter.getValue())
                    ? fileTypeFilter.getValue() : "";

            PageRequest pageable = PageRequest.of(pageRequest.getPageNumber(),
                    pageRequest.getPageSize(),
                    pageRequest.getSort());

            org.springframework.data.domain.Page<Content> page;
            if (!searchTerm.isEmpty() && !fileType.isEmpty()) {
                page = contentService.searchByFileType(fileType, searchTerm, pageable);
            } else if (!searchTerm.isEmpty()) {
                page = contentService.search(searchTerm, pageable);
            } else if (!fileType.isEmpty()) {
                page = contentService.findByFileType(fileType, pageable);
            } else {
                page = contentService.findAll(pageable);
            }
            
            // Cache il totale per evitare query duplicate
            cachedTotal = page.getTotalElements();
            updateTotalLabel();
            
            return page;
        });
        
        // Imposta ordinamento di default lato server per ID ascendente
        contentGrid.setDefaultSort("id", org.springframework.data.domain.Sort.Direction.DESC);

        // Ricarica grid quando cambiano filtri
        searchField.addValueChangeListener(e -> contentGrid.refresh());
        fileTypeFilter.addValueChangeListener(e -> contentGrid.refresh());

        // Layout finale
        add(title, totalLabel, toolbar, filterLayout, contentGrid);
        expand(contentGrid);
    }
    
    private HorizontalLayout createToolbar() {
        Button addButton = new Button("Aggiungi Contenuto", new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openAddDialog());
        
        Button refreshButton = new Button("Aggiorna", new Icon(VaadinIcon.REFRESH));
        refreshButton.addClickListener(e -> contentGrid.refresh());
        
        HorizontalLayout toolbar = new HorizontalLayout(addButton, refreshButton);
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        toolbar.setSpacing(true);
        
        return toolbar;
    }
    
    private void configureGrid() {
        contentGrid.getGrid().addColumn(Content::getId).setHeader("ID").setSortable(true).setKey("id").setWidth("80px").setFlexGrow(0);
        contentGrid.getGrid().addColumn(Content::getFileName).setHeader("Nome File").setSortable(true).setKey("fileName");
        contentGrid.getGrid().addColumn(Content::getFormattedFileSize).setHeader("Dimensione").setSortable(true).setKey("fileSize").setWidth("120px").setFlexGrow(0);
        contentGrid.getGrid().addColumn(Content::getFileType).setHeader("Tipo").setSortable(true).setKey("fileType").setWidth("120px").setFlexGrow(0);
        contentGrid.getGrid().addColumn(content -> 
                content.getCreationDate() != null 
                    ? content.getCreationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : ""
        ).setHeader("Data Creazione").setSortable(true).setKey("creationDate").setWidth("180px").setFlexGrow(0);
        
        // Colonna azioni
        contentGrid.getGrid().addColumn(new ComponentRenderer<>(this::createActionsLayout))
                .setHeader("Azioni")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // Detail inline: clic sulla riga apre il pannello sotto quella riga
        contentGrid.getGrid().setItemDetailsRenderer(new ComponentRenderer<>(this::buildRowDetail));
        contentGrid.getGrid().setDetailsVisibleOnClick(true);
    }
    
    private Component createActionsLayout(Content content) {
        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editButton.setTooltipText("Modifica");
        editButton.addClickListener(e -> openEditDialog(content));
        
        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteButton.setTooltipText("Elimina");
        deleteButton.addClickListener(e -> confirmDelete(content));
        
        HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
        actions.setSpacing(false);
        return actions;
    }
    
    private void openAddDialog() {
        Content newContent = new Content();
        openEditDialog(newContent);
    }
    
    private void openEditDialog(Content content) {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setMaxHeight("90vh");
        
        H3 title = new H3(content.getId() == null ? "Nuovo Contenuto" : "Modifica Contenuto");
        
        Binder<Content> binder = new Binder<>(Content.class);
        
        // Form principale
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        
        TextField fileNameField = new TextField("Nome File");
        fileNameField.setWidthFull();
        fileNameField.setRequiredIndicatorVisible(true);
        
        TextField fileSizeField = new TextField("Dimensione (bytes)");
        fileSizeField.setWidthFull();
        fileSizeField.setRequiredIndicatorVisible(true);
        
        ComboBox<String> fileTypeField = new ComboBox<>("Tipo File");
        fileTypeField.setItems("PDF", "IMAGE", "DOCUMENT", "VIDEO", "AUDIO", "ARCHIVE", "OTHER");
        fileTypeField.setAllowCustomValue(true);
        fileTypeField.addCustomValueSetListener(e -> fileTypeField.setValue(e.getDetail()));
        fileTypeField.setWidthFull();
        fileTypeField.setRequiredIndicatorVisible(true);
        
        ComboBox<String> categoryField = new ComboBox<>("Categoria");
        categoryField.setItems("Documenti", "Immagini", "Video", "Audio", "Archivi", "Altro");
        categoryField.setAllowCustomValue(true);
        categoryField.addCustomValueSetListener(e -> categoryField.setValue(e.getDetail()));
        categoryField.setWidthFull();
        
        TextArea descriptionField = new TextArea("Descrizione");
        descriptionField.setWidthFull();
        descriptionField.setMinHeight("100px");
        
        TextField tagsField = new TextField("Tags (separati da virgola)");
        tagsField.setWidthFull();
        
        // Binding campi modificabili
        binder.forField(fileNameField)
                .asRequired("Nome file è obbligatorio")
                .bind(Content::getFileName, Content::setFileName);
        
        binder.forField(fileSizeField)
                .asRequired("Dimensione è obbligatoria")
                .withConverter(
                        value -> value != null && !value.isEmpty() ? Long.parseLong(value) : null,
                        value -> value != null ? value.toString() : ""
                )
                .bind(Content::getFileSize, Content::setFileSize);
        
        binder.forField(fileTypeField)
                .asRequired("Tipo file è obbligatorio")
                .bind(Content::getFileType, Content::setFileType);
        
        binder.forField(categoryField)
                .bind(Content::getCategory, Content::setCategory);
        
        binder.forField(descriptionField)
                .bind(Content::getDescription, Content::setDescription);
        
        binder.forField(tagsField)
                .bind(Content::getTags, Content::setTags);
        
        formLayout.add(fileNameField, fileSizeField, fileTypeField, categoryField);
        formLayout.add(descriptionField, 2);
        formLayout.add(tagsField, 2);
        
        // Sezione metadati fissi (read-only)
        VerticalLayout fixedMetadataSection = createFixedMetadataSection(content);
        
        // Sezione metadati custom (modificabili)
        VerticalLayout customMetadataSection = createCustomMetadataSection(content);
        
        // Pulsanti
        Button saveButton = new Button("Salva", e -> {
            if (binder.validate().isOk()) {
                try {
                    binder.writeBean(content);
                    contentService.save(content);
                    contentGrid.refresh();
                    dialog.close();
                    Notification.show("Contenuto salvato con successo", 3000, Notification.Position.BOTTOM_START)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } catch (Exception ex) {
                    Notification.show("Errore durante il salvataggio: " + ex.getMessage(), 
                            3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Button cancelButton = new Button("Annulla", e -> dialog.close());
        
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();
        
        // Layout dialog
        VerticalLayout dialogLayout = new VerticalLayout(
                title, 
                formLayout, 
                fixedMetadataSection,
                customMetadataSection,
                buttons
        );
        dialogLayout.setPadding(true);
        dialogLayout.setSpacing(true);
        
        dialog.add(dialogLayout);
        
        // Carica dati nel binder
        binder.readBean(content);
        
        dialog.open();
    }
    
    private VerticalLayout createFixedMetadataSection(Content content) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        
        H3 sectionTitle = new H3("Metadati Fissi (Read-Only)");
        sectionTitle.getStyle().set("margin-top", "var(--lumo-space-m)");
        
        FormLayout metadataLayout = new FormLayout();
        metadataLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        
        TextField fileHashField = new TextField("Hash File");
        fileHashField.setValue(content.getFileHash() != null ? content.getFileHash() : "N/A");
        fileHashField.setReadOnly(true);
        fileHashField.setWidthFull();
        
        TextField originalPathField = new TextField("Percorso Originale");
        originalPathField.setValue(content.getOriginalPath() != null ? content.getOriginalPath() : "N/A");
        originalPathField.setReadOnly(true);
        originalPathField.setWidthFull();
        
        TextField mimeTypeField = new TextField("MIME Type");
        mimeTypeField.setValue(content.getMimeType() != null ? content.getMimeType() : "N/A");
        mimeTypeField.setReadOnly(true);
        mimeTypeField.setWidthFull();
        
        TextField uploadUserField = new TextField("Caricato da");
        uploadUserField.setValue(content.getUploadUser() != null ? content.getUploadUser() : "N/A");
        uploadUserField.setReadOnly(true);
        uploadUserField.setWidthFull();
        
        TextField creationDateField = new TextField("Data Creazione");
        creationDateField.setValue(content.getCreationDate() != null 
                ? content.getCreationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) 
                : "N/A");
        creationDateField.setReadOnly(true);
        creationDateField.setWidthFull();
        
        TextField lastModifiedField = new TextField("Ultima Modifica");
        lastModifiedField.setValue(content.getLastModified() != null 
                ? content.getLastModified().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) 
                : "N/A");
        lastModifiedField.setReadOnly(true);
        lastModifiedField.setWidthFull();
        
        metadataLayout.add(fileHashField, originalPathField, mimeTypeField, uploadUserField, creationDateField, lastModifiedField);
        
        section.add(sectionTitle, metadataLayout);
        return section;
    }
    
    private VerticalLayout createCustomMetadataSection(Content content) {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        
        H3 sectionTitle = new H3("Metadati Custom (Chiave-Valore)");
        sectionTitle.getStyle().set("margin-top", "var(--lumo-space-m)");
        
        VerticalLayout metadataList = new VerticalLayout();
        metadataList.setPadding(false);
        metadataList.setSpacing(true);
        
        // Carica metadati esistenti
        Map<String, String> metadata = parseCustomMetadata(content.getCustomMetadata());
        
        // Crea campi per ogni metadato esistente
        metadata.forEach((key, value) -> {
            HorizontalLayout row = createMetadataRow(key, value, metadataList);
            metadataList.add(row);
        });
        
        // Pulsante per aggiungere nuovo metadato
        Button addMetadataButton = new Button("Aggiungi Metadato", new Icon(VaadinIcon.PLUS));
        addMetadataButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        addMetadataButton.addClickListener(e -> {
            HorizontalLayout row = createMetadataRow("", "", metadataList);
            metadataList.add(row);
        });
        
        // Pulsante per salvare metadati
        Button saveMetadataButton = new Button("Applica Metadati", new Icon(VaadinIcon.CHECK));
        saveMetadataButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
        saveMetadataButton.addClickListener(e -> {
            Map<String, String> newMetadata = new HashMap<>();
            metadataList.getChildren().forEach(component -> {
                if (component instanceof HorizontalLayout) {
                    HorizontalLayout row = (HorizontalLayout) component;
                    TextField keyField = (TextField) row.getComponentAt(0);
                    TextField valueField = (TextField) row.getComponentAt(1);
                    
                    String key = keyField.getValue();
                    String value = valueField.getValue();
                    
                    if (key != null && !key.trim().isEmpty()) {
                        newMetadata.put(key.trim(), value != null ? value : "");
                    }
                }
            });
            
            try {
                String json = objectMapper.writeValueAsString(newMetadata);
                content.setCustomMetadata(json);
                Notification.show("Metadati aggiornati", 2000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Errore nel salvataggio metadati: " + ex.getMessage(), 
                        3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        
        HorizontalLayout metadataButtons = new HorizontalLayout(addMetadataButton, saveMetadataButton);
        
        section.add(sectionTitle, metadataList, metadataButtons);
        return section;
    }
    
    private HorizontalLayout createMetadataRow(String key, String value, VerticalLayout parent) {
        TextField keyField = new TextField("Chiave");
        keyField.setValue(key != null ? key : "");
        keyField.setWidth("200px");
        
        TextField valueField = new TextField("Valore");
        valueField.setValue(value != null ? value : "");
        valueField.setWidth("300px");
        
        Button removeButton = new Button(new Icon(VaadinIcon.TRASH));
        removeButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        removeButton.setTooltipText("Rimuovi");
        removeButton.addClickListener(e -> {
            parent.remove(removeButton.getParent().get());
        });
        
        HorizontalLayout row = new HorizontalLayout(keyField, valueField, removeButton);
        row.setAlignItems(FlexComponent.Alignment.END);
        row.setSpacing(true);
        
        return row;
    }
    
    private Map<String, String> parseCustomMetadata(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
    
    private Component buildRowDetail(Content content) {
        // --- Stepper orizzontale (larghezza piena, in cima) ---
        Div stepperWrapper = buildContentStepper(content);
        stepperWrapper.getStyle().set("padding", "var(--lumo-space-m) var(--lumo-space-m) 0");

        // --- Sezione anteprima ---
        VerticalLayout previewSection = new VerticalLayout();
        previewSection.setPadding(false);
        previewSection.setSpacing(true);
        previewSection.setWidth("340px");
        previewSection.setMinWidth("220px");

        Span previewLabel = new Span("Anteprima");
        previewLabel.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)");
        Image img = new Image("/api/preview/" + content.getId(), "Anteprima");
        img.setMaxWidth("320px");
        img.setWidthFull();
        img.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "4px")
                .set("object-fit", "contain")
                .set("background", "#f5f5f5");
        previewSection.add(previewLabel, img);

        // --- Sezione campi ---
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("400px", 2)
        );

        form.addFormItem(label(content.getId() != null ? content.getId().toString() : "—"), "ID");
        form.addFormItem(label(content.getFileName()), "Nome File");
        form.addFormItem(label(content.getFormattedFileSize()), "Dimensione");
        form.addFormItem(label(content.getFileType()), "Tipo File");
        form.addFormItem(label(content.getMimeType()), "MIME Type");
        form.addFormItem(label(content.getCategory()), "Categoria");
        form.addFormItem(label(content.getTags()), "Tags");
        form.addFormItem(label(content.getUploadUser()), "Caricato da");
        form.addFormItem(label(content.getOriginalPath()), "Percorso Originale");
        form.addFormItem(label(content.getFileHash()), "Hash File");
        form.addFormItem(label(content.getCreationDate() != null
                ? content.getCreationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : null),
                "Data Creazione");
        form.addFormItem(label(content.getLastModified() != null
                ? content.getLastModified().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : null),
                "Ultima Modifica");

        if (content.getDescription() != null && !content.getDescription().isEmpty()) {
            Span desc = new Span(content.getDescription());
            desc.getStyle().set("white-space", "pre-wrap");
            form.setColspan(form.addFormItem(desc, "Descrizione"), 2);
        }

        parseCustomMetadata(content.getCustomMetadata())
                .forEach((key, value) -> form.addFormItem(label(value), key));

        // --- Layout orizzontale: anteprima | campi ---
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.START);
        row.getStyle().set("padding", "var(--lumo-space-m)");
        row.add(previewSection);
        row.setFlexGrow(1, form);
        row.add(form);

        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setPadding(false);
        wrapper.setSpacing(false);
        wrapper.setWidthFull();
        wrapper.add(stepperWrapper, row);
        return wrapper;
    }

    private Span label(String value) {
        return new Span(value != null && !value.isBlank() ? value : "—");
    }

    private Div buildContentStepper(Content content) {
        boolean[] done = {
            content.getId() != null,
            content.getFileType() != null && !content.getFileType().isBlank()
                && content.getCategory() != null && !content.getCategory().isBlank(),
            content.getDescription() != null && !content.getDescription().isBlank(),
            content.getTags() != null && !content.getTags().isBlank(),
            content.getCustomMetadata() != null && !content.getCustomMetadata().isBlank()
                && !content.getCustomMetadata().equals("{}")
        };
        String[] titles    = {"Caricato",   "Classificato",  "Descritto",    "Tags aggiunti", "Metadati custom"};
        String[] subtitles = {"Nel sistema","Tipo e categoria","Descrizione","Tag ricerca",   "Metadati custom"};

        int currentStep = done.length;
        for (int i = 0; i < done.length; i++) {
            if (!done[i]) { currentStep = i; break; }
        }

        // Riga superiore: cerchi + linee orizzontali
        Div topRow = new Div();
        topRow.getStyle()
                .set("display", "flex")
                .set("flex-direction", "row")
                .set("align-items", "center")
                .set("width", "100%");

        // Riga inferiore: etichette centrate sotto ogni cerchio
        Div bottomRow = new Div();
        bottomRow.getStyle()
                .set("display", "flex")
                .set("flex-direction", "row")
                .set("align-items", "flex-start")
                .set("width", "100%")
                .set("margin-top", "6px");

        for (int i = 0; i < titles.length; i++) {
            boolean isDone    = done[i];
            boolean isCurrent = (i == currentStep);
            boolean isLast    = (i == titles.length - 1);

            // Cerchio
            Div circle = new Div();
            circle.getStyle()
                    .set("width", "28px")
                    .set("height", "28px")
                    .set("border-radius", "50%")
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("justify-content", "center")
                    .set("flex-shrink", "0");

            if (isDone) {
                circle.getStyle()
                        .set("background-color", "var(--lumo-success-color)")
                        .set("color", "white");
                Icon checkIcon = VaadinIcon.CHECK.create();
                checkIcon.setSize("13px");
                circle.add(checkIcon);
            } else if (isCurrent) {
                circle.getStyle()
                        .set("background-color", "var(--lumo-primary-color)")
                        .set("color", "white");
                Span num = new Span(String.valueOf(i + 1));
                num.getStyle().set("font-size", "12px").set("font-weight", "700");
                circle.add(num);
            } else {
                circle.getStyle()
                        .set("background-color", "var(--lumo-contrast-10pct)")
                        .set("color", "var(--lumo-secondary-text-color)")
                        .set("border", "1px solid var(--lumo-contrast-20pct)");
                Span num = new Span(String.valueOf(i + 1));
                num.getStyle().set("font-size", "12px");
                circle.add(num);
            }

            // Cella step (cerchio centrato)
            Div stepCell = new Div();
            stepCell.getStyle()
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("align-items", "center")
                    .set("flex", isLast ? "0 0 auto" : "1 1 0%");

            if (!isLast) {
                // Cerchio + linea orizzontale a destra
                Div circleAndLine = new Div();
                circleAndLine.getStyle()
                        .set("display", "flex")
                        .set("flex-direction", "row")
                        .set("align-items", "center")
                        .set("width", "100%");
                Div line = new Div();
                line.getStyle()
                        .set("flex", "1")
                        .set("height", "2px")
                        .set("background-color", isDone
                                ? "var(--lumo-success-color)"
                                : "var(--lumo-contrast-20pct)");
                circleAndLine.add(circle, line);
                stepCell.add(circleAndLine);
            } else {
                stepCell.add(circle);
            }
            topRow.add(stepCell);

            // Etichetta centrata
            Div labelCell = new Div();
            labelCell.getStyle()
                    .set("flex", isLast ? "0 0 auto" : "1 1 0%")
                    .set("text-align", "center")
                    .set("padding-right", isLast ? "0" : "4px");

            Span titleSpan = new Span(titles[i]);
            titleSpan.getStyle()
                    .set("display", "block")
                    .set("font-size", "var(--lumo-font-size-xs)")
                    .set("font-weight", isDone || isCurrent ? "600" : "400")
                    .set("color", isDone
                            ? "var(--lumo-success-text-color)"
                            : isCurrent
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-secondary-text-color)");

            Span subtitleSpan = new Span(subtitles[i]);
            subtitleSpan.getStyle()
                    .set("display", "block")
                    .set("font-size", "10px")
                    .set("color", "var(--lumo-secondary-text-color)");

            labelCell.add(titleSpan, subtitleSpan);
            bottomRow.add(labelCell);
        }

        Div stepper = new Div(topRow, bottomRow);
        stepper.setWidthFull();
        return stepper;
    }

    private void updateTotalLabel() {
        String searchTerm = searchField.getValue() != null ? searchField.getValue() : "";
        String fileType = fileTypeFilter.getValue() != null && !"Tutti".equals(fileTypeFilter.getValue())
                ? fileTypeFilter.getValue() : "";
        
        String filterText = (!searchTerm.isEmpty() || !fileType.isEmpty()) ? " (filtrati)" : " totali";
        totalLabel.setText(cachedTotal + " contenut" + (cachedTotal != 1 ? "i" : "o") + filterText);
    }
    
    private void confirmDelete(Content content) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Conferma eliminazione");
        dialog.setText("Sei sicuro di voler eliminare \"" + content.getFileName() + "\"?");
        
        dialog.setCancelable(true);
        dialog.setCancelText("Annulla");
        
        dialog.setConfirmText("Elimina");
        dialog.setConfirmButtonTheme("error primary");
        
        dialog.addConfirmListener(e -> deleteContent(content));
        
        dialog.open();
    }
    
    private void deleteContent(Content content) {
        try {
            contentService.delete(content.getId());
            contentGrid.refresh();
            Notification.show("Contenuto eliminato con successo", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Errore durante l'eliminazione: " + e.getMessage(), 
                    3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
