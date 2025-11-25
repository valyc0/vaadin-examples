package io.bootify.my_app.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
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

        // Profile with badges (multiple) - clickable
        userGrid.addColumn(new ComponentRenderer<>(user -> {
            if (user.getProfiles() != null && !user.getProfiles().isEmpty()) {
                HorizontalLayout badgesLayout = new HorizontalLayout();
                badgesLayout.setSpacing(true);
                badgesLayout.getStyle()
                        .set("flex-wrap", "wrap")
                        .set("gap", "var(--lumo-space-xs)");
                
                user.getProfiles().stream()
                        .sorted((p1, p2) -> p1.getName().compareTo(p2.getName()))
                        .forEach(profile -> {
                            Span badge = new Span(profile.getName());
                            badge.getElement().getThemeList().add("badge");
                            badge.getStyle()
                                    .set("background", "var(--lumo-primary-color-10pct)")
                                    .set("color", "var(--lumo-primary-text-color)")
                                    .set("padding", "var(--lumo-space-xs) var(--lumo-space-s)")
                                    .set("border-radius", "var(--lumo-border-radius-m)")
                                    .set("font-size", "var(--lumo-font-size-xs)")
                                    .set("font-weight", "500")
                                    .set("white-space", "nowrap")
                                    .set("cursor", "pointer")
                                    .set("transition", "all 0.2s");
                            
                            // Hover effect
                            badge.getElement().addEventListener("mouseenter", e -> {
                                badge.getStyle()
                                        .set("background", "var(--lumo-primary-color-50pct)")
                                        .set("transform", "scale(1.05)");
                            });
                            badge.getElement().addEventListener("mouseleave", e -> {
                                badge.getStyle()
                                        .set("background", "var(--lumo-primary-color-10pct)")
                                        .set("transform", "scale(1)");
                            });
                            
                            // Click to show permissions
                            badge.getElement().addEventListener("click", e -> {
                                openProfilePermissionsDialog(profile);
                            });
                            
                            badgesLayout.add(badge);
                        });
                
                return badgesLayout;
            }
            Span noprofile = new Span("Nessun profilo");
            noprofile.getStyle()
                    .set("color", "var(--lumo-error-text-color)")
                    .set("font-size", "var(--lumo-font-size-s)");
            return noprofile;
        }))
                .setHeader("Profili")
                .setAutoWidth(true)
                .setFlexGrow(1);

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

        // Permission count - clickable
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
            layout.getStyle()
                    .set("cursor", "pointer")
                    .set("padding", "var(--lumo-space-xs) var(--lumo-space-s)")
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("transition", "all 0.2s");
            
            // Hover effect
            layout.getElement().addEventListener("mouseenter", e -> {
                layout.getStyle().set("background", "var(--lumo-primary-color-10pct)");
            });
            layout.getElement().addEventListener("mouseleave", e -> {
                layout.getStyle().set("background", "transparent");
            });
            
            // Click to show permissions
            layout.getElement().addEventListener("click", e -> {
                openProfilePermissionsDialog(profile);
            });
            
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
        UserFormDialog dialog = new UserFormDialog(user, userService, profileService, permissionService, savedUser -> {
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

    private void openProfilePermissionsDialog(Profile profile) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Permessi: " + profile.getName());
        dialog.setWidth("600px");
        dialog.setMaxHeight("80vh");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        // Profile info
        if (profile.getDescription() != null && !profile.getDescription().isEmpty()) {
            Span description = new Span(profile.getDescription());
            description.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("display", "block")
                    .set("margin-bottom", "var(--lumo-space-m)")
                    .set("font-style", "italic");
            content.add(description);
        }

        // Load permissions
        profileService.findByIdWithPermissions(profile.getId()).ifPresent(p -> {
            if (p.getPermissions().isEmpty()) {
                Div emptyState = new Div();
                Icon warningIcon = new Icon(VaadinIcon.WARNING);
                warningIcon.setSize("48px");
                warningIcon.setColor("var(--lumo-error-color)");
                warningIcon.getStyle()
                        .set("margin", "var(--lumo-space-l) auto")
                        .set("display", "block");

                Span message = new Span("Questo profilo non ha permessi assegnati");
                message.getStyle()
                        .set("color", "var(--lumo-error-text-color)")
                        .set("text-align", "center")
                        .set("display", "block")
                        .set("font-size", "var(--lumo-font-size-l)");

                emptyState.add(warningIcon, message);
                emptyState.getStyle()
                        .set("text-align", "center")
                        .set("padding", "var(--lumo-space-xl)");
                content.add(emptyState);
            } else {
                // Permission count badge
                Div countBadge = new Div();
                Span countText = new Span(p.getPermissions().size() + " permess" + (p.getPermissions().size() == 1 ? "o" : "i"));
                countText.getStyle()
                        .set("background", "var(--lumo-success-color-10pct)")
                        .set("color", "var(--lumo-success-text-color)")
                        .set("padding", "var(--lumo-space-xs) var(--lumo-space-s)")
                        .set("border-radius", "var(--lumo-border-radius-m)")
                        .set("font-size", "var(--lumo-font-size-s)")
                        .set("font-weight", "600")
                        .set("display", "inline-block")
                        .set("margin-bottom", "var(--lumo-space-m)");
                countBadge.add(countText);
                content.add(countBadge);

                // Group permissions by category
                java.util.Map<String, java.util.List<io.bootify.my_app.domain.Permission>> permissionsByCategory = 
                    p.getPermissions().stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                            io.bootify.my_app.domain.Permission::getCategory,
                            java.util.stream.Collectors.toList()
                        ));

                permissionsByCategory.entrySet().stream()
                        .sorted(java.util.Map.Entry.comparingByKey())
                        .forEach(entry -> {
                            String category = entry.getKey();
                            java.util.List<io.bootify.my_app.domain.Permission> permissions = entry.getValue();

                            // Category header
                            HorizontalLayout categoryHeader = new HorizontalLayout();
                            categoryHeader.setAlignItems(FlexComponent.Alignment.CENTER);
                            categoryHeader.getStyle()
                                    .set("margin-top", "var(--lumo-space-m)")
                                    .set("margin-bottom", "var(--lumo-space-s)");

                            Icon categoryIcon = new Icon(VaadinIcon.FOLDER_OPEN);
                            categoryIcon.setSize("18px");
                            categoryIcon.setColor("var(--lumo-primary-color)");

                            Span categoryName = new Span(category);
                            categoryName.getStyle()
                                    .set("font-weight", "600")
                                    .set("font-size", "var(--lumo-font-size-m)")
                                    .set("color", "var(--lumo-primary-text-color)");

                            Span categoryCount = new Span("(" + permissions.size() + ")");
                            categoryCount.getStyle()
                                    .set("color", "var(--lumo-secondary-text-color)")
                                    .set("font-size", "var(--lumo-font-size-s)")
                                    .set("margin-left", "var(--lumo-space-xs)");

                            categoryHeader.add(categoryIcon, categoryName, categoryCount);
                            content.add(categoryHeader);

                            // Permissions in this category
                            VerticalLayout permissionsList = new VerticalLayout();
                            permissionsList.setPadding(false);
                            permissionsList.setSpacing(false);
                            permissionsList.getStyle()
                                    .set("margin-left", "var(--lumo-space-l)")
                                    .set("gap", "var(--lumo-space-xs)");

                            permissions.stream()
                                    .sorted((perm1, perm2) -> perm1.getName().compareTo(perm2.getName()))
                                    .forEach(permission -> {
                                        HorizontalLayout permRow = new HorizontalLayout();
                                        permRow.setAlignItems(FlexComponent.Alignment.CENTER);
                                        permRow.setSpacing(true);
                                        permRow.getStyle()
                                                .set("padding", "var(--lumo-space-xs)")
                                                .set("border-radius", "var(--lumo-border-radius-s)")
                                                .set("background", "var(--lumo-contrast-5pct)");

                                        Icon checkIcon = new Icon(VaadinIcon.CHECK_CIRCLE);
                                        checkIcon.setSize("16px");
                                        checkIcon.setColor("var(--lumo-success-color)");

                                        VerticalLayout permInfo = new VerticalLayout();
                                        permInfo.setPadding(false);
                                        permInfo.setSpacing(false);

                                        Span permName = new Span(permission.getName());
                                        permName.getStyle()
                                                .set("font-weight", "500")
                                                .set("font-size", "var(--lumo-font-size-s)");

                                        Span permDesc = new Span(permission.getDescription());
                                        permDesc.getStyle()
                                                .set("color", "var(--lumo-secondary-text-color)")
                                                .set("font-size", "var(--lumo-font-size-xs)");

                                        permInfo.add(permName, permDesc);
                                        permRow.add(checkIcon, permInfo);

                                        permissionsList.add(permRow);
                                    });

                            content.add(permissionsList);
                        });
            }
        });

        dialog.add(content);

        // Footer with close button
        Button closeButton = new Button("Chiudi", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout footer = new HorizontalLayout(closeButton);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.setWidthFull();
        footer.setPadding(true);

        dialog.getFooter().add(footer);
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
