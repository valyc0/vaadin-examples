package io.bootify.my_app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import io.bootify.my_app.domain.FileUpload;
import io.bootify.my_app.service.FileUploadService;

import java.io.InputStream;
import java.util.function.Consumer;

public class FileUploadDialog extends Dialog {

    private final FileUploadService fileUploadService;
    private final Consumer<FileUpload> onSuccessCallback;

    private final TextField uploaderField;
    private final TextArea descriptionField;
    private final MemoryBuffer buffer;
    private final Upload upload;
    private final Span uploadStatus;
    private Button saveButton;

    private byte[] fileData;
    private String uploadedFileName;
    private String uploadedMimeType;
    private Long uploadedFileSize;

    public FileUploadDialog(FileUploadService fileUploadService, Consumer<FileUpload> onSuccessCallback) {
        this.fileUploadService = fileUploadService;
        this.onSuccessCallback = onSuccessCallback;

        setModal(true);
        setDraggable(false);
        setResizable(false);
        setWidth("600px");

        // Header
        H2 title = new H2("Carica Nuovo File");
        title.getStyle().set("margin", "0");

        // Upload component
        buffer = new MemoryBuffer();
        upload = new Upload(buffer);
        upload.setMaxFiles(1);
        upload.setAcceptedFileTypes("image/*", "application/pdf", ".doc", ".docx", ".xls", ".xlsx", ".txt", ".csv");
        upload.setMaxFileSize(10 * 1024 * 1024); // 10 MB

        uploadStatus = new Span("Nessun file selezionato");
        uploadStatus.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        // Upload listeners
        upload.addSucceededListener(event -> {
            try {
                InputStream inputStream = buffer.getInputStream();
                fileData = inputStream.readAllBytes();
                uploadedFileName = event.getFileName();
                uploadedMimeType = event.getMIMEType();
                uploadedFileSize = (long) fileData.length;

                uploadStatus.setText("✓ File caricato: " + uploadedFileName + 
                                   " (" + formatFileSize(uploadedFileSize) + ")");
                uploadStatus.getStyle().set("color", "var(--lumo-success-color)");
                saveButton.setEnabled(true);
            } catch (Exception e) {
                Notification.show("Errore durante il caricamento del file", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        upload.addFileRejectedListener(event -> {
            Notification.show(event.getErrorMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        // Form fields
        uploaderField = new TextField("Caricato da");
        uploaderField.setPlaceholder("Nome utente");
        uploaderField.setRequiredIndicatorVisible(true);
        uploaderField.setWidthFull();

        descriptionField = new TextArea("Descrizione");
        descriptionField.setPlaceholder("Aggiungi una descrizione opzionale...");
        descriptionField.setMaxLength(500);
        descriptionField.setWidthFull();

        FormLayout formLayout = new FormLayout();
        formLayout.add(uploaderField, descriptionField);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        // Buttons
        saveButton = new Button("Salva", event -> save());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setEnabled(false);

        Button cancelButton = new Button("Annulla", event -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();
        buttonLayout.getStyle().set("margin-top", "var(--lumo-space-m)");

        // Layout
        Div uploadSection = new Div(upload, uploadStatus);
        uploadSection.getStyle().set("margin-bottom", "var(--lumo-space-m)");

        VerticalLayout layout = new VerticalLayout(
                title,
                uploadSection,
                formLayout,
                buttonLayout
        );
        layout.setPadding(true);
        layout.setSpacing(true);

        add(layout);
    }

    private void save() {
        if (fileData == null || fileData.length == 0) {
            Notification.show("Seleziona un file da caricare", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (uploaderField.isEmpty()) {
            Notification.show("Inserisci il nome dell'utente", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            FileUpload fileUpload = new FileUpload();
            fileUpload.setFileName(uploadedFileName);
            fileUpload.setFileType(uploadedMimeType);
            fileUpload.setFileSize(uploadedFileSize);
            fileUpload.setUploadedBy(uploaderField.getValue());
            fileUpload.setDescription(descriptionField.getValue());
            fileUpload.setFileData(fileData);

            fileUploadService.save(fileUpload);

            Notification.show("File caricato con successo!", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            if (onSuccessCallback != null) {
                onSuccessCallback.accept(fileUpload);
            }

            close();
        } catch (Exception e) {
            Notification.show("Errore durante il salvataggio: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private String formatFileSize(Long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        }
    }

    @Override
    public void open() {
        super.open();
        // Reset form
        uploaderField.clear();
        descriptionField.clear();
        upload.clearFileList();
        uploadStatus.setText("Nessun file selezionato");
        uploadStatus.getStyle().set("color", "var(--lumo-secondary-text-color)");
        saveButton.setEnabled(false);
        fileData = null;
        uploadedFileName = null;
        uploadedMimeType = null;
        uploadedFileSize = null;
    }
}
