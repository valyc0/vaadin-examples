package io.bootify.my_app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.bootify.my_app.model.TreeResponse;

import java.util.List;

@Route(value = "ricerca-file", layout = MainLayout.class)
@PageTitle("Ricerca Documenti")
public class SearchView extends VerticalLayout {

    // ---------------- FILTRI ----------------
    private final TextField nomeFile = new TextField("Nome File");
    private final ComboBox<String> tipologia = new ComboBox<>("Tipologia");
    private final DatePicker dataDa = new DatePicker("Data Da");
    private final DatePicker dataA = new DatePicker("Data A");
    private final TextField metaKey = new TextField("Metadato chiave");
    private final TextField metaValue = new TextField("Metadato valore");

    private final TreeGrid<TreeResponse> tree = new TreeGrid<>();

    private final Details generalFiltersDetails = new Details();
    private final Details metadataFiltersDetails = new Details();
    private final Details treeDetails = new Details();

    private final Grid<FileResultDTO> resultsGrid = new Grid<>(FileResultDTO.class, false);

    private final FlexLayout filtersSummary = new FlexLayout();
    private final Button modifyFiltersButton = new Button("Modifica Filtri", new Icon(VaadinIcon.EDIT));
    private final Button filterButton = new Button("Cerca Documenti", new Icon(VaadinIcon.SEARCH));
    private final Button resetButton = new Button("Pulisci Filtri", new Icon(VaadinIcon.ERASER));

    private TreeResponse selectedTreeItem;

    public SearchView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);

        add(new H3("Ricerca Documenti"));

        buildFiltersSection();

        // Pulsanti azione
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        filterButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        filterButton.addClickListener(e -> performSearch());
        
        resetButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        resetButton.addClickListener(e -> resetFilters());
        
        buttonLayout.add(filterButton, resetButton);
        add(buttonLayout);

        // Riepilogo filtri
        filtersSummary.setWidthFull();
        filtersSummary.getStyle()
            .set("padding", "12px")
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("flex-wrap", "wrap")
            .set("gap", "8px");
        filtersSummary.setVisible(false);

        modifyFiltersButton.addClickListener(e -> showFilters());
        modifyFiltersButton.setVisible(false);

        buildResultsGrid();

        add(filtersSummary, modifyFiltersButton, resultsGrid);
    }

    private void buildFiltersSection() {
        // Configurazione campi con placeholder e clear button
        nomeFile.setPlaceholder("Inserisci nome file o parte di esso...");
        nomeFile.setClearButtonVisible(true);
        nomeFile.setPrefixComponent(new Icon(VaadinIcon.FILE_TEXT));
        
        tipologia.setItems("CLASSIFICATO", "NON CLASSIFICATO");
        tipologia.setPlaceholder("Seleziona tipologia...");
        tipologia.setClearButtonVisible(true);
        
        dataDa.setPlaceholder("gg/mm/aaaa");
        dataDa.setClearButtonVisible(true);
        dataDa.setLocale(java.util.Locale.ITALY);
        dataDa.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                dataA.setMin(e.getValue());
            } else {
                dataA.setMin(null);
            }
            validateDateRange();
        });
        
        dataA.setPlaceholder("gg/mm/aaaa");
        dataA.setClearButtonVisible(true);
        dataA.setLocale(java.util.Locale.ITALY);
        dataA.addValueChangeListener(e -> validateDateRange());
        
        metaKey.setPlaceholder("es: autore, categoria...");
        metaKey.setClearButtonVisible(true);
        metaKey.setPrefixComponent(new Icon(VaadinIcon.KEY));
        
        metaValue.setPlaceholder("Valore del metadato...");
        metaValue.setClearButtonVisible(true);
        metaValue.setPrefixComponent(new Icon(VaadinIcon.INPUT));

        // Layout responsive a coppie
        FlexLayout generalRow1 = new FlexLayout(nomeFile, tipologia);
        generalRow1.setWidthFull();
        generalRow1.getStyle().set("gap", "1rem").set("flex-wrap", "wrap");
        nomeFile.setWidth("calc(50% - 0.5rem)");
        nomeFile.getStyle().set("min-width", "200px").set("flex", "1");
        tipologia.setWidth("calc(50% - 0.5rem)");
        tipologia.getStyle().set("min-width", "200px").set("flex", "1");

        FlexLayout generalRow2 = new FlexLayout(dataDa, dataA);
        generalRow2.setWidthFull();
        generalRow2.getStyle().set("gap", "1rem").set("flex-wrap", "wrap");
        dataDa.setWidth("calc(50% - 0.5rem)");
        dataDa.getStyle().set("min-width", "200px").set("flex", "1");
        dataA.setWidth("calc(50% - 0.5rem)");
        dataA.getStyle().set("min-width", "200px").set("flex", "1");

        FlexLayout metaRow = new FlexLayout(metaKey, metaValue);
        metaRow.setWidthFull();
        metaRow.getStyle().set("gap", "1rem").set("flex-wrap", "wrap");
        metaKey.setWidth("calc(50% - 0.5rem)");
        metaKey.getStyle().set("min-width", "200px").set("flex", "1");
        metaValue.setWidth("calc(50% - 0.5rem)");
        metaValue.getStyle().set("min-width", "200px").set("flex", "1");

        VerticalLayout generalLayout = new VerticalLayout(generalRow1, generalRow2);
        generalLayout.setPadding(true);
        generalFiltersDetails.setSummaryText("Filtri Generali");
        generalFiltersDetails.setContent(generalLayout);
        generalFiltersDetails.setOpened(true);

        VerticalLayout metaLayout = new VerticalLayout(metaRow);
        metaLayout.setPadding(true);
        metadataFiltersDetails.setSummaryText("Filtri Metadati");
        metadataFiltersDetails.setContent(metaLayout);
        metadataFiltersDetails.setOpened(false);

        tree.addHierarchyColumn(TreeResponse::getDescrizione)
            .setHeader("Elemento")
            .setFlexGrow(3)
            .setWidth("400px");
        tree.addColumn(TreeResponse::getCode)
            .setHeader("Codice")
            .setWidth("150px");
        tree.addColumn(TreeResponse::getType)
            .setHeader("Tipo")
            .setWidth("200px");
        tree.setItems(generateTreeExample(), TreeResponse::getChildren);
        tree.setWidth("100%");
        tree.getStyle().set("min-width", "800px");
        tree.setHeight("350px");
        tree.getStyle().set("font-size", "var(--lumo-font-size-m)");
        tree.addSelectionListener(event -> {
            selectedTreeItem = event.getFirstSelectedItem().orElse(null);
            if (selectedTreeItem != null) {
                Notification.show(
                    "Selezionato: " + selectedTreeItem.getDescrizione(),
                    2000,
                    Notification.Position.BOTTOM_CENTER
                );
            }
        });

        treeDetails.setSummaryText("Filtro Strutturato");
        treeDetails.setContent(tree);
        treeDetails.setOpened(false);

        add(generalFiltersDetails, metadataFiltersDetails, treeDetails);
    }

    private void buildResultsGrid() {
        resultsGrid.addColumn(FileResultDTO::getFileName)
            .setHeader("Nome File")
            .setFlexGrow(2)
            .setSortable(true);
        resultsGrid.addColumn(FileResultDTO::getDescription)
            .setHeader("Descrizione")
            .setFlexGrow(3);
        resultsGrid.addColumn(FileResultDTO::getUploadDate)
            .setHeader("Data Upload")
            .setWidth("130px")
            .setSortable(true);
        resultsGrid.addColumn(FileResultDTO::getClassification)
            .setHeader("Tipologia")
            .setWidth("150px");
        resultsGrid.addColumn(FileResultDTO::getSize)
            .setHeader("Dimensione")
            .setWidth("120px");
        
        resultsGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        resultsGrid.setWidthFull();
        resultsGrid.setHeight("600px");
        resultsGrid.setVisible(false);
        resultsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        resultsGrid.addSelectionListener(event -> {
            event.getFirstSelectedItem().ifPresent(file -> {
                Notification.show(
                    "Selezionato: " + file.getFileName(),
                    2000,
                    Notification.Position.BOTTOM_END
                );
            });
        });
    }

    private void performSearch() {
        if (!validateDateRange()) {
            return;
        }
        
        // Mostra riepilogo filtri come badge
        filtersSummary.removeAll();
        addFilterBadge("Nome", nomeFile.getValue());
        addFilterBadge("Tipologia", tipologia.getValue());
        if (dataDa.getValue() != null) {
            addFilterBadge("Data Da", dataDa.getValue().toString());
        }
        if (dataA.getValue() != null) {
            addFilterBadge("Data A", dataA.getValue().toString());
        }
        if (metaKey.getValue() != null && !metaKey.getValue().isEmpty() && 
            metaValue.getValue() != null && !metaValue.getValue().isEmpty()) {
            addFilterBadge(metaKey.getValue(), metaValue.getValue());
        }
        if (selectedTreeItem != null) {
            addFilterBadge("Struttura", selectedTreeItem.getDescrizione());
        }
        
        boolean hasFilters = filtersSummary.getChildren().count() > 0;
        filtersSummary.setVisible(hasFilters);

        modifyFiltersButton.setVisible(true);

        // Nascondi filtri
        generalFiltersDetails.setVisible(false);
        metadataFiltersDetails.setVisible(false);
        treeDetails.setVisible(false);
        filterButton.setVisible(false);
        resetButton.setVisible(false);

        // Mostra grid con loading
        resultsGrid.setVisible(true);

        // Simula ricerca con feedback
        Notification.show("Ricerca in corso...", 1000, Notification.Position.BOTTOM_CENTER);
        
        // MOCK risultati
        resultsGrid.setItems(List.of(
                new FileResultDTO("report_sicurezza.pdf","Analisi difesa 2025","2024-03-10","CLASSIFICATO","1.2 MB"),
                new FileResultDTO("cyber-threats.docx","Minacce attuali","2024-02-01","NON CLASSIFICATO","530 KB"),
                new FileResultDTO("innovazione2024.pdf","Progetto nuove tecnologie","2024-01-15","CLASSIFICATO","2.5 MB"),
                new FileResultDTO("budget-2025.xlsx","Piano finanziario annuale","2024-02-15","CLASSIFICATO","850 KB"),
                new FileResultDTO("meeting-notes.docx","Verbale riunione","2024-03-01","NON CLASSIFICATO","120 KB")
        ));
        
        Notification notification = Notification.show(
            "Trovati " + resultsGrid.getListDataView().getItemCount() + " documenti",
            3000,
            Notification.Position.BOTTOM_END
        );
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showFilters() {
        generalFiltersDetails.setVisible(true);
        metadataFiltersDetails.setVisible(true);
        treeDetails.setVisible(true);
        filtersSummary.setVisible(false);
        modifyFiltersButton.setVisible(false);
        filterButton.setVisible(true);
        resetButton.setVisible(true);
        resultsGrid.setVisible(false);
    }
    
    private void resetFilters() {
        nomeFile.clear();
        tipologia.clear();
        dataDa.clear();
        dataA.clear();
        metaKey.clear();
        metaValue.clear();
        selectedTreeItem = null;
        tree.deselectAll();
        
        Notification.show("Filtri azzerati", 2000, Notification.Position.BOTTOM_CENTER);
    }
    
    private boolean validateDateRange() {
        if (dataDa.getValue() != null && dataA.getValue() != null) {
            if (dataDa.getValue().isAfter(dataA.getValue())) {
                Notification notification = Notification.show(
                    "La data di inizio non può essere successiva alla data di fine",
                    4000,
                    Notification.Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                dataDa.setInvalid(true);
                dataA.setInvalid(true);
                return false;
            }
        }
        dataDa.setInvalid(false);
        dataA.setInvalid(false);
        return true;
    }
    
    private void addFilterBadge(String label, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        Span badge = new Span(label + ": " + value);
        badge.getElement().getThemeList().add("badge");
        badge.getStyle()
            .set("background-color", "var(--lumo-primary-color-10pct)")
            .set("color", "var(--lumo-primary-text-color)")
            .set("padding", "4px 12px")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("font-size", "var(--lumo-font-size-s)")
            .set("font-weight", "500");
        filtersSummary.add(badge);
    }

    private String StreamNonNull(String label, String value) {
        return (value != null && !value.isEmpty()) ? label + "=" + value + ", " : "";
    }

    private String StreamNonNull(String label, String key, String val) {
        return (key != null && !key.isEmpty() && val != null && !val.isEmpty()) ? label + "=" + key + ":" + val + ", " : "";
    }

    private List<TreeResponse> generateTreeExample() {
        TreeResponse comp1 = new TreeResponse("CT01", "Complesso", "Sicurezza Nazionale");
        TreeResponse area1 = new TreeResponse("AR01", "Area", "Intelligence");
        TreeResponse tr1 = new TreeResponse("TR01", "Trattazione", "Analisi Strategica");
        area1.getChildren().add(tr1);
        comp1.getChildren().add(area1);

        TreeResponse comp2 = new TreeResponse("CT02", "Complesso", "Innovazione Tecnologica");
        TreeResponse area2 = new TreeResponse("AR02", "Area", "Cybersecurity");
        TreeResponse tr2 = new TreeResponse("TR02", "Trattazione", "Minacce Informatiche");
        area2.getChildren().add(tr2);
        comp2.getChildren().add(area2);

        return List.of(comp1, comp2);
    }

    public static class FileResultDTO {
        private final String fileName;
        private final String description;
        private final String uploadDate;
        private final String classification;
        private final String size;

        public FileResultDTO(String fileName, String description, String uploadDate, String classification, String size) {
            this.fileName = fileName;
            this.description = description;
            this.uploadDate = uploadDate;
            this.classification = classification;
            this.size = size;
        }

        public String getFileName() { return fileName; }
        public String getDescription() { return description; }
        public String getUploadDate() { return uploadDate; }
        public String getClassification() { return classification; }
        public String getSize() { return size; }
    }
}