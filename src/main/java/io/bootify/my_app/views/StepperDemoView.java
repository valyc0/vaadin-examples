package io.bootify.my_app.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "stepper-demo", layout = MainLayout.class)
@PageTitle("Stepper Demo")
public class StepperDemoView extends VerticalLayout {

    private static final String[] TITLES    = {"Caricato",    "Classificato",   "Descritto",   "Tags aggiunti", "Metadati custom"};
    private static final String[] SUBTITLES = {"Nel sistema", "Tipo e categoria", "Descrizione", "Tag ricerca",   "Metadati custom"};

    public StepperDemoView() {
        setPadding(true);
        setSpacing(true);
        setMaxWidth("900px");

        add(new H2("Demo Stepper – stati di avanzamento"));

        addScenario("Step 1 – solo caricato",
                new boolean[]{true, false, false, false, false});

        addScenario("Step 2 – caricato + classificato",
                new boolean[]{true, true, false, false, false});

        addScenario("Step 3 – arrivato a Descritto (corrente)",
                new boolean[]{true, true, false, false, false});
        // ↑ questo è il caso che hai chiesto: done[0,1]=true, done[2]=false → currentStep=2

        addScenario("Step 4 – descritto, mancano tags e metadati",
                new boolean[]{true, true, true, false, false});

        addScenario("Step 5 – tutti completati",
                new boolean[]{true, true, true, true, true});
    }

    private void addScenario(String label, boolean[] done) {
        H4 title = new H4(label);
        title.getStyle().set("margin-bottom", "4px").set("margin-top", "var(--lumo-space-l)");

        Div stepper = buildStepper(done);
        stepper.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px")
                .set("padding", "var(--lumo-space-m)");

        add(title, stepper);
    }

    private Div buildStepper(boolean[] done) {
        int currentStep = done.length;
        for (int i = 0; i < done.length; i++) {
            if (!done[i]) { currentStep = i; break; }
        }

        Div topRow = new Div();
        topRow.getStyle()
                .set("display", "flex")
                .set("flex-direction", "row")
                .set("align-items", "center")
                .set("width", "100%");

        Div bottomRow = new Div();
        bottomRow.getStyle()
                .set("display", "flex")
                .set("flex-direction", "row")
                .set("align-items", "flex-start")
                .set("width", "100%")
                .set("margin-top", "6px");

        for (int i = 0; i < TITLES.length; i++) {
            boolean isDone    = done[i];
            boolean isCurrent = (i == currentStep);
            boolean isLast    = (i == TITLES.length - 1);

            Div circle = new Div();
            circle.getStyle()
                    .set("width", "28px")
                    .set("height", "28px")
                    .set("border-radius", "50%")
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("justify-content", "center")
                    .set("flex-shrink", "0");

            if (isDone) {
                circle.getStyle()
                        .set("background-color", "var(--lumo-success-color)")
                        .set("color", "white");
                Icon checkIcon = VaadinIcon.CHECK.create();
                checkIcon.setSize("13px");
                circle.add(checkIcon);
            } else if (isCurrent) {
                circle.getStyle()
                        .set("background-color", "var(--lumo-primary-color)")
                        .set("color", "white");
                Span num = new Span(String.valueOf(i + 1));
                num.getStyle().set("font-size", "12px").set("font-weight", "700");
                circle.add(num);
            } else {
                circle.getStyle()
                        .set("background-color", "var(--lumo-contrast-10pct)")
                        .set("color", "var(--lumo-secondary-text-color)")
                        .set("border", "1px solid var(--lumo-contrast-20pct)");
                Span num = new Span(String.valueOf(i + 1));
                num.getStyle().set("font-size", "12px");
                circle.add(num);
            }

            Div stepCell = new Div();
            stepCell.getStyle()
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("align-items", "center")
                    .set("flex", isLast ? "0 0 auto" : "1 1 0%");

            if (!isLast) {
                Div circleAndLine = new Div();
                circleAndLine.getStyle()
                        .set("display", "flex")
                        .set("flex-direction", "row")
                        .set("align-items", "center")
                        .set("width", "100%");
                Div line = new Div();
                line.getStyle()
                        .set("flex", "1")
                        .set("height", "2px")
                        .set("background-color", isDone
                                ? "var(--lumo-success-color)"
                                : "var(--lumo-contrast-20pct)");
                circleAndLine.add(circle, line);
                stepCell.add(circleAndLine);
            } else {
                stepCell.add(circle);
            }
            topRow.add(stepCell);

            Div labelCell = new Div();
            labelCell.getStyle()
                    .set("flex", isLast ? "0 0 auto" : "1 1 0%")
                    .set("text-align", "center")
                    .set("padding-right", isLast ? "0" : "4px");

            Span titleSpan = new Span(TITLES[i]);
            titleSpan.getStyle()
                    .set("display", "block")
                    .set("font-size", "var(--lumo-font-size-xs)")
                    .set("font-weight", isDone || isCurrent ? "600" : "400")
                    .set("color", isDone
                            ? "var(--lumo-success-text-color)"
                            : isCurrent
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-secondary-text-color)");

            Span subtitleSpan = new Span(SUBTITLES[i]);
            subtitleSpan.getStyle()
                    .set("display", "block")
                    .set("font-size", "10px")
                    .set("color", "var(--lumo-secondary-text-color)");

            labelCell.add(titleSpan, subtitleSpan);
            bottomRow.add(labelCell);
        }

        return new Div(topRow, bottomRow);
    }
}
