package io.bootify.my_app.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import io.bootify.my_app.domain.Product;

import java.math.BigDecimal;
import java.util.function.Consumer;

/**
 * Dialog semplice per inserimento/modifica Product
 */
public class ProductFormDialog extends Dialog {
    
    private final Binder<Product> binder = new Binder<>(Product.class);
    private final Product product;
    private final Consumer<Product> saveCallback;
    private final boolean isEditMode;
    
    private TextField nameField;
    private ComboBox<String> categoryField;
    private NumberField priceField;
    private IntegerField quantityField;
    private TextArea descriptionField;
    
    public ProductFormDialog(Product product, Consumer<Product> saveCallback) {
        this.product = product != null ? product : new Product();
        this.saveCallback = saveCallback;
        this.isEditMode = product != null;
        
        setModal(true);
        setWidth("600px");
        
        createForm();
        createButtons();
        
        if (isEditMode) {
            binder.readBean(product);
        }
    }
    
    private void createForm() {
        H3 title = new H3(isEditMode ? "Modifica Prodotto" : "Nuovo Prodotto");
        
        // Campi del form
        nameField = new TextField("Nome");
        nameField.setWidthFull();
        nameField.setRequiredIndicatorVisible(true);
        
        categoryField = new ComboBox<>("Categoria");
        categoryField.setItems("Elettronica", "Accessori", "Audio", "Componenti",
                "Rete", "Periferiche", "Arredamento", "Smart Home", "Storage");
        categoryField.setAllowCustomValue(true);
        categoryField.addCustomValueSetListener(e -> categoryField.setValue(e.getDetail()));
        categoryField.setWidthFull();
        categoryField.setRequiredIndicatorVisible(true);
        
        priceField = new NumberField("Prezzo");
        priceField.setPrefixComponent(new com.vaadin.flow.component.html.Span("€"));
        priceField.setMin(0);
        priceField.setWidthFull();
        priceField.setRequiredIndicatorVisible(true);
        
        quantityField = new IntegerField("Quantità");
        quantityField.setMin(0);
        quantityField.setWidthFull();
        quantityField.setRequiredIndicatorVisible(true);
        
        descriptionField = new TextArea("Descrizione");
        descriptionField.setWidthFull();
        descriptionField.setMinHeight("100px");
        
        // Binding
        binder.forField(nameField)
                .asRequired("Nome è obbligatorio")
                .bind(Product::getName, Product::setName);
        
        binder.forField(categoryField)
                .asRequired("Categoria è obbligatoria")
                .bind(Product::getCategory, Product::setCategory);
        
        binder.forField(priceField)
                .asRequired("Prezzo è obbligatorio")
                .withConverter(
                        value -> value != null ? BigDecimal.valueOf(value) : null,
                        value -> value != null ? value.doubleValue() : null
                )
                .bind(Product::getPrice, Product::setPrice);
        
        binder.forField(quantityField)
                .asRequired("Quantità è obbligatoria")
                .bind(Product::getQuantity, Product::setQuantity);
        
        binder.forField(descriptionField)
                .bind(Product::getDescription, Product::setDescription);
        
        // Layout
        FormLayout formLayout = new FormLayout();
        formLayout.add(nameField, categoryField, priceField, quantityField, descriptionField);
        formLayout.setColspan(descriptionField, 2);
        
        VerticalLayout content = new VerticalLayout(title, formLayout);
        content.setPadding(false);
        add(content);
    }
    
    private void createButtons() {
        Button saveButton = new Button("Salva");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> save());
        
        Button cancelButton = new Button("Annulla");
        cancelButton.addClickListener(e -> close());
        
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();
        
        add(buttons);
    }
    
    private void save() {
        try {
            binder.writeBean(product);
            saveCallback.accept(product);
            
            String message = isEditMode ? "Prodotto modificato con successo" : "Prodotto creato con successo";
            Notification.show(message, 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            close();
        } catch (ValidationException e) {
            Notification.show("Compila tutti i campi obbligatori", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
