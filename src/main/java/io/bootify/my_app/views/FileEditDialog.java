package io.bootify.my_app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
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
import io.bootify.my_app.domain.FileUpload;
import io.bootify.my_app.service.FileUploadService;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class FileEditDialog extends Dialog {

    private final FileUploadService fileUploadService;
    private final FileUpload fileUpload;
    private final Consumer<FileUpload> onSuccessCallback;

    private final TextField fileNameField;
    private final TextField fileTypeField;
    private final TextArea descriptionField;
    private final TextArea metadataField;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public FileEditDialog(FileUpload fileUpload, FileUploadService fileUploadService, 
                          Consumer<FileUpload> onSuccessCallback) {
        this.fileUpload = fileUpload;
        this.fileUploadService = fileUploadService;
        this.onSuccessCallback = onSuccessCallback;

        setModal(true);
        setDraggable(false);
        setResizable(false);
        setWidth("700px");

        // Header
        H2 title = new H2("Modifica File");
        title.getStyle().set("margin", "0");

        // File info section
        VerticalLayout infoSection = createInfoSection();

        // Form fields
        fileNameField = new TextField("Nome File");
        fileNameField.setValue(fileUpload.getFileName());
        fileNameField.setWidthFull();
        fileNameField.setRequiredIndicatorVisible(true);

        fileTypeField = new TextField("Tipo File");
        fileTypeField.setValue(fileUpload.getFileType());
        fileTypeField.setWidthFull();

        descriptionField = new TextArea("Descrizione");
        descriptionField.setValue(fileUpload.getDescription() != null ? fileUpload.getDescription() : "");
        descriptionField.setPlaceholder("Aggiungi una descrizione...");
        descriptionField.setMaxLength(500);
        descriptionField.setWidthFull();
        descriptionField.setHeight("100px");

        // Metadata section
        H3 metadataTitle = new H3("Metadati Personalizzati");
        metadataTitle.getStyle()
                .set("margin-top", "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-xs)")
                .set("font-size", "var(--lumo-font-size-l)");

        Span metadataHint = new Span("Inserisci metadati in formato JSON o testo libero");
        metadataHint.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        metadataField = new TextArea();
        metadataField.setValue(fileUpload.getMetadata() != null ? fileUpload.getMetadata() : "");
        metadataField.setPlaceholder("{\n  \"author\": \"Nome Autore\",\n  \"project\": \"Nome Progetto\",\n  \"tags\": [\"tag1\", \"tag2\"]\n}");
        metadataField.setWidthFull();
        metadataField.setHeight("150px");
        metadataField.getStyle().set("font-family", "monospace");

        FormLayout formLayout = new FormLayout();
        formLayout.add(fileNameField, fileTypeField, descriptionField);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        // Buttons
        Button saveButton = new Button("Salva Modifiche", event -> save());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setIcon(new Icon(VaadinIcon.CHECK));

        Button cancelButton = new Button("Annulla", event -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();
        buttonLayout.getStyle().set("margin-top", "var(--lumo-space-m)");

        // Main layout
        VerticalLayout layout = new VerticalLayout(
                title,
                infoSection,
                formLayout,
                metadataTitle,
                metadataHint,
                metadataField,
                buttonLayout
        );
        layout.setPadding(true);
        layout.setSpacing(true);

        add(layout);
    }

    private VerticalLayout createInfoSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(false);
        section.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin-bottom", "var(--lumo-space-m)");

        Span uploadedByLabel = new Span("Caricato da: ");
        uploadedByLabel.getStyle().set("font-weight", "500");
        Span uploadedByValue = new Span(fileUpload.getUploadedBy());
        HorizontalLayout uploadedByLayout = new HorizontalLayout(uploadedByLabel, uploadedByValue);
        uploadedByLayout.setSpacing(false);

        Span uploadDateLabel = new Span("Data caricamento: ");
        uploadDateLabel.getStyle().set("font-weight", "500");
        Span uploadDateValue = new Span(fileUpload.getUploadDate().format(DATE_FORMATTER));
        HorizontalLayout uploadDateLayout = new HorizontalLayout(uploadDateLabel, uploadDateValue);
        uploadDateLayout.setSpacing(false);

        Span fileSizeLabel = new Span("Dimensione: ");
        fileSizeLabel.getStyle().set("font-weight", "500");
        Span fileSizeValue = new Span(fileUpload.getFormattedFileSize());
        HorizontalLayout fileSizeLayout = new HorizontalLayout(fileSizeLabel, fileSizeValue);
        fileSizeLayout.setSpacing(false);

        section.add(uploadedByLayout, uploadDateLayout, fileSizeLayout);
        return section;
    }

    private void save() {
        if (fileNameField.isEmpty()) {
            Notification.show("Il nome del file è obbligatorio", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            fileUpload.setFileName(fileNameField.getValue());
            fileUpload.setFileType(fileTypeField.getValue());
            fileUpload.setDescription(descriptionField.getValue());
            fileUpload.setMetadata(metadataField.getValue().trim().isEmpty() ? null : metadataField.getValue());

            fileUploadService.save(fileUpload);

            Notification.show("File modificato con successo!", 3000, Notification.Position.MIDDLE)
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
}
