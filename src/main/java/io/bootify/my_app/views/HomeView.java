package io.bootify.my_app.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Value;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Home")
public class HomeView extends VerticalLayout {

    public HomeView(@Value("${app.name}") final String appName) {
        setSpacing(true);
        setPadding(true);
        
        H1 title = new H1("Welcome to " + appName);
        Paragraph description = new Paragraph("This is the home page. Use the menu to navigate to the Dashboard.");
        
        add(title, description);
    }
}
