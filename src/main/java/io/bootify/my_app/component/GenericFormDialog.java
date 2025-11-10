package io.bootify.my_app.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Dialog generico per inserimento/modifica di qualsiasi entità
 * @param <T> Il tipo dell'entità
 */
public class GenericFormDialog<T> extends Dialog {
    
    private final Binder<T> binder;
    private final T entity;
    private final Consumer<T> saveCallback;
    private final boolean isEditMode;
    private final String entityName;
    private final FormLayout formLayout;
    
    public GenericFormDialog(String entityName, T entity, Binder<T> binder, 
                             BiConsumer<FormLayout, Binder<T>> formConfigurator, 
                             Consumer<T> saveCallback) {
        this.entityName = entityName;
        this.entity = entity;
        this.binder = binder;
        this.saveCallback = saveCallback;
        this.isEditMode = entity != null;
        this.formLayout = new FormLayout();
        
        setModal(true);
        setWidth("600px");
        
        createForm(formConfigurator);
        createButtons();
        
        if (isEditMode && entity != null) {
            binder.readBean(entity);
        }
    }
    
    private void createForm(BiConsumer<FormLayout, Binder<T>> formConfigurator) {
        H3 title = new H3(isEditMode ? "Modifica " + entityName : "Nuovo " + entityName);
        
        // Configura il form dall'esterno passando anche il binder
        formConfigurator.accept(formLayout, binder);
        
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
            binder.writeBean(entity);
            saveCallback.accept(entity);
            
            String message = isEditMode ? 
                    entityName + " modificato con successo" : 
                    entityName + " creato con successo";
            Notification.show(message, 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            
            close();
        } catch (ValidationException e) {
            Notification.show("Compila tutti i campi obbligatori", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Errore durante il salvataggio: " + e.getMessage(), 
                    3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
