package io.bootify.my_app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.StringLengthValidator;
import io.bootify.my_app.domain.Permission;
import io.bootify.my_app.domain.Profile;
import io.bootify.my_app.service.PermissionService;
import io.bootify.my_app.service.ProfileService;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Consumer;
import java.util.Locale;

public class ProfileFormDialog extends Dialog {

    private final ProfileService profileService;
    private final PermissionService permissionService;
    private final Binder<Profile> binder;
    private final Profile profile;
    private final Consumer<Profile> onSaveCallback;

    private TextField nameField;
    private TextArea descriptionField;
    private Checkbox activeCheckbox;
    private DateTimePicker validFromPicker;
    private DateTimePicker validToPicker;
    private Map<Long, Checkbox> permissionCheckboxes;

    public ProfileFormDialog(Profile profile, ProfileService profileService,
            PermissionService permissionService, Consumer<Profile> onSaveCallback) {
        this.profile = profile != null ? profile : new Profile();
        this.profileService = profileService;
        this.permissionService = permissionService;
        this.onSaveCallback = onSaveCallback;
        this.binder = new Binder<>(Profile.class);
        this.permissionCheckboxes = new HashMap<>();

        setHeaderTitle(profile != null && profile.getId() != null ? "Modifica Profilo" : "Nuovo Profilo");
        setWidth("800px");
        setMaxHeight("90vh");

        createForm();
        createFooter();

        if (this.profile.getId() != null) {
            binder.readBean(this.profile);
            loadPermissions();
        }
    }

    private void createForm() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        // Basic info section
        FormLayout basicInfoForm = new FormLayout();
        basicInfoForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        nameField = new TextField("Nome Profilo");
        nameField.setRequired(true);
        nameField.setPlaceholder("es. Amministratore, Operatore, Visualizzatore");
        nameField.setWidthFull();

        descriptionField = new TextArea("Descrizione");
        descriptionField.setPlaceholder("Descrizione del profilo e delle sue responsabilità");
        descriptionField.setWidthFull();
        descriptionField.setMaxLength(500);
        descriptionField.setHelperText("Max 500 caratteri");

        activeCheckbox = new Checkbox("Profilo attivo");
        activeCheckbox.setValue(profile.getId() == null ? true : profile.getActive());

        validFromPicker = new DateTimePicker("Valido da");
        validFromPicker.setWidthFull();
        validFromPicker.setHelperText("Data e ora da cui il profilo è valido");
        validFromPicker.setLocale(Locale.ITALY);
        validFromPicker.setDatePlaceholder("gg/mm/aaaa");
        validFromPicker.setTimePlaceholder("hh:mm");

        validToPicker = new DateTimePicker("Valido fino a");
        validToPicker.setWidthFull();
        validToPicker.setHelperText("Data e ora fino a cui il profilo è valido");
        validToPicker.setLocale(Locale.ITALY);
        validToPicker.setDatePlaceholder("gg/mm/aaaa");
        validToPicker.setTimePlaceholder("hh:mm");

        basicInfoForm.add(nameField, 2);
        basicInfoForm.add(descriptionField, 2);
        basicInfoForm.add(activeCheckbox, 2);
        basicInfoForm.add(validFromPicker, 1);
        basicInfoForm.add(validToPicker, 1);

        // Binders
        binder.forField(nameField)
                .withValidator(new StringLengthValidator("Il nome è obbligatorio", 1, 100))
                .withValidator(name -> !isDuplicateName(name), "Esiste già un profilo con questo nome")
                .bind(Profile::getName, Profile::setName);

        binder.forField(descriptionField)
                .withValidator(new StringLengthValidator("Massimo 500 caratteri", 0, 500))
                .bind(Profile::getDescription, Profile::setDescription);

        binder.forField(activeCheckbox)
                .bind(Profile::getActive, Profile::setActive);

        binder.forField(validFromPicker)
                .bind(
                    profile -> profile.getValidFrom() != null ? profile.getValidFrom().toLocalDateTime() : null,
                    (profile, localDateTime) -> profile.setValidFrom(localDateTime != null ? OffsetDateTime.of(localDateTime, ZoneId.systemDefault().getRules().getOffset(localDateTime)) : null)
                );

        binder.forField(validToPicker)
                .withValidator(validTo -> {
                    if (validTo != null && validFromPicker.getValue() != null) {
                        return validTo.isAfter(validFromPicker.getValue());
                    }
                    return true;
                }, "La data 'Valido fino a' deve essere successiva a 'Valido da'")
                .bind(
                    profile -> profile.getValidTo() != null ? profile.getValidTo().toLocalDateTime() : null,
                    (profile, localDateTime) -> profile.setValidTo(localDateTime != null ? OffsetDateTime.of(localDateTime, ZoneId.systemDefault().getRules().getOffset(localDateTime)) : null)
                );

        // Permissions section
        H3 permissionsTitle = new H3("Permessi");
        permissionsTitle.getStyle()
                .set("margin-top", "var(--lumo-space-l)")
                .set("margin-bottom", "var(--lumo-space-m)");

        Span permissionsHelper = new Span("Seleziona i permessi da assegnare a questo profilo");
        permissionsHelper.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        VerticalLayout permissionsLayout = createPermissionsLayout();

        content.add(basicInfoForm, permissionsTitle, permissionsHelper, permissionsLayout);
        add(content);
    }

    private VerticalLayout createPermissionsLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        List<String> categories = permissionService.findAllCategories();

        for (String category : categories) {
            Div categorySection = new Div();
            categorySection.getStyle()
                    .set("border", "1px solid var(--lumo-contrast-10pct)")
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("padding", "var(--lumo-space-m)")
                    .set("margin-bottom", "var(--lumo-space-s)");

            H3 categoryTitle = new H3(getCategoryDisplayName(category));
            categoryTitle.getStyle()
                    .set("margin", "0 0 var(--lumo-space-s) 0")
                    .set("font-size", "var(--lumo-font-size-m)")
                    .set("color", "var(--lumo-primary-text-color)");

            VerticalLayout checkboxesLayout = new VerticalLayout();
            checkboxesLayout.setPadding(false);
            checkboxesLayout.setSpacing(false);
            checkboxesLayout.getStyle().set("margin-left", "var(--lumo-space-s)");

            List<Permission> categoryPermissions = permissionService.findByCategory(category);

            for (Permission permission : categoryPermissions) {
                Checkbox checkbox = new Checkbox(permission.getDescription());
                checkbox.getStyle().set("margin", "var(--lumo-space-xs) 0");
                permissionCheckboxes.put(permission.getId(), checkbox);
                checkboxesLayout.add(checkbox);
            }

            categorySection.add(categoryTitle, checkboxesLayout);
            layout.add(categorySection);
        }

        return layout;
    }

    private void loadPermissions() {
        if (profile.getId() != null) {
            Optional<Profile> fullProfile = profileService.findByIdWithPermissions(profile.getId());
            fullProfile.ifPresent(p -> {
                Set<Long> permissionIds = new HashSet<>();
                for (Permission perm : p.getPermissions()) {
                    permissionIds.add(perm.getId());
                }
                for (Map.Entry<Long, Checkbox> entry : permissionCheckboxes.entrySet()) {
                    entry.getValue().setValue(permissionIds.contains(entry.getKey()));
                }
            });
        }
    }

    private void createFooter() {
        Button cancelButton = new Button("Annulla", e -> close());

        Button saveButton = new Button("Salva", e -> save());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout footer = new HorizontalLayout(cancelButton, saveButton);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.setWidthFull();
        footer.setPadding(true);

        getFooter().add(footer);
    }

    private void save() {
        try {
            binder.writeBean(profile);

            // Collect all selected permissions
            Set<Permission> selectedPermissions = new HashSet<>();
            for (Map.Entry<Long, Checkbox> entry : permissionCheckboxes.entrySet()) {
                if (entry.getValue().getValue()) {
                    permissionService.findById(entry.getKey()).ifPresent(selectedPermissions::add);
                }
            }

            if (selectedPermissions.isEmpty()) {
                Notification.show("Seleziona almeno un permesso", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            profile.setPermissions(selectedPermissions);

            Profile savedProfile = profileService.save(profile);

            Notification.show(
                    profile.getId() == null ? "Profilo creato con successo" : "Profilo aggiornato con successo",
                    3000,
                    Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            if (onSaveCallback != null) {
                onSaveCallback.accept(savedProfile);
            }

            close();
        } catch (ValidationException ex) {
            Notification.show("Correggi gli errori nel form", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception ex) {
            Notification.show("Errore durante il salvataggio: " + ex.getMessage(),
                    3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            ex.printStackTrace();
        }
    }

    private boolean isDuplicateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        if (profile.getId() == null) {
            return profileService.existsByName(name.trim());
        } else {
            return profileService.existsByNameAndIdNot(name.trim(), profile.getId());
        }
    }

    private String getCategoryDisplayName(String category) {
        return switch (category) {
            case "USERS" -> "👥 Utenti";
            case "PRODUCTS" -> "📦 Prodotti";
            case "FILES" -> "📁 File";
            case "REPORTS" -> "📊 Report";
            case "SYSTEM" -> "⚙️ Sistema";
            default -> category;
        };
    }
}
