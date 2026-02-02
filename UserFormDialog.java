package io.bootify.my_app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
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
import io.bootify.my_app.domain.UserProfile;
import io.bootify.my_app.dto.UserProfileDto;
import io.bootify.my_app.service.PermissionService;
import io.bootify.my_app.service.ProfileService;
import io.bootify.my_app.service.UserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public class UserFormDialog extends Dialog {

    private final UserService userService;
    private final ProfileService profileService;
    private final PermissionService permissionService;
    private final Binder<User> binder;
    private final User user;
    private final Consumer<User> onSaveCallback;

    private TextField usernameField;
    private EmailField emailField;
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField phoneField;
    private TextField departmentField;
    private Grid<UserProfileDto> profileGrid;
    private List<UserProfileDto> userProfileDtos = new ArrayList<>();
    private TextArea notesField;
    private ComboBox<Boolean> activeComboBox;
    private Div permissionPreview;

    public UserFormDialog(User user, UserService userService, ProfileService profileService,
            PermissionService permissionService, Consumer<User> onSaveCallback) {
        this.user = user != null ? user : new User();
        this.userService = userService;
        this.profileService = profileService;
        this.permissionService = permissionService;
        this.onSaveCallback = onSaveCallback;
        this.binder = new Binder<>(User.class);

        setHeaderTitle(user != null && user.getId() != null ? "Modifica Utente" : "Nuovo Utente");
        setWidth("700px");
        setMaxHeight("90vh");

        createForm();
        createFooter();

        if (this.user.getId() != null) {
            binder.readBean(this.user);
            // Carica i profili esistenti nella grid
            loadExistingProfiles();
            updatePermissionPreview();
        }
    }
    
    private void loadExistingProfiles() {
        userProfileDtos.clear();
        if (user.getUserProfiles() != null) {
            for (UserProfile up : user.getUserProfiles()) {
                UserProfileDto dto = new UserProfileDto();
                dto.setProfile(up.getProfile());
                dto.setStartDate(up.getStartDate());
                dto.setEndDate(up.getEndDate());
                userProfileDtos.add(dto);
            }
        }
        profileGrid.getDataProvider().refreshAll();
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

        // Profile Grid - Grid con profilo, data inizio e fine
        profileGrid = new Grid<>(UserProfileDto.class, false);
        profileGrid.setHeight("300px");
        
        // Colonna Profilo
        Grid.Column<UserProfileDto> profileColumn = profileGrid.addColumn(dto -> 
            dto.getProfile() != null ? dto.getProfile().getName() : "")
            .setHeader("Profilo")
            .setFlexGrow(2);
        
        // Colonna Data Inizio
        Grid.Column<UserProfileDto> startDateColumn = profileGrid.addColumn(dto -> 
            dto.getStartDate() != null ? dto.getStartDate().toString() : "")
            .setHeader("Data Inizio")
            .setFlexGrow(1);
        
        // Colonna Data Fine
        Grid.Column<UserProfileDto> endDateColumn = profileGrid.addColumn(dto -> 
            dto.getEndDate() != null ? dto.getEndDate().toString() : "")
            .setHeader("Data Fine")
            .setFlexGrow(1);
        
        // Colonna Azioni
        profileGrid.addComponentColumn(dto -> {
            Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ICON);
            deleteBtn.addClickListener(e -> {
                userProfileDtos.remove(dto);
                profileGrid.getDataProvider().refreshAll();
                updatePermissionPreview();
            });
            return deleteBtn;
        }).setHeader("Azioni").setFlexGrow(0).setAutoWidth(true);
        
        // Editor per la grid
        Editor<UserProfileDto> editor = profileGrid.getEditor();
        Binder<UserProfileDto> editorBinder = new Binder<>(UserProfileDto.class);
        editor.setBinder(editorBinder);
        
        // Editor per profilo
        ComboBox<Profile> profileComboBox = new ComboBox<>();
        profileComboBox.setItemLabelGenerator(Profile::getName);
        profileComboBox.setWidthFull();
        profileComboBox.setPlaceholder("Seleziona un profilo");
        
        editorBinder.forField(profileComboBox)
            .asRequired("Profilo obbligatorio")
            .withValidator(profile -> {
                if (profile == null) return true;
                // Verifica che il profilo non sia già presente in altre righe
                UserProfileDto currentItem = editor.getItem();
                boolean isDuplicate = userProfileDtos.stream()
                    .filter(dto -> dto != currentItem)
                    .anyMatch(dto -> dto.getProfile() != null && dto.getProfile().getId().equals(profile.getId()));
                return !isDuplicate;
            }, "Questo profilo è già stato aggiunto")
            .bind(UserProfileDto::getProfile, UserProfileDto::setProfile);
        profileColumn.setEditorComponent(profileComboBox);
        
        // Listener per impedire la selezione di profili duplicati
        profileComboBox.addValueChangeListener(event -> {
            if (event.getValue() != null && editor.getItem() != null) {
                Profile selectedProfile = event.getValue();
                UserProfileDto currentItem = editor.getItem();
                
                // Controlla se il profilo è già stato selezionato in un'altra riga
                boolean isDuplicate = userProfileDtos.stream()
                    .filter(dto -> dto != currentItem)
                    .anyMatch(dto -> dto.getProfile() != null && dto.getProfile().getId().equals(selectedProfile.getId()));
                
                if (isDuplicate) {
                    Notification notification = Notification.show(
                        "Questo profilo è già stato aggiunto", 
                        3000, 
                        Notification.Position.MIDDLE
                    );
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    profileComboBox.setValue(null);
                }
            }
        });
        
        // Quando si apre l'editor, aggiorna la lista dei profili disponibili
        editor.addOpenListener(e -> {
            UserProfileDto currentItem = e.getItem();
            updateAvailableProfilesForComboBox(profileComboBox, currentItem);
            
            // Se non ci sono profili disponibili, mostra un messaggio
            if (profileComboBox.getListDataView().getItemCount() == 0) {
                Notification notification = Notification.show(
                    "Tutti i profili disponibili sono già stati aggiunti", 
                    3000, 
                    Notification.Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                editor.cancel();
            }
        });
        
        // Editor per data inizio
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setWidthFull();
        startDatePicker.setPlaceholder("Opzionale");
        editorBinder.forField(startDatePicker)
            .bind(UserProfileDto::getStartDate, UserProfileDto::setStartDate);
        startDateColumn.setEditorComponent(startDatePicker);
        
        // Editor per data fine
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setWidthFull();
        endDatePicker.setPlaceholder("Opzionale");
        editorBinder.forField(endDatePicker)
            .bind(UserProfileDto::getEndDate, UserProfileDto::setEndDate);
        endDateColumn.setEditorComponent(endDatePicker);
        
        // Abilita edit al doppio click
        profileGrid.addItemDoubleClickListener(e -> {
            editor.editItem(e.getItem());
            profileComboBox.focus();
        });
        
        // Salva le modifiche quando si chiude l'editor
        editor.addCloseListener(e -> {
            UserProfileDto editedItem = e.getItem();
            
            // Se l'editor viene chiuso senza salvare o il profilo è null, rimuovi la riga vuota
            if (editedItem.getProfile() == null) {
                userProfileDtos.remove(editedItem);
            }
            
            profileGrid.getDataProvider().refreshAll();
            updatePermissionPreview();
        });
        
        profileGrid.setItems(userProfileDtos);

        // Bottone per aggiungere nuovo profilo
        Button addProfileButton = new Button("Aggiungi profilo");
        addProfileButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        addProfileButton.setIcon(new Icon(VaadinIcon.PLUS));
        addProfileButton.addClickListener(e -> {
            // Verifica se ci sono ancora profili disponibili
            List<Profile> alreadySelected = userProfileDtos.stream()
                .filter(dto -> dto.getProfile() != null)
                .map(UserProfileDto::getProfile)
                .toList();
            
            List<Profile> availableProfiles = profileService.findActive().stream()
                .filter(p -> !alreadySelected.contains(p))
                .toList();
            
            if (availableProfiles.isEmpty()) {
                Notification notification = Notification.show(
                    "Tutti i profili disponibili sono già stati aggiunti", 
                    3000, 
                    Notification.Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }
            
            UserProfileDto newDto = new UserProfileDto();
            userProfileDtos.add(newDto);
            profileGrid.getDataProvider().refreshAll();
            editor.editItem(newDto);
            profileComboBox.focus();
        });

        // Create profile button
        Button createProfileButton = new Button("Crea nuovo profilo");
        createProfileButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        createProfileButton.setIcon(new Icon(VaadinIcon.PLUS_CIRCLE));
        createProfileButton.addClickListener(e -> openCreateProfileDialog());

        // Info message for profiles
        Div profileInfo = new Div();
        if (profileService.findActive().isEmpty()) {
            Span infoText = new Span("⚠️ Non ci sono profili disponibili. Crea prima un profilo per poter assegnare permessi agli utenti.");
            infoText.getStyle()
                    .set("color", "var(--lumo-warning-text-color)")
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("display", "block")
                    .set("margin-top", "var(--lumo-space-xs)");
            profileInfo.add(infoText);
        }

        // Profile layout with grid and buttons
        VerticalLayout profileLayout = new VerticalLayout();
        profileLayout.setPadding(false);
        profileLayout.setSpacing(true);
        
        Span profileLabel = new Span("Profili e Date di Validità");
        profileLabel.getStyle()
            .set("font-weight", "500")
            .set("font-size", "var(--lumo-font-size-s)")
            .set("color", "var(--lumo-secondary-text-color)");
        
        HorizontalLayout buttonLayout = new HorizontalLayout(addProfileButton, createProfileButton);
        buttonLayout.setSpacing(true);
        buttonLayout.setPadding(false);
        
        profileLayout.add(profileLabel, profileGrid, buttonLayout);
        if (!profileInfo.getChildren().findAny().isEmpty()) {
            profileLayout.add(profileInfo);
        }
        
        // Active
        activeComboBox = new ComboBox<>("Stato Utente");
        activeComboBox.setItems(true, false);
        activeComboBox.setItemLabelGenerator(active -> active ? "Attivo" : "Disattivato");
        activeComboBox.setValue(user.getId() == null ? true : user.getActive());
        activeComboBox.setPlaceholder("Seleziona stato");
        activeComboBox.setAllowCustomValue(false);

        // Notes
        notesField = new TextArea("Note");
        notesField.setPlaceholder("Note aggiuntive sull'utente");
        notesField.setMaxLength(500);
        notesField.setHelperText("Max 500 caratteri");

        // Add fields to form
        formLayout.add(usernameField, emailField);
        formLayout.add(firstNameField, lastNameField);
        formLayout.add(phoneField, departmentField);
        formLayout.add(profileLayout, 2);
        formLayout.add(activeComboBox, 2);
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

        // La gestione dei profili è fatta manualmente tramite la grid
        // Non usiamo più il binder per i profili

        binder.forField(activeComboBox)
                .asRequired("Seleziona lo stato dell'utente")
                .bind(
                    user -> {
                        // Getter custom - aggiungi qui logica personalizzata
                        return user.getActive();
                    },
                    (user, value) -> {
                        // Setter custom - aggiungi qui logica personalizzata
                        user.setActive(value);
                    }
                );

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

        if (userProfileDtos == null || userProfileDtos.isEmpty()) {
            Span hint = new Span("Aggiungi uno o più profili per visualizzare i permessi associati");
            hint.getStyle().set("color", "var(--lumo-secondary-text-color)");
            permissionPreview.add(hint);
            return;
        }
        
        // Filtra solo i profili validi (quelli che hanno un profilo selezionato)
        List<UserProfileDto> validDtos = userProfileDtos.stream()
            .filter(dto -> dto.getProfile() != null)
            .toList();
        
        if (validDtos.isEmpty()) {
            Span hint = new Span("Seleziona almeno un profilo dalla grid");
            hint.getStyle().set("color", "var(--lumo-secondary-text-color)");
            permissionPreview.add(hint);
            return;
        }

        H4 title = new H4("Permessi combinati dei profili selezionati");
        title.getStyle().set("margin", "0 0 var(--lumo-space-s) 0");
        permissionPreview.add(title);

        // Mostra i profili selezionati
        Div profilesDiv = new Div();
        profilesDiv.getStyle()
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("font-size", "var(--lumo-font-size-s)");
        
        validDtos.stream()
                .sorted((p1, p2) -> p1.getProfile().getName().compareTo(p2.getProfile().getName()))
                .forEach(dto -> {
                    Icon profileIcon = new Icon(VaadinIcon.USER_CARD);
                    profileIcon.setSize("14px");
                    profileIcon.getStyle().set("margin-right", "var(--lumo-space-xs)");
                    
                    Span profileName = new Span(dto.getProfile().getName());
                    profileName.getStyle()
                            .set("color", "var(--lumo-primary-text-color)")
                            .set("font-weight", "500");
                    
                    // Aggiungi info sulle date
                    String dateInfo = "";
                    if (dto.getStartDate() != null || dto.getEndDate() != null) {
                        if (dto.getStartDate() != null && dto.getEndDate() != null) {
                            dateInfo = " (" + dto.getStartDate() + " - " + dto.getEndDate() + ")";
                        } else if (dto.getStartDate() != null) {
                            dateInfo = " (da " + dto.getStartDate() + ")";
                        } else {
                            dateInfo = " (fino al " + dto.getEndDate() + ")";
                        }
                    }
                    
                    Span dateSpan = new Span(dateInfo);
                    dateSpan.getStyle()
                            .set("color", "var(--lumo-secondary-text-color)")
                            .set("font-size", "var(--lumo-font-size-xs)");
                    
                    HorizontalLayout profileRow = new HorizontalLayout(profileIcon, profileName, dateSpan);
                    profileRow.setAlignItems(FlexComponent.Alignment.CENTER);
                    profileRow.setSpacing(false);
                    profileRow.getStyle()
                            .set("padding", "var(--lumo-space-xs) var(--lumo-space-s)")
                            .set("background", "var(--lumo-primary-color-10pct)")
                            .set("border-radius", "var(--lumo-border-radius-m)")
                            .set("margin-bottom", "var(--lumo-space-xs)")
                            .set("display", "inline-flex");
                    
                    profilesDiv.add(profileRow);
                });
        
        permissionPreview.add(profilesDiv);

        // Raccoglie tutti i permessi unici dai profili selezionati
        java.util.Set<io.bootify.my_app.domain.Permission> allPermissions = new java.util.HashSet<>();
        validDtos.forEach(dto -> {
            profileService.findByIdWithPermissions(dto.getProfile().getId()).ifPresent(p -> {
                allPermissions.addAll(p.getPermissions());
            });
        });

        if (allPermissions.isEmpty()) {
            Span noPermissions = new Span("⚠️ I profili selezionati non hanno permessi assegnati");
            noPermissions.getStyle().set("color", "var(--lumo-error-text-color)");
            permissionPreview.add(noPermissions);
        } else {
            Span permissionsCount = new Span("Totale permessi unici: " + allPermissions.size());
            permissionsCount.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("display", "block")
                    .set("margin-bottom", "var(--lumo-space-s)");
            permissionPreview.add(permissionsCount);

            VerticalLayout permissionsList = new VerticalLayout();
            permissionsList.setPadding(false);
            permissionsList.setSpacing(false);

            allPermissions.stream()
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
    }

    private void openCreateProfileDialog() {
        ProfileFormDialog dialog = new ProfileFormDialog(null, profileService, permissionService, savedProfile -> {
            // Ricarica la lista dei profili
            refreshProfileList();
            
            Notification.show("Profilo creato! Ora puoi aggiungerlo alla lista.", 
                    3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        dialog.open();
    }

    private void refreshProfileList() {
        // Aggiorna le liste dei profili disponibili negli editor della grid
        // Questo verrà fatto automaticamente quando l'editor viene aperto
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
            // Valida che ci sia almeno un profilo valido
            List<UserProfileDto> validDtos = userProfileDtos.stream()
                .filter(dto -> dto.getProfile() != null)
                .toList();
            
            if (validDtos.isEmpty()) {
                Notification.show("Devi selezionare almeno un profilo", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            
            // Verifica che non ci siano profili duplicati
            long uniqueProfileCount = validDtos.stream()
                .map(UserProfileDto::getProfile)
                .distinct()
                .count();
            
            if (uniqueProfileCount != validDtos.size()) {
                Notification.show("Non puoi associare lo stesso profilo più volte", 
                    3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            
            // Valida le date
            for (UserProfileDto dto : validDtos) {
                if (dto.getStartDate() != null && dto.getEndDate() != null) {
                    if (dto.getEndDate().isBefore(dto.getStartDate())) {
                        Notification.show("La data di fine non può essere precedente alla data di inizio", 
                            3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return;
                    }
                }
            }
            
            binder.writeBean(user);
            
            // Pulisce i vecchi profili
            user.getUserProfiles().clear();
            
            // Aggiungi i nuovi profili
            for (UserProfileDto dto : validDtos) {
                UserProfile userProfile = new UserProfile();
                userProfile.setUser(user);
                userProfile.setProfile(dto.getProfile());
                userProfile.setStartDate(dto.getStartDate());
                userProfile.setEndDate(dto.getEndDate());
                user.getUserProfiles().add(userProfile);
            }

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
    
    private void updateAvailableProfilesForComboBox(ComboBox<Profile> comboBox, UserProfileDto currentItem) {
        // Ottieni i profili già selezionati (escluso quello corrente in editing)
        List<Profile> selectedProfiles = userProfileDtos.stream()
            .filter(dto -> dto != currentItem)
            .filter(dto -> dto.getProfile() != null)
            .map(UserProfileDto::getProfile)
            .toList();
        
        // Filtra i profili disponibili escludendo quelli già usati
        List<Profile> availableProfiles = new java.util.ArrayList<>(
            profileService.findActive().stream()
                .filter(p -> !selectedProfiles.contains(p))
                .toList()
        );
        
        // Aggiungi sempre il profilo correntemente selezionato se esiste
        if (currentItem.getProfile() != null && !availableProfiles.contains(currentItem.getProfile())) {
            availableProfiles.add(currentItem.getProfile());
            availableProfiles.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        }
        
        comboBox.setItems(availableProfiles);
    }
}
