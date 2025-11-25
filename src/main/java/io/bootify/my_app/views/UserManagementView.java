package io.bootify.my_app.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.bootify.my_app.domain.Profile;
import io.bootify.my_app.domain.User;
import io.bootify.my_app.service.PermissionService;
import io.bootify.my_app.service.ProfileService;
import io.bootify.my_app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "users", layout = MainLayout.class)
@PageTitle("Gestione Utenti")
public class UserManagementView extends VerticalLayout {

    private final UserService userService;
    private final ProfileService profileService;
    private final PermissionService permissionService;

    private Grid<User> userGrid;
    private Grid<Profile> profileGrid;
    private TextField userSearchField;
    private TextField profileSearchField;
    private Div userStatsDiv;
    private Div profileStatsDiv;
    private Tabs tabs;
    private VerticalLayout usersContent;
    private VerticalLayout profilesContent;
    private VerticalLayout headerLayout;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    public UserManagementView(UserService userService, ProfileService profileService,
            PermissionService permissionService) {
        this.userService = userService;
        this.profileService = profileService;
        this.permissionService = permissionService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        createHeader();
        createTabs();
        createUsersContent();
        createProfilesContent();

        // Show users tab by default
        showTab(0);

        refreshUserGrid();
        refreshProfileGrid();
    }

    private void createHeader() {
        H1 title = new H1("Gestione Utenti e Profili");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        Span subtitle = new Span("Gestisci utenti, profili e permessi del sistema");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-m)");

        headerLayout = new VerticalLayout(title, subtitle);
        headerLayout.setPadding(false);
        headerLayout.setSpacing(false);

        add(headerLayout);
    }

    private void createTabs() {
        Tab usersTab = new Tab(new Icon(VaadinIcon.USERS), new Span("Utenti"));
        Tab profilesTab = new Tab(new Icon(VaadinIcon.USER_CARD), new Span("Profili"));

        tabs = new Tabs(usersTab, profilesTab);
        tabs.setWidthFull();
        tabs.addSelectedChangeListener(event -> {
            int selectedIndex = tabs.getSelectedIndex();
            showTab(selectedIndex);
        });

        add(tabs);
    }

    private void createUsersContent() {
        usersContent = new VerticalLayout();
        usersContent.setSizeFull();
        usersContent.setPadding(false);
        usersContent.setSpacing(true);

        // Stats
        userStatsDiv = new Div();
        updateUserStats();

        // Search and actions
        userSearchField = new TextField();
        userSearchField.setPlaceholder("Cerca utente per nome, email, username...");
        userSearchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        userSearchField.setWidth("350px");
        userSearchField.setValueChangeMode(ValueChangeMode.LAZY);
        userSearchField.addValueChangeListener(e -> filterUsers(e.getValue()));
        userSearchField.setClearButtonVisible(true);

        Button addUserButton = new Button("Nuovo Utente", new Icon(VaadinIcon.PLUS));
        addUserButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addUserButton.addClickListener(e -> openUserDialog(null));

        Button refreshUsersButton = new Button("Aggiorna", new Icon(VaadinIcon.REFRESH));
        refreshUsersButton.addClickListener(e -> refreshUserGrid());

        HorizontalLayout userToolbar = new HorizontalLayout(userSearchField, addUserButton, refreshUsersButton);
        userToolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        userToolbar.setWidthFull();
        userToolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        userToolbar.getStyle().set("flex-wrap", "wrap");

        // Grid
        userGrid = new Grid<>(User.class, false);
        configureUserGrid();

        usersContent.add(userStatsDiv, userToolbar, userGrid);
        usersContent.setFlexGrow(1, userGrid);
    }

    private void configureUserGrid() {
        userGrid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_COMPACT,
                GridVariant.LUMO_WRAP_CELL_CONTENT);
        userGrid.setSizeFull();

        // Status indicator
        userGrid.addColumn(new ComponentRenderer<>(user -> {
            Icon icon = new Icon(user.getActive() ? VaadinIcon.CHECK_CIRCLE : VaadinIcon.CLOSE_CIRCLE);
            icon.setSize("20px");
            icon.setColor(user.getActive() ? "var(--lumo-success-color)" : "var(--lumo-error-color)");
            icon.setTooltipText(user.getActive() ? "Attivo" : "Disattivato");
            return icon;
        }))
                .setHeader("")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // Full name with avatar
        userGrid.addColumn(new ComponentRenderer<>(user -> {
            Div avatar = new Div();
            avatar.getStyle()
                    .set("width", "32px")
                    .set("height", "32px")
                    .set("border-radius", "50%")
                    .set("background", "var(--lumo-primary-color)")
                    .set("color", "var(--lumo-primary-contrast-color)")
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("justify-content", "center")
                    .set("font-weight", "bold")
                    .set("font-size", "14px");

            String initials = (user.getFirstName().substring(0, 1) + user.getLastName().substring(0, 1)).toUpperCase();
            avatar.setText(initials);

            Span name = new Span(user.getFullName());
            name.getStyle().set("font-weight", "500");

            Span username = new Span("@" + user.getUsername());
            username.getStyle()
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("display", "block");

            VerticalLayout nameLayout = new VerticalLayout(name, username);
            nameLayout.setPadding(false);
            nameLayout.setSpacing(false);

            HorizontalLayout layout = new HorizontalLayout(avatar, nameLayout);
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            layout.setSpacing(true);
            return layout;
        }))
                .setHeader("Utente")
                .setAutoWidth(true)
                .setFlexGrow(2);

        // Email
        userGrid.addColumn(new ComponentRenderer<>(user -> {
            Icon icon = new Icon(VaadinIcon.ENVELOPE);
            icon.setSize("16px");
            icon.getStyle().set("margin-right", "var(--lumo-space-xs)");

            Span email = new Span(user.getEmail());

            HorizontalLayout layout = new HorizontalLayout(icon, email);
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            layout.setSpacing(false);
            return layout;
        }))
                .setHeader("Email")
                .setAutoWidth(true)
                .setFlexGrow(2);

        // Department
        userGrid.addColumn(new ComponentRenderer<>(user -> {
            if (user.getDepartment() != null && !user.getDepartment().isEmpty()) {
                return new Span(user.getDepartment());
            }
            Span empty = new Span("—");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)");
            return empty;
        }))
                .setHeader("Dipartimento")
                .setAutoWidth(true);

        // Profile with badge
        userGrid.addColumn(new ComponentRenderer<>(user -> {
            if (user.getProfile() != null) {
                Span badge = new Span(user.getProfile().getName());
                badge.getElement().getThemeList().add("badge");
                badge.getStyle()
                        .set("background", "var(--lumo-primary-color-10pct)")
                        .set("color", "var(--lumo-primary-text-color)")
                        .set("padding", "var(--lumo-space-xs) var(--lumo-space-s)")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("font-size", "var(--lumo-font-size-s)")
                        .set("font-weight", "500");
                return badge;
            }
            Span noprofile = new Span("Nessun profilo");
            noprofile.getStyle()
                    .set("color", "var(--lumo-error-text-color)")
                    .set("font-size", "var(--lumo-font-size-s)");
            return noprofile;
        }))
                .setHeader("Profilo")
                .setAutoWidth(true);

        // Date created
        userGrid.addColumn(user -> user.getDateCreated() != null ? user.getDateCreated().format(DATE_FORMATTER) : "—")
                .setHeader("Data Creazione")
                .setAutoWidth(true)
                .setSortable(true);

        // Actions
        userGrid.addColumn(new ComponentRenderer<>(this::createUserActionsLayout))
                .setHeader("Azioni")
                .setAutoWidth(true)
                .setFlexGrow(0);
    }

    private Component createUserActionsLayout(User user) {
        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editButton.setTooltipText("Modifica");
        editButton.addClickListener(e -> openUserDialog(user));

        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteButton.setTooltipText("Elimina");
        deleteButton.addClickListener(e -> confirmDeleteUser(user));

        HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
        actions.setSpacing(false);
        return actions;
    }

    private void createProfilesContent() {
        profilesContent = new VerticalLayout();
        profilesContent.setSizeFull();
        profilesContent.setPadding(false);
        profilesContent.setSpacing(true);

        // Stats
        profileStatsDiv = new Div();
        updateProfileStats();

        // Search and actions
        profileSearchField = new TextField();
        profileSearchField.setPlaceholder("Cerca profilo...");
        profileSearchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        profileSearchField.setWidth("300px");
        profileSearchField.setValueChangeMode(ValueChangeMode.LAZY);
        profileSearchField.addValueChangeListener(e -> filterProfiles(e.getValue()));
        profileSearchField.setClearButtonVisible(true);

        Button addProfileButton = new Button("Nuovo Profilo", new Icon(VaadinIcon.PLUS));
        addProfileButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addProfileButton.addClickListener(e -> openProfileDialog(null));

        Button refreshProfilesButton = new Button("Aggiorna", new Icon(VaadinIcon.REFRESH));
        refreshProfilesButton.addClickListener(e -> refreshProfileGrid());

        HorizontalLayout profileToolbar = new HorizontalLayout(profileSearchField, addProfileButton,
                refreshProfilesButton);
        profileToolbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        profileToolbar.setWidthFull();
        profileToolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        profileToolbar.getStyle().set("flex-wrap", "wrap");

        // Grid
        profileGrid = new Grid<>(Profile.class, false);
        configureProfileGrid();

        profilesContent.add(profileStatsDiv, profileToolbar, profileGrid);
        profilesContent.setFlexGrow(1, profileGrid);
    }

    private void configureProfileGrid() {
        profileGrid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_COMPACT,
                GridVariant.LUMO_WRAP_CELL_CONTENT);
        profileGrid.setSizeFull();

        // Status
        profileGrid.addColumn(new ComponentRenderer<>(profile -> {
            Icon icon = new Icon(profile.getActive() ? VaadinIcon.CHECK_CIRCLE : VaadinIcon.CLOSE_CIRCLE);
            icon.setSize("20px");
            icon.setColor(profile.getActive() ? "var(--lumo-success-color)" : "var(--lumo-error-color)");
            icon.setTooltipText(profile.getActive() ? "Attivo" : "Disattivato");
            return icon;
        }))
                .setHeader("")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // Name
        profileGrid.addColumn(new ComponentRenderer<>(profile -> {
            Icon icon = new Icon(VaadinIcon.USER_CARD);
            icon.getStyle().set("margin-right", "var(--lumo-space-s)");
            icon.setColor("var(--lumo-primary-color)");

            Span name = new Span(profile.getName());
            name.getStyle().set("font-weight", "500");

            HorizontalLayout layout = new HorizontalLayout(icon, name);
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            layout.setSpacing(false);
            return layout;
        }))
                .setHeader("Nome Profilo")
                .setAutoWidth(true)
                .setFlexGrow(2);

        // Description
        profileGrid.addColumn(new ComponentRenderer<>(profile -> {
            if (profile.getDescription() != null && !profile.getDescription().isEmpty()) {
                Span desc = new Span(profile.getDescription());
                desc.getStyle()
                        .set("font-size", "var(--lumo-font-size-s)")
                        .set("color", "var(--lumo-secondary-text-color)");
                return desc;
            }
            Span empty = new Span("—");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)");
            return empty;
        }))
                .setHeader("Descrizione")
                .setAutoWidth(true)
                .setFlexGrow(3);

        // Permission count
        profileGrid.addColumn(new ComponentRenderer<>(profile -> {
            int count = profile.getPermissionCount();
            Icon icon = new Icon(VaadinIcon.KEY);
            icon.setSize("16px");
            icon.getStyle().set("margin-right", "var(--lumo-space-xs)");

            Span countSpan = new Span(count + " permess" + (count != 1 ? "i" : "o"));
            countSpan.getStyle().set("font-weight", "500");

            HorizontalLayout layout = new HorizontalLayout(icon, countSpan);
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            layout.setSpacing(false);
            return layout;
        }))
                .setHeader("Permessi")
                .setAutoWidth(true);

        // User count
        profileGrid.addColumn(new ComponentRenderer<>(profile -> {
            int count = profile.getUserCount();
            Icon icon = new Icon(VaadinIcon.USERS);
            icon.setSize("16px");
            icon.getStyle().set("margin-right", "var(--lumo-space-xs)");

            Span countSpan = new Span(count + " utent" + (count != 1 ? "i" : "e"));

            HorizontalLayout layout = new HorizontalLayout(icon, countSpan);
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            layout.setSpacing(false);
            return layout;
        }))
                .setHeader("Utenti")
                .setAutoWidth(true);

        // Date created
        profileGrid
                .addColumn(profile -> profile.getDateCreated() != null ? profile.getDateCreated().format(DATE_FORMATTER)
                        : "—")
                .setHeader("Data Creazione")
                .setAutoWidth(true)
                .setSortable(true);

        // Actions
        profileGrid.addColumn(new ComponentRenderer<>(this::createProfileActionsLayout))
                .setHeader("Azioni")
                .setAutoWidth(true)
                .setFlexGrow(0);
    }

    private Component createProfileActionsLayout(Profile profile) {
        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editButton.setTooltipText("Modifica");
        editButton.addClickListener(e -> openProfileDialog(profile));

        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteButton.setTooltipText("Elimina");
        deleteButton.addClickListener(e -> confirmDeleteProfile(profile));

        HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
        actions.setSpacing(false);
        return actions;
    }

    private void showTab(int index) {
        removeAll();
        add(headerLayout);
        add(tabs);

        if (index == 0) {
            add(usersContent);
            setFlexGrow(1, usersContent);
        } else {
            add(profilesContent);
            setFlexGrow(1, profilesContent);
        }
    }

    private void openUserDialog(User user) {
        UserFormDialog dialog = new UserFormDialog(user, userService, profileService, savedUser -> {
            refreshUserGrid();
        });
        dialog.open();
    }

    private void openProfileDialog(Profile profile) {
        ProfileFormDialog dialog = new ProfileFormDialog(profile, profileService, permissionService, savedProfile -> {
            refreshProfileGrid();
        });
        dialog.open();
    }

    private void confirmDeleteUser(User user) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Conferma eliminazione");
        dialog.setText("Sei sicuro di voler eliminare l'utente \"" + user.getFullName()
                + "\"? Questa operazione non può essere annullata.");

        dialog.setCancelable(true);
        dialog.setCancelText("Annulla");

        dialog.setConfirmText("Elimina");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> deleteUser(user));

        dialog.open();
    }

    private void deleteUser(User user) {
        try {
            userService.delete(user.getId());
            refreshUserGrid();
            Notification.show("Utente eliminato: " + user.getFullName(), 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Errore durante l'eliminazione: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmDeleteProfile(Profile profile) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Conferma eliminazione");

        int userCount = profile.getUserCount();
        if (userCount > 0) {
            dialog.setText("ATTENZIONE: Il profilo \"" + profile.getName() + "\" è assegnato a " +
                    userCount + " utent" + (userCount != 1 ? "i" : "e") + ". " +
                    "Eliminando questo profilo, gli utenti perderanno il loro profilo. Continuare?");
        } else {
            dialog.setText("Sei sicuro di voler eliminare il profilo \"" + profile.getName()
                    + "\"? Questa operazione non può essere annullata.");
        }

        dialog.setCancelable(true);
        dialog.setCancelText("Annulla");

        dialog.setConfirmText("Elimina");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> deleteProfile(profile));

        dialog.open();
    }

    private void deleteProfile(Profile profile) {
        try {
            profileService.delete(profile.getId());
            refreshProfileGrid();
            Notification.show("Profilo eliminato: " + profile.getName(), 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Errore durante l'eliminazione: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void filterUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            refreshUserGrid();
        } else {
            List<User> users = userService.search(searchTerm);
            userGrid.setItems(users);
        }
    }

    private void filterProfiles(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            refreshProfileGrid();
        } else {
            List<Profile> profiles = profileService.search(searchTerm);
            profileGrid.setItems(profiles);
        }
    }

    private void refreshUserGrid() {
        List<User> users = userService.findAll();
        userGrid.setItems(users);
        updateUserStats();
        userSearchField.clear();
    }

    private void refreshProfileGrid() {
        List<Profile> profiles = profileService.findAll();
        profileGrid.setItems(profiles);
        updateProfileStats();
        profileSearchField.clear();
    }

    private void updateUserStats() {
        long total = userService.count();
        long active = userService.countActive();

        userStatsDiv.removeAll();

        Span stats = new Span(total + " utent" + (total != 1 ? "i totali" : "e totale") +
                " • " + active + " attiv" + (active != 1 ? "i" : "o"));
        stats.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-m)")
                .set("font-weight", "500");

        userStatsDiv.add(stats);
    }

    private void updateProfileStats() {
        long count = profileService.count();

        profileStatsDiv.removeAll();

        Span stats = new Span(count + " profil" + (count != 1 ? "i totali" : "o totale"));
        stats.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-m)")
                .set("font-weight", "500");

        profileStatsDiv.add(stats);
    }
}
