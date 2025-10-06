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
import java.util.function.Consumer;

public class ProdottoUploadDialog extends Dialog {

    private final ProductService productService;
    private final Consumer<Product> onSuccessCallback;

    private final TextField nameField;
    private final TextArea descriptionField;
    private final BigDecimalField priceField;
    private final TextField categoryField;
    private final IntegerField quantityField;
    private final TextField uploaderField;
    private final MemoryBuffer buffer;
    private final Upload upload;
    private final Span uploadStatus;
    private Button saveButton;

    private byte[] fileData;
    private String uploadedFileName;
    private String uploadedMimeType;
    private Long uploadedFileSize;

    public ProdottoUploadDialog(ProductService productService, Consumer<Product> onSuccessCallback) {
        this.productService = productService;
        this.onSuccessCallback = onSuccessCallback;

        setModal(true);
        setDraggable(false);
        setResizable(false);
        setWidth("700px");

        // Header
        H2 title = new H2("Aggiungi Nuovo Prodotto");
        title.getStyle().set("margin", "0");

        // Product fields
        nameField = new TextField("Nome Prodotto");
        nameField.setPlaceholder("Nome del prodotto");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setWidthFull();

        descriptionField = new TextArea("Descrizione");
        descriptionField.setPlaceholder("Descrizione del prodotto...");
        descriptionField.setMaxLength(1000);
        descriptionField.setWidthFull();

        priceField = new BigDecimalField("Prezzo");
        priceField.setPrefixComponent(new Span("€"));
        priceField.setRequiredIndicatorVisible(true);
        priceField.setValue(BigDecimal.ZERO);

        categoryField = new TextField("Categoria");
        categoryField.setPlaceholder("es. Elettronica, Abbigliamento...");
        categoryField.setRequiredIndicatorVisible(true);

        quantityField = new IntegerField("Quantità");
        quantityField.setValue(0);
        quantityField.setMin(0);
        quantityField.setRequiredIndicatorVisible(true);

        uploaderField = new TextField("Caricato da");
        uploaderField.setPlaceholder("Nome utente");

        // Upload component
        buffer = new MemoryBuffer();
        upload = new Upload(buffer);
        upload.setMaxFiles(1);
        upload.setAcceptedFileTypes("image/*", "application/pdf", ".doc", ".docx", ".xls", ".xlsx", ".txt", ".csv");
        upload.setMaxFileSize(10 * 1024 * 1024); // 10 MB

        uploadStatus = new Span("Nessun file allegato (opzionale)");
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

                uploadStatus.setText("✓ File allegato: " + uploadedFileName + 
                                   " (" + formatFileSize(uploadedFileSize) + ")");
                uploadStatus.getStyle().set("color", "var(--lumo-success-color)");
            } catch (Exception e) {
                Notification.show("Errore durante il caricamento del file", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        upload.addFileRejectedListener(event -> {
            Notification.show(event.getErrorMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });

        FormLayout formLayout = new FormLayout();
        formLayout.add(nameField, descriptionField);
        formLayout.add(priceField, categoryField, quantityField);
        formLayout.add(uploaderField);
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setColspan(nameField, 2);
        formLayout.setColspan(descriptionField, 2);
        formLayout.setColspan(uploaderField, 2);

        // File upload section
        Span fileLabel = new Span("Allega File (opzionale)");
        fileLabel.getStyle().set("font-weight", "500");
        Div uploadSection = new Div(fileLabel, upload, uploadStatus);
        uploadSection.getStyle().set("margin-top", "var(--lumo-space-m)");

        // Buttons
        saveButton = new Button("Salva Prodotto", event -> save());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annulla", event -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();
        buttonLayout.getStyle().set("margin-top", "var(--lumo-space-m)");

        // Layout
        VerticalLayout layout = new VerticalLayout(
                title,
                formLayout,
                uploadSection,
                buttonLayout
        );
        layout.setPadding(true);
        layout.setSpacing(true);

        add(layout);
    }

    private void save() {
        if (nameField.isEmpty()) {
            Notification.show("Inserisci il nome del prodotto", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (priceField.isEmpty() || priceField.getValue().compareTo(BigDecimal.ZERO) < 0) {
            Notification.show("Inserisci un prezzo valido", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (categoryField.isEmpty()) {
            Notification.show("Inserisci la categoria", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (quantityField.isEmpty()) {
            Notification.show("Inserisci la quantità", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            Product product = new Product();
            product.setName(nameField.getValue());
            product.setDescription(descriptionField.getValue());
            product.setPrice(priceField.getValue());
            product.setCategory(categoryField.getValue());
            product.setQuantity(quantityField.getValue());
            product.setUploadedBy(uploaderField.getValue());

            // Add file if uploaded
            if (fileData != null && fileData.length > 0) {
                product.setFileName(uploadedFileName);
                product.setFileType(uploadedMimeType);
                product.setFileSize(uploadedFileSize);
                product.setFileData(fileData);
            }

            productService.save(product);

            Notification.show("Prodotto creato con successo!", 3000, Notification.Position.MIDDLE)
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

    @Override
    public void open() {
        super.open();
        // Reset form
        nameField.clear();
        descriptionField.clear();
        priceField.setValue(BigDecimal.ZERO);
        categoryField.clear();
        quantityField.setValue(0);
        uploaderField.clear();
        upload.clearFileList();
        uploadStatus.setText("Nessun file allegato (opzionale)");
        uploadStatus.getStyle().set("color", "var(--lumo-secondary-text-color)");
        fileData = null;
        uploadedFileName = null;
        uploadedMimeType = null;
        uploadedFileSize = null;
    }
}
