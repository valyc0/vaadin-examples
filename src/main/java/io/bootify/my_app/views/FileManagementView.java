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
import io.bootify.my_app.domain.FileUpload;
import io.bootify.my_app.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "files", layout = MainLayout.class)
@PageTitle("Gestione File")
public class FileManagementView extends VerticalLayout {

    private final FileUploadService fileUploadService;
    private final Grid<FileUpload> grid;
    private final TextField searchField;
    private final Span statsLabel;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    public FileManagementView(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Header
        H1 title = new H1("Gestione File");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        // Stats
        statsLabel = new Span();
        updateStats();
        statsLabel.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        // Search field
        searchField = new TextField();
        searchField.setPlaceholder("Cerca file per nome...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidth("300px");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> filterFiles(e.getValue()));
        searchField.setClearButtonVisible(true);

        // Upload button
        Button uploadButton = new Button("Carica File", new Icon(VaadinIcon.UPLOAD));
        uploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        uploadButton.addClickListener(e -> openUploadDialog());

        // Refresh button
        Button refreshButton = new Button("Aggiorna", new Icon(VaadinIcon.REFRESH));
        refreshButton.addClickListener(e -> refreshGrid());

        // Toolbar
        HorizontalLayout toolbar = new HorizontalLayout(searchField, uploadButton, refreshButton);
        toolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        toolbar.getStyle().set("flex-wrap", "wrap");

        // Grid
        grid = new Grid<>(FileUpload.class, false);
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

        // File name column with icon
        grid.addColumn(new ComponentRenderer<>(file -> {
            Icon icon = getFileIcon(file.getFileType());
            icon.getStyle().set("margin-right", "var(--lumo-space-s)");
            
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
        .setSortable(true);

        // File type column
        grid.addColumn(FileUpload::getFileType)
                .setHeader("Tipo")
                .setAutoWidth(true)
                .setSortable(true);

        // File size column
        grid.addColumn(FileUpload::getFormattedFileSize)
                .setHeader("Dimensione")
                .setAutoWidth(true)
                .setSortable(true);

        // Uploaded by column
        grid.addColumn(file -> file.getUploadedBy())
                .setHeader("Caricato da")
                .setAutoWidth(true)
                .setSortable(true);

        // Upload date column
        grid.addColumn(file -> file.getUploadDate().format(DATE_FORMATTER))
                .setHeader("Data")
                .setAutoWidth(true)
                .setSortable(true);

        // Description column
        grid.addColumn(new ComponentRenderer<>(file -> {
            String description = file.getDescription();
            if (description != null && !description.isEmpty()) {
                Span span = new Span(description);
                span.getStyle()
                        .set("overflow", "hidden")
                        .set("text-overflow", "ellipsis")
                        .set("white-space", "nowrap")
                        .set("max-width", "200px")
                        .set("display", "block");
                span.setTitle(description);
                return span;
            }
            Span empty = new Span("—");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)");
            return empty;
        }))
        .setHeader("Descrizione")
        .setAutoWidth(true)
        .setFlexGrow(1);

        // Metadata indicator
        grid.addColumn(new ComponentRenderer<>(file -> {
            if (file.getMetadata() != null && !file.getMetadata().isEmpty()) {
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

    private Component createActionsLayout(FileUpload file) {
        Button downloadButton = new Button(new Icon(VaadinIcon.DOWNLOAD));
        downloadButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        downloadButton.setTooltipText("Scarica");
        downloadButton.addClickListener(e -> downloadFile(file));

        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editButton.setTooltipText("Modifica");
        editButton.addClickListener(e -> editFile(file));

        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteButton.setTooltipText("Elimina");
        deleteButton.addClickListener(e -> confirmDelete(file));

        HorizontalLayout actions = new HorizontalLayout(downloadButton, editButton, deleteButton);
        actions.setSpacing(false);
        return actions;
    }

    private Icon getFileIcon(String mimeType) {
        if (mimeType == null) {
            return VaadinIcon.FILE_O.create();
        }

        if (mimeType.startsWith("image/")) {
            return VaadinIcon.FILE_PICTURE.create();
        } else if (mimeType.contains("pdf")) {
            return VaadinIcon.FILE_TEXT_O.create();
        } else if (mimeType.contains("word") || mimeType.contains("document")) {
            return VaadinIcon.FILE_TEXT.create();
        } else if (mimeType.contains("excel") || mimeType.contains("spreadsheet")) {
            return VaadinIcon.FILE_TABLE.create();
        } else if (mimeType.contains("text")) {
            return VaadinIcon.FILE_CODE.create();
        }

        return VaadinIcon.FILE_O.create();
    }

    private void openUploadDialog() {
        FileUploadDialog dialog = new FileUploadDialog(fileUploadService, file -> {
            refreshGrid();
            Notification.show("File caricato: " + file.getFileName(), 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        dialog.open();
    }

    private void editFile(FileUpload file) {
        FileEditDialog dialog = new FileEditDialog(file, fileUploadService, updatedFile -> {
            refreshGrid();
        });
        dialog.open();
    }

    private void downloadFile(FileUpload file) {
        // Use REST endpoint for download - more reliable than StreamResource
        String downloadUrl = "/api/files/download/" + file.getId();
        
        getUI().ifPresent(ui -> {
            ui.getPage().executeJs(
                "const link = document.createElement('a');" +
                "link.href = $0;" +
                "link.download = $1;" +
                "document.body.appendChild(link);" +
                "link.click();" +
                "document.body.removeChild(link);",
                downloadUrl,
                file.getFileName()
            );
        });

        Notification.show("Download avviato: " + file.getFileName(), 2000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void confirmDelete(FileUpload file) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Conferma eliminazione");
        dialog.setText("Sei sicuro di voler eliminare il file \"" + file.getFileName() + "\"? Questa operazione non può essere annullata.");
        
        dialog.setCancelable(true);
        dialog.setCancelText("Annulla");
        
        dialog.setConfirmText("Elimina");
        dialog.setConfirmButtonTheme("error primary");
        
        dialog.addConfirmListener(e -> deleteFile(file));
        
        dialog.open();
    }

    private void deleteFile(FileUpload file) {
        try {
            fileUploadService.delete(file.getId());
            refreshGrid();
            Notification.show("File eliminato: " + file.getFileName(), 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Errore durante l'eliminazione: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void filterFiles(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            refreshGrid();
        } else {
            List<FileUpload> files = fileUploadService.searchByFileName(searchTerm);
            grid.setItems(files);
        }
    }

    private void refreshGrid() {
        List<FileUpload> files = fileUploadService.findAll();
        grid.setItems(files);
        updateStats();
        searchField.clear();
    }

    private void updateStats() {
        long count = fileUploadService.count();
        statsLabel.setText(count + " file" + (count != 1 ? " totali" : " totale"));
    }
}
