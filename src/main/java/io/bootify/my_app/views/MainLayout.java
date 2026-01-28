package io.bootify.my_app.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("My Application");
        logo.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.MEDIUM);

        DrawerToggle toggle = new DrawerToggle();

        HorizontalLayout header = new HorizontalLayout(toggle, logo);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames(
                LumoUtility.Padding.Vertical.NONE,
                LumoUtility.Padding.Horizontal.MEDIUM);

        addToNavbar(header);
    }

    private void createDrawer() {
        VerticalLayout navigation = new VerticalLayout(
                createNavItem("Home", HomeView.class, VaadinIcon.HOME),
                createNavItem("Dashboard", DashboardView.class, VaadinIcon.DASHBOARD),
                createNavItem("Gestione Utenti", UserManagementView.class, VaadinIcon.USERS),
                createNavItem("Grafo Prodotti", GraphView.class, VaadinIcon.CLUSTER),
                createNavItem("Gestione Prodotti", ProdottoManagementView.class, VaadinIcon.PACKAGE),
                createNavItem("Gestione File Avanzata", EnhancedFileManagementView.class, VaadinIcon.FOLDER_OPEN),
                createNavItem("Dettaglio File", FileDetailView.class, VaadinIcon.FILE_TEXT_O),
                createNavItem("Generic Grid", GenericGridView.class, VaadinIcon.TABLE),
                createNavItem("Gestione Contenuti", ContentManagementView.class, VaadinIcon.EDIT),
                createNavItem("Ricerca Documenti", SearchView.class, VaadinIcon.SEARCH),
                createNavItem("AI Chatbot", ChatbotView.class, VaadinIcon.COMMENT_ELLIPSIS),
                createNavItem("Google Search", GoogleSearchView.class, VaadinIcon.GLOBE)
        );

        navigation.setPadding(true);
        navigation.setSpacing(false);
        navigation.getStyle()
                .set("gap", "4px");

        addToDrawer(navigation);
    }

    /**
     * Crea un item di navigazione con icona, testo e effetti hover
     *
     * @param text             Il testo da visualizzare
     * @param navigationTarget La classe della vista di destinazione
     * @param icon             L'icona Vaadin da utilizzare
     * @return RouterLink stilizzato con HorizontalLayout interno
     */
    private RouterLink createNavItem(String text,
                                     Class<? extends Component> navigationTarget,
                                     VaadinIcon icon) {
        // Crea l'icona
        Icon navIcon = icon.create();
        navIcon.getStyle()
                .set("min-width", "20px")
                .set("width", "20px")
                .set("height", "20px")
                .set("color", "var(--lumo-secondary-text-color)");

        // Crea il testo
        Span label = new Span(text);
        label.getStyle()
                .set("font-weight", "500")
                .set("font-size", "var(--lumo-font-size-s)");

        // Crea il layout contenitore
        HorizontalLayout content = new HorizontalLayout(navIcon, label);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setSpacing(false);
        content.getStyle()
                .set("gap", "12px")
                .set("pointer-events", "none"); // Per far passare il click al RouterLink

        // Crea il RouterLink come wrapper
        RouterLink link = new RouterLink();
        link.setRoute(navigationTarget);
        link.add(content);

        // Stili inline per rimuovere sottolineatura e aggiungere padding
        link.getStyle()
                .set("text-decoration", "none")
                .set("color", "var(--lumo-body-text-color)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("width", "100%")
                .set("padding", "10px 16px")
                .set("border-radius", "8px")
                .set("cursor", "pointer")
                .set("transition", "all 0.2s ease")
                .set("box-sizing", "border-box")
                .set("margin", "0");

        // Aggiungi effetti hover tramite JavaScript inline
        link.getElement().executeJs(
                "this.addEventListener('mouseenter', function() { " +
                        "   this.style.backgroundColor = 'var(--lumo-contrast-10pct)'; " +
                        "   this.style.transform = 'translateX(4px)'; " +
                        "   this.querySelector('vaadin-icon').style.color = 'var(--lumo-primary-color)'; " +
                        "});" +
                        "this.addEventListener('mouseleave', function() { " +
                        "   this.style.backgroundColor = 'transparent'; " +
                        "   this.style.transform = 'translateX(0)'; " +
                        "   this.querySelector('vaadin-icon').style.color = 'var(--lumo-secondary-text-color)'; " +
                        "});"
        );

        return link;
    }
}
