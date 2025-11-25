package io.bootify.my_app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import io.bootify.my_app.domain.Profile;
import io.bootify.my_app.domain.User;
import io.bootify.my_app.service.ProfileService;
import io.bootify.my_app.service.UserService;

import java.util.function.Consumer;

public class UserFormDialog extends Dialog {

    private final UserService userService;
    private final ProfileService profileService;
    private final Binder<User> binder;
    private final User user;
    private final Consumer<User> onSaveCallback;

    private TextField usernameField;
    private EmailField emailField;
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField phoneField;
    private TextField departmentField;
    private ComboBox<Profile> profileComboBox;
    private TextArea notesField;
    private Checkbox activeCheckbox;
    private Div permissionPreview;

    public UserFormDialog(User user, UserService userService, ProfileService profileService,
            Consumer<User> onSaveCallback) {
        this.user = user != null ? user : new User();
        this.userService = userService;
        this.profileService = profileService;
        this.onSaveCallback = onSaveCallback;
        this.binder = new Binder<>(User.class);

        setHeaderTitle(user != null && user.getId() != null ? "Modifica Utente" : "Nuovo Utente");
        setWidth("700px");
        setMaxHeight("90vh");

        createForm();
        createFooter();

        if (this.user.getId() != null) {
            binder.readBean(this.user);
            updatePermissionPreview();
        }
    }

    private void createForm() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        // Username
        usernameField = new TextField("Username");
        usernameField.setRequired(true);
        usernameField.setPrefixComponent(new Icon(VaadinIcon.USER));
        usernameField.setPlaceholder("es. mario.rossi");

        // Email
        emailField = new EmailField("Email");
        emailField.setRequired(true);
        emailField.setPrefixComponent(new Icon(VaadinIcon.ENVELOPE));
        emailField.setPlaceholder("mario.rossi@example.com");

        // First Name
        firstNameField = new TextField("Nome");
        firstNameField.setRequired(true);
        firstNameField.setPlaceholder("Mario");

        // Last Name
        lastNameField = new TextField("Cognome");
        lastNameField.setRequired(true);
        lastNameField.setPlaceholder("Rossi");

        // Phone
        phoneField = new TextField("Telefono");
        phoneField.setPrefixComponent(new Icon(VaadinIcon.PHONE));
        phoneField.setPlaceholder("+39 123 456 7890");

        // Department
        departmentField = new TextField("Dipartimento");
        departmentField.setPrefixComponent(new Icon(VaadinIcon.BUILDING));
        departmentField.setPlaceholder("es. IT, Vendite, Amministrazione");

        // Profile
        profileComboBox = new ComboBox<>("Profilo");
        profileComboBox.setRequired(true);
        profileComboBox.setItems(profileService.findActive());
        profileComboBox.setItemLabelGenerator(Profile::getName);
        profileComboBox.setPlaceholder("Seleziona un profilo");
        profileComboBox.addValueChangeListener(e -> updatePermissionPreview());

        // Active
        activeCheckbox = new Checkbox("Utente attivo");
        activeCheckbox.setValue(user.getId() == null ? true : user.getActive());

        // Notes
        notesField = new TextArea("Note");
        notesField.setPlaceholder("Note aggiuntive sull'utente");
        notesField.setMaxLength(500);
        notesField.setHelperText("Max 500 caratteri");

        // Add fields to form
        formLayout.add(usernameField, emailField);
        formLayout.add(firstNameField, lastNameField);
        formLayout.add(phoneField, departmentField);
        formLayout.add(profileComboBox, 2);
        formLayout.add(activeCheckbox, 2);
        formLayout.add(notesField, 2);

        // Binders
        binder.forField(usernameField)
                .withValidator(new StringLengthValidator("Username obbligatorio (3-100 caratteri)", 3, 100))
                .withValidator(username -> !isDuplicateUsername(username), "Username già in uso")
                .bind(User::getUsername, User::setUsername);

        binder.forField(emailField)
                .withValidator(new EmailValidator("Email non valida"))
                .withValidator(email -> !isDuplicateEmail(email), "Email già in uso")
                .bind(User::getEmail, User::setEmail);

        binder.forField(firstNameField)
                .withValidator(new StringLengthValidator("Nome obbligatorio (1-100 caratteri)", 1, 100))
                .bind(User::getFirstName, User::setFirstName);

        binder.forField(lastNameField)
                .withValidator(new StringLengthValidator("Cognome obbligatorio (1-100 caratteri)", 1, 100))
                .bind(User::getLastName, User::setLastName);

        binder.forField(phoneField)
                .withValidator(new StringLengthValidator("Massimo 20 caratteri", 0, 20))
                .bind(User::getPhone, User::setPhone);

        binder.forField(departmentField)
                .withValidator(new StringLengthValidator("Massimo 100 caratteri", 0, 100))
                .bind(User::getDepartment, User::setDepartment);

        binder.forField(profileComboBox)
                .asRequired("Seleziona un profilo")
                .bind(User::getProfile, User::setProfile);

        binder.forField(activeCheckbox)
                .bind(User::getActive, User::setActive);

        binder.forField(notesField)
                .withValidator(new StringLengthValidator("Massimo 500 caratteri", 0, 500))
                .bind(User::getNotes, User::setNotes);

        // Permission preview section
        permissionPreview = new Div();
        permissionPreview.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("margin-top", "var(--lumo-space-m)");

        content.add(formLayout, permissionPreview);
        add(content);
    }

    private void updatePermissionPreview() {
        permissionPreview.removeAll();

        Profile selectedProfile = profileComboBox.getValue();
        if (selectedProfile == null) {
            Span hint = new Span("Seleziona un profilo per visualizzare i permessi associati");
            hint.getStyle().set("color", "var(--lumo-secondary-text-color)");
            permissionPreview.add(hint);
            return;
        }

        H4 title = new H4("Permessi del profilo: " + selectedProfile.getName());
        title.getStyle().set("margin", "0 0 var(--lumo-space-s) 0");

        permissionPreview.add(title);

        if (selectedProfile.getDescription() != null && !selectedProfile.getDescription().isEmpty()) {
            Span description = new Span(selectedProfile.getDescription());
            description.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("display", "block")
                    .set("margin-bottom", "var(--lumo-space-m)");
            permissionPreview.add(description);
        }

        profileService.findByIdWithPermissions(selectedProfile.getId()).ifPresent(profile -> {
            if (profile.getPermissions().isEmpty()) {
                Span noPermissions = new Span("⚠️ Questo profilo non ha permessi assegnati");
                noPermissions.getStyle().set("color", "var(--lumo-error-text-color)");
                permissionPreview.add(noPermissions);
            } else {
                VerticalLayout permissionsList = new VerticalLayout();
                permissionsList.setPadding(false);
                permissionsList.setSpacing(false);

                profile.getPermissions().stream()
                        .sorted((p1, p2) -> {
                            int catCompare = p1.getCategory().compareTo(p2.getCategory());
                            return catCompare != 0 ? catCompare : p1.getName().compareTo(p2.getName());
                        })
                        .forEach(permission -> {
                            Icon icon = new Icon(VaadinIcon.CHECK_CIRCLE);
                            icon.setSize("16px");
                            icon.setColor("var(--lumo-success-color)");

                            Span permName = new Span(permission.getDescription());
                            permName.getStyle().set("font-size", "var(--lumo-font-size-s)");

                            HorizontalLayout permRow = new HorizontalLayout(icon, permName);
                            permRow.setAlignItems(FlexComponent.Alignment.CENTER);
                            permRow.setSpacing(true);
                            permRow.getStyle().set("padding", "var(--lumo-space-xs) 0");

                            permissionsList.add(permRow);
                        });

                permissionPreview.add(permissionsList);
            }
        });
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
            binder.writeBean(user);

            User savedUser = userService.save(user);

            Notification.show(
                    user.getId() == null ? "Utente creato con successo" : "Utente aggiornato con successo",
                    3000,
                    Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            if (onSaveCallback != null) {
                onSaveCallback.accept(savedUser);
            }

            close();
        } catch (ValidationException ex) {
            Notification.show("Correggi gli errori nel form", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception ex) {
            Notification.show("Errore durante il salvataggio: " + ex.getMessage(),
                    3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private boolean isDuplicateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (user.getId() == null) {
            return userService.existsByUsername(username.trim());
        } else {
            return userService.existsByUsernameAndIdNot(username.trim(), user.getId());
        }
    }

    private boolean isDuplicateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        if (user.getId() == null) {
            return userService.existsByEmail(email.trim());
        } else {
            return userService.existsByEmailAndIdNot(email.trim(), user.getId());
        }
    }
}
