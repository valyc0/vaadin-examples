package io.bootify.my_app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.bootify.my_app.domain.FileUpload;
import io.bootify.my_app.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Vista avanzata per la visualizzazione dettagliata dei file
 * con componenti Details per un'esperienza utente elegante e organizzata
 */
@Route(value = "file-details", layout = MainLayout.class)
@PageTitle("Dettagli File - Vista Avanzata")
public class FileDetailView extends VerticalLayout {

    private final FileUploadService fileUploadService;
    private final Grid<FileUpload> grid;
    private final VerticalLayout detailsPanel;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    @Autowired
    public FileDetailView(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
        
        setWidthFull();
        setPadding(true);
        setSpacing(true);
        
        // Header
        H1 title = new H1("📋 Gestione File - Vista Dettagliata");
        title.getStyle()
            .set("color", "var(--lumo-primary-color)")
            .set("margin-top", "0");
        
        Paragraph description = new Paragraph(
            "Seleziona un file dalla lista per visualizzare tutti i dettagli, " +
            "inclusi trascrizioni e traduzioni disponibili per il download."
        );
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        // Grid compatta per la selezione
        grid = createFileGrid();
        grid.setHeight("300px");
        
        // Pannello dettagli (inizialmente vuoto)
        detailsPanel = new VerticalLayout();
        detailsPanel.setPadding(true);
        detailsPanel.setSpacing(true);
        detailsPanel.addClassNames(
            LumoUtility.Border.ALL,
            LumoUtility.BorderRadius.MEDIUM,
            LumoUtility.Background.CONTRAST_5
        );
        detailsPanel.setVisible(false);
        
        // Refresh button
        Button refreshBtn = new Button("🔄 Aggiorna Lista", new Icon(VaadinIcon.REFRESH));
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshBtn.addClickListener(e -> refreshGrid());
        
        add(title, description, refreshBtn, grid, detailsPanel);
        
        // Load data
        refreshGrid();
    }
    
    private Grid<FileUpload> createFileGrid() {
        Grid<FileUpload> fileGrid = new Grid<>(FileUpload.class, false);
        fileGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        fileGrid.setWidthFull();
        
        // File name with icon
        fileGrid.addColumn(new ComponentRenderer<>(file -> {
            Icon icon = getFileIcon(file.getFileType());
            icon.getStyle().set("margin-right", "8px");
            
            Span fileName = new Span(file.getFileName());
            fileName.getStyle().set("font-weight", "500");
            
            HorizontalLayout layout = new HorizontalLayout(icon, fileName);
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            layout.setSpacing(false);
            return layout;
        }))
        .setHeader("Nome File")
        .setAutoWidth(true)
        .setFlexGrow(2);
        
        fileGrid.addColumn(FileUpload::getFormattedFileSize)
            .setHeader("Dimensione")
            .setAutoWidth(true);
        
        fileGrid.addColumn(file -> file.getUploadDate().format(DATE_FORMATTER))
            .setHeader("Data Upload")
            .setAutoWidth(true);
        
        fileGrid.addColumn(FileUpload::getUploadedBy)
            .setHeader("Caricato da")
            .setAutoWidth(true);
        
        // Selection listener
        fileGrid.addSelectionListener(event -> {
            event.getFirstSelectedItem().ifPresent(this::showFileDetails);
        });
        
        return fileGrid;
    }
    
    private void showFileDetails(FileUpload file) {
        detailsPanel.removeAll();
        detailsPanel.setVisible(true);
        
        H2 detailsTitle = new H2("📄 Dettagli: " + file.getFileName());
        detailsTitle.getStyle().set("margin-top", "0");
        
        detailsPanel.add(detailsTitle);
        
        // Informazioni Base (sempre espanse)
        Details basicInfo = createBasicInfoDetails(file);
        basicInfo.setOpened(true);
        detailsPanel.add(basicInfo);
        
        // Metadati Tecnici
        Details technicalInfo = createTechnicalInfoDetails(file);
        detailsPanel.add(technicalInfo);
        
        // Trascrizione (se presente)
        if (file.getTrascrizione() != null && !file.getTrascrizione().isEmpty()) {
            Details transcriptionDetails = createTranscriptionDetails(file);
            detailsPanel.add(transcriptionDetails);
        } else {
            detailsPanel.add(createEmptyFieldNotice("📝 Trascrizione", "Nessuna trascrizione disponibile per questo file"));
        }
        
        // Traduzione (se presente)
        if (file.getTraduzione() != null && !file.getTraduzione().isEmpty()) {
            Details translationDetails = createTranslationDetails(file);
            detailsPanel.add(translationDetails);
        } else {
            detailsPanel.add(createEmptyFieldNotice("🌐 Traduzione", "Nessuna traduzione disponibile per questo file"));
        }
        
        // Azioni
        detailsPanel.add(createActionsSection(file));
    }
    
    private Details createBasicInfoDetails(FileUpload file) {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        
        content.add(createInfoRow("📄 Nome File:", file.getFileName()));
        content.add(createInfoRow("📦 Dimensione:", file.getFormattedFileSize()));
        content.add(createInfoRow("🗂️ Tipo:", file.getFileType()));
        content.add(createInfoRow("👤 Caricato da:", file.getUploadedBy()));
        content.add(createInfoRow("📅 Data Upload:", file.getUploadDate().format(DATE_FORMATTER)));
        
        if (file.getCategory() != null) {
            content.add(createInfoRow("🏷️ Categoria:", file.getCategory()));
        }
        
        if (file.getStatus() != null) {
            Span statusBadge = new Span(file.getStatus());
            statusBadge.getElement().getThemeList().add("badge");
            statusBadge.getStyle()
                .set("background-color", getStatusColor(file.getStatus()))
                .set("color", "white")
                .set("padding", "4px 12px")
                .set("border-radius", "12px")
                .set("font-size", "0.875rem")
                .set("font-weight", "500");
            
            HorizontalLayout statusRow = createInfoRow("✅ Status:", "");
            statusRow.add(statusBadge);
            content.add(statusRow);
        }
        
        if (file.getDescription() != null && !file.getDescription().isEmpty()) {
            content.add(createInfoRow("📝 Descrizione:", file.getDescription()));
        }
        
        Details details = new Details("ℹ️ Informazioni Base", content);
        details.addThemeVariants(DetailsVariant.FILLED);
        return details;
    }
    
    private Details createTechnicalInfoDetails(FileUpload file) {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        
        content.add(createInfoRow("🆔 ID Database:", String.valueOf(file.getId())));
        
        if (file.getUniqueFileName() != null) {
            content.add(createInfoRow("🔑 Nome Unico:", file.getUniqueFileName()));
        } else {
            content.add(createInfoRow("🔑 Nome Unico:", "Non assegnato"));
        }
        
        if (file.getEtag() != null) {
            content.add(createInfoRow("🏷️ E-Tag:", file.getEtag()));
        } else {
            content.add(createInfoRow("🏷️ E-Tag:", "Non disponibile"));
        }
        
        if (file.getMetadata() != null && !file.getMetadata().isEmpty()) {
            content.add(createInfoRow("📊 Metadata:", file.getMetadata()));
        }
        
        content.add(createInfoRow("💾 Dimensione Blob:", file.getFileSize() + " bytes"));
        
        Details details = new Details("⚙️ Metadati Tecnici", content);
        details.addThemeVariants(DetailsVariant.FILLED);
        return details;
    }
    
    private Details createTranscriptionDetails(FileUpload file) {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        
        // Preview del testo (primi 500 caratteri)
        String transcription = file.getTrascrizione();
        String preview = transcription.length() > 500 
            ? transcription.substring(0, 500) + "..." 
            : transcription;
        
        Div previewDiv = new Div();
        previewDiv.setText(preview);
        previewDiv.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "var(--lumo-space-m)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("font-family", "monospace")
            .set("font-size", "0.875rem")
            .set("white-space", "pre-wrap")
            .set("overflow-wrap", "break-word")
            .set("max-height", "300px")
            .set("overflow-y", "auto");
        
        content.add(new Span("Anteprima (primi 500 caratteri):"));
        content.add(previewDiv);
        
        // Info sulla lunghezza
        Span lengthInfo = new Span(String.format("📏 Lunghezza totale: %,d caratteri", transcription.length()));
        lengthInfo.getStyle()
            .set("font-size", "0.875rem")
            .set("color", "var(--lumo-secondary-text-color)");
        content.add(lengthInfo);
        
        // Download button
        Button downloadBtn = createDownloadButton(
            "Scarica Trascrizione Completa",
            file.getFileName() + "_trascrizione.txt",
            transcription
        );
        content.add(downloadBtn);
        
        Details details = new Details("📝 Trascrizione", content);
        details.addThemeVariants(DetailsVariant.FILLED);
        return details;
    }
    
    private Details createTranslationDetails(FileUpload file) {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        
        // Preview del testo (primi 500 caratteri)
        String translation = file.getTraduzione();
        String preview = translation.length() > 500 
            ? translation.substring(0, 500) + "..." 
            : translation;
        
        Div previewDiv = new Div();
        previewDiv.setText(preview);
        previewDiv.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "var(--lumo-space-m)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("font-family", "monospace")
            .set("font-size", "0.875rem")
            .set("white-space", "pre-wrap")
            .set("overflow-wrap", "break-word")
            .set("max-height", "300px")
            .set("overflow-y", "auto");
        
        content.add(new Span("Anteprima (primi 500 caratteri):"));
        content.add(previewDiv);
        
        // Info sulla lunghezza
        Span lengthInfo = new Span(String.format("📏 Lunghezza totale: %,d caratteri", translation.length()));
        lengthInfo.getStyle()
            .set("font-size", "0.875rem")
            .set("color", "var(--lumo-secondary-text-color)");
        content.add(lengthInfo);
        
        // Download button
        Button downloadBtn = createDownloadButton(
            "Scarica Traduzione Completa",
            file.getFileName() + "_traduzione.txt",
            translation
        );
        content.add(downloadBtn);
        
        Details details = new Details("🌐 Traduzione", content);
        details.addThemeVariants(DetailsVariant.FILLED);
        return details;
    }
    
    private Div createEmptyFieldNotice(String title, String message) {
        Div notice = new Div();
        notice.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "var(--lumo-space-m)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("border-left", "4px solid var(--lumo-contrast-30pct)");
        
        H4 noticeTitle = new H4(title);
        noticeTitle.getStyle().set("margin", "0 0 8px 0");
        
        Span noticeText = new Span(message);
        noticeText.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-size", "0.875rem");
        
        notice.add(noticeTitle, noticeText);
        return notice;
    }
    
    private Button createDownloadButton(String text, String filename, String content) {
        Button downloadBtn = new Button(text, new Icon(VaadinIcon.DOWNLOAD));
        downloadBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        StreamResource resource = new StreamResource(filename,
            () -> new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        
        downloadBtn.getElement().setAttribute("download", true);
        downloadBtn.addClickListener(e -> {
            downloadBtn.getElement().setAttribute("href", resource);
            Notification.show("📥 Download avviato: " + filename, 3000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        
        // Set the download link
        Anchor downloadLink = new Anchor(resource, "");
        downloadLink.getElement().setAttribute("download", true);
        downloadLink.add(downloadBtn);
        
        return downloadBtn;
    }
    
    private HorizontalLayout createActionsSection(FileUpload file) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setPadding(true);
        actions.setSpacing(true);
        actions.getStyle()
            .set("margin-top", "var(--lumo-space-m)")
            .set("padding-top", "var(--lumo-space-m)")
            .set("border-top", "1px solid var(--lumo-contrast-10pct)");
        
        Button editBtn = new Button("✏️ Modifica", new Icon(VaadinIcon.EDIT));
        editBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        editBtn.addClickListener(e -> {
            Notification.show("Funzionalità di modifica da implementare", 3000, Notification.Position.MIDDLE);
        });
        
        Button deleteBtn = new Button("🗑️ Elimina", new Icon(VaadinIcon.TRASH));
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteBtn.addClickListener(e -> {
            Notification.show("Funzionalità di eliminazione da implementare", 3000, Notification.Position.MIDDLE);
        });
        
        actions.add(editBtn, deleteBtn);
        return actions;
    }
    
    private HorizontalLayout createInfoRow(String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setSpacing(true);
        row.setAlignItems(FlexComponent.Alignment.BASELINE);
        
        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("font-weight", "600")
            .set("min-width", "150px")
            .set("color", "var(--lumo-secondary-text-color)");
        
        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("flex", "1");
        
        row.add(labelSpan, valueSpan);
        return row;
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
    
    private void refreshGrid() {
        List<FileUpload> files = fileUploadService.findAll();
        grid.setItems(files);
        
        Notification.show("✅ Lista aggiornata: " + files.size() + " file trovati", 
            2000, Notification.Position.BOTTOM_START);
    }
}
