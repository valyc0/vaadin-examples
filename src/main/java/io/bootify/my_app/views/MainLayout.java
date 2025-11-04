package io.bootify.my_app.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
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
            LumoUtility.Margin.MEDIUM
        );

        DrawerToggle toggle = new DrawerToggle();

        HorizontalLayout header = new HorizontalLayout(toggle, logo);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames(
            LumoUtility.Padding.Vertical.NONE,
            LumoUtility.Padding.Horizontal.MEDIUM
        );

        addToNavbar(header);
    }

    private void createDrawer() {
        RouterLink homeLink = new RouterLink("Home", HomeView.class);
        RouterLink dashboardLink = new RouterLink("Dashboard", DashboardView.class);
        RouterLink graphLink = new RouterLink("Grafo Prodotti", GraphView.class);
        RouterLink prodottiLink = new RouterLink("Gestione Prodotti", ProdottoManagementView.class);
        RouterLink filesLink = new RouterLink("Gestione File Avanzata", EnhancedFileManagementView.class);
        RouterLink fileDetailLink = new RouterLink("Dettaglio File", FileDetailView.class);
        
        homeLink.addClassNames(LumoUtility.Padding.MEDIUM);
        dashboardLink.addClassNames(LumoUtility.Padding.MEDIUM);
        graphLink.addClassNames(LumoUtility.Padding.MEDIUM);
        prodottiLink.addClassNames(LumoUtility.Padding.MEDIUM);
        filesLink.addClassNames(LumoUtility.Padding.MEDIUM);
        fileDetailLink.addClassNames(LumoUtility.Padding.MEDIUM);

        VerticalLayout navigation = new VerticalLayout(homeLink, dashboardLink, graphLink, prodottiLink, filesLink, fileDetailLink);
        navigation.setPadding(true);
        navigation.setSpacing(false);

        addToDrawer(navigation);
    }
}
