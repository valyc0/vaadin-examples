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
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import io.bootify.my_app.domain.Product;
import io.bootify.my_app.service.ProductService;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class ProdottoEditDialog extends Dialog {

    private final ProductService productService;
    private final Product product;
    private final Consumer<Product> onSuccessCallback;

    private final TextField nameField;
    private final TextArea descriptionField;
    private final BigDecimalField priceField;
    private final TextField categoryField;
    private final IntegerField quantityField;
    private final TextArea metadataField;
    private final MemoryBuffer buffer;
    private final Upload upload;
    private final Span uploadStatus;

    private byte[] newFileData;
    private String newFileName;
    private String newFileType;
    private Long newFileSize;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ProdottoEditDialog(Product product, ProductService productService, 
                          Consumer<Product> onSuccessCallback) {
        this.product = product;
        this.productService = productService;
        this.onSuccessCallback = onSuccessCallback;

        setModal(true);
        setDraggable(false);
        setResizable(false);
        setWidth("800px");

        // Header
        H2 title = new H2("Modifica Prodotto");
        title.getStyle().set("margin", "0");

        // Info section
        VerticalLayout infoSection = createInfoSection();

        // Form fields
        nameField = new TextField("Nome Prodotto");
        nameField.setValue(product.getName());
        nameField.setWidthFull();
        nameField.setRequiredIndicatorVisible(true);

        descriptionField = new TextArea("Descrizione");
        descriptionField.setValue(product.getDescription() != null ? product.getDescription() : "");
        descriptionField.setPlaceholder("Descrizione del prodotto...");
        descriptionField.setMaxLength(1000);
        descriptionField.setWidthFull();
        descriptionField.setHeight("100px");

        priceField = new BigDecimalField("Prezzo");
        priceField.setPrefixComponent(new Span("€"));
        priceField.setValue(product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO);
        priceField.setRequiredIndicatorVisible(true);

        categoryField = new TextField("Categoria");
        categoryField.setValue(product.getCategory());
        categoryField.setRequiredIndicatorVisible(true);

        quantityField = new IntegerField("Quantità");
        quantityField.setValue(product.getQuantity() != null ? product.getQuantity() : 0);
        quantityField.setMin(0);
        quantityField.setRequiredIndicatorVisible(true);

        FormLayout formLayout = new FormLayout();
        formLayout.add(nameField, descriptionField);
        formLayout.add(priceField, categoryField, quantityField);
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setColspan(nameField, 2);
        formLayout.setColspan(descriptionField, 2);

        // File section
        H3 fileTitle = new H3("File Allegato");
        fileTitle.getStyle()
                .set("margin-top", "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-xs)")
                .set("font-size", "var(--lumo-font-size-l)");

        // Current file status
        uploadStatus = new Span();
        updateFileStatus();
        uploadStatus.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        // Upload component for replacing file
        buffer = new MemoryBuffer();
        upload = new Upload(buffer);
        upload.setMaxFiles(1);
        upload.setAcceptedFileTypes("image/*", "application/pdf", ".doc", ".docx", ".xls", ".xlsx", ".txt", ".csv");
        upload.setMaxFileSize(10 * 1024 * 1024);
        upload.setUploadButton(new Button("Sostituisci File"));

        upload.addSucceededListener(event -> {
            try {
                InputStream inputStream = buffer.getInputStream();
                newFileData = inputStream.readAllBytes();
                newFileName = event.getFileName();
                newFileType = event.getMIMEType();
                newFileSize = (long) newFileData.length;

                uploadStatus.setText("✓ Nuovo file: " + newFileName + 
                                   " (" + formatFileSize(newFileSize) + ")");
                uploadStatus.getStyle().set("color", "var(--lumo-success-color)");
            } catch (Exception e) {
                Notification.show("Errore durante il caricamento del file", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        Button removeFileButton = new Button("Rimuovi File", new Icon(VaadinIcon.TRASH));
        removeFileButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        removeFileButton.setEnabled(product.hasFile());
        removeFileButton.addClickListener(e -> {
            if (confirmRemoveFile()) {
                product.setFileData(null);
                product.setFileName(null);
                product.setFileType(null);
                product.setFileSize(null);
                newFileData = null;
                updateFileStatus();
                removeFileButton.setEnabled(false);
            }
        });

        HorizontalLayout fileButtons = new HorizontalLayout(upload, removeFileButton);
        fileButtons.setAlignItems(FlexComponent.Alignment.CENTER);

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
        metadataField.setValue(product.getMetadata() != null ? product.getMetadata() : "");
        metadataField.setPlaceholder("{\n  \"fornitore\": \"Nome Fornitore\",\n  \"codice\": \"ABC123\",\n  \"note\": \"Note aggiuntive\"\n}");
        metadataField.setWidthFull();
        metadataField.setHeight("150px");
        metadataField.getStyle().set("font-family", "monospace");

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
                fileTitle,
                uploadStatus,
                fileButtons,
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

        if (product.getUploadedBy() != null && !product.getUploadedBy().isEmpty()) {
            Span uploadedByLabel = new Span("Caricato da: ");
            uploadedByLabel.getStyle().set("font-weight", "500");
            Span uploadedByValue = new Span(product.getUploadedBy());
            HorizontalLayout uploadedByLayout = new HorizontalLayout(uploadedByLabel, uploadedByValue);
            uploadedByLayout.setSpacing(false);
            section.add(uploadedByLayout);
        }

        if (product.getDateCreated() != null) {
            Span dateLabel = new Span("Data creazione: ");
            dateLabel.getStyle().set("font-weight", "500");
            Span dateValue = new Span(product.getDateCreated().format(DATE_FORMATTER));
            HorizontalLayout dateLayout = new HorizontalLayout(dateLabel, dateValue);
            dateLayout.setSpacing(false);
            section.add(dateLayout);
        }

        if (product.getLastUpdated() != null) {
            Span updateLabel = new Span("Ultimo aggiornamento: ");
            updateLabel.getStyle().set("font-weight", "500");
            Span updateValue = new Span(product.getLastUpdated().format(DATE_FORMATTER));
            HorizontalLayout updateLayout = new HorizontalLayout(updateLabel, updateValue);
            updateLayout.setSpacing(false);
            section.add(updateLayout);
        }

        return section;
    }

    private void updateFileStatus() {
        if (product.hasFile()) {
            uploadStatus.setText("File attuale: " + product.getFileName() + 
                               " (" + product.getFormattedFileSize() + ")");
            uploadStatus.getStyle().set("color", "var(--lumo-contrast-70pct)");
        } else {
            uploadStatus.setText("Nessun file allegato");
            uploadStatus.getStyle().set("color", "var(--lumo-secondary-text-color)");
        }
    }

    private boolean confirmRemoveFile() {
        // In a real app, you'd use a ConfirmDialog here
        return true;
    }

    private void save() {
        if (nameField.isEmpty()) {
            Notification.show("Il nome del prodotto è obbligatorio", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (priceField.isEmpty() || priceField.getValue().compareTo(BigDecimal.ZERO) < 0) {
            Notification.show("Inserisci un prezzo valido", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (categoryField.isEmpty()) {
            Notification.show("La categoria è obbligatoria", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            product.setName(nameField.getValue());
            product.setDescription(descriptionField.getValue());
            product.setPrice(priceField.getValue());
            product.setCategory(categoryField.getValue());
            product.setQuantity(quantityField.getValue());
            product.setMetadata(metadataField.getValue().trim().isEmpty() ? null : metadataField.getValue());

            // Update file if new one uploaded
            if (newFileData != null && newFileData.length > 0) {
                product.setFileName(newFileName);
                product.setFileType(newFileType);
                product.setFileSize(newFileSize);
                product.setFileData(newFileData);
            }

            productService.save(product);

            Notification.show("Prodotto modificato con successo!", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            if (onSuccessCallback != null) {
                onSuccessCallback.accept(product);
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
}
