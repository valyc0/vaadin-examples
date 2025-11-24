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
import io.bootify.my_app.dto.DocumentSearchFilterDTO;
import io.bootify.my_app.model.TreeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Route(value = "ricerca-file", layout = MainLayout.class)
@PageTitle("Ricerca Documenti")
public class SearchView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(SearchView.class);

    // ---------------- FILTRI ----------------
    private final TextField nomeFile = new TextField("Nome File");
    private final ComboBox<String> tipologia = new ComboBox<>("Tipologia");
    private final DatePicker dataDa = new DatePicker("Data Da");
    private final DatePicker dataA = new DatePicker("Data A");
    private final TextField autore = new TextField("Autore/Creatore");
    private final ComboBox<String> formato = new ComboBox<>("Formato File");
    private final TextField dimensioneMin = new TextField("Dimensione Min (KB)");
    private final TextField dimensioneMax = new TextField("Dimensione Max (KB)");
    private final TextField titolo = new TextField("Titolo Documento");
    private final TextField tags = new TextField("Tag/Parole chiave");
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
        
        // Configurazione campi metadati principali
        autore.setPlaceholder("Nome autore o creatore...");
        autore.setClearButtonVisible(true);
        autore.setPrefixComponent(new Icon(VaadinIcon.USER));
        
        formato.setItems("PDF", "DOCX", "XLSX", "PPTX", "TXT", "CSV", "XML", "JSON", "ZIP", "RAR", "JPG", "PNG");
        formato.setPlaceholder("Seleziona formato...");
        formato.setClearButtonVisible(true);
        
        dimensioneMin.setPlaceholder("es: 100");
        dimensioneMin.setClearButtonVisible(true);
        dimensioneMin.setPrefixComponent(new Icon(VaadinIcon.ARROW_UP));
        dimensioneMin.addValueChangeListener(e -> validateSizeRange());
        
        dimensioneMax.setPlaceholder("es: 5000");
        dimensioneMax.setClearButtonVisible(true);
        dimensioneMax.setPrefixComponent(new Icon(VaadinIcon.ARROW_DOWN));
        dimensioneMax.addValueChangeListener(e -> validateSizeRange());
        
        titolo.setPlaceholder("Titolo del documento...");
        titolo.setClearButtonVisible(true);
        titolo.setPrefixComponent(new Icon(VaadinIcon.FILE_TEXT_O));
        
        tags.setPlaceholder("Separati da virgola: urgente, riservato...");
        tags.setClearButtonVisible(true);
        tags.setPrefixComponent(new Icon(VaadinIcon.TAGS));
        
        metaKey.setPlaceholder("es: categoria, progetto...");
        metaKey.setClearButtonVisible(true);
        metaKey.setPrefixComponent(new Icon(VaadinIcon.KEY));
        
        metaValue.setPlaceholder("Valore del metadato custom...");
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

        // Layout metadati principali
        FlexLayout metaRow1 = new FlexLayout(autore, formato);
        metaRow1.setWidthFull();
        metaRow1.getStyle().set("gap", "1rem").set("flex-wrap", "wrap");
        autore.setWidth("calc(50% - 0.5rem)");
        autore.getStyle().set("min-width", "200px").set("flex", "1");
        formato.setWidth("calc(50% - 0.5rem)");
        formato.getStyle().set("min-width", "200px").set("flex", "1");
        
        FlexLayout metaRow2 = new FlexLayout(dimensioneMin, dimensioneMax);
        metaRow2.setWidthFull();
        metaRow2.getStyle().set("gap", "1rem").set("flex-wrap", "wrap");
        dimensioneMin.setWidth("calc(50% - 0.5rem)");
        dimensioneMin.getStyle().set("min-width", "200px").set("flex", "1");
        dimensioneMax.setWidth("calc(50% - 0.5rem)");
        dimensioneMax.getStyle().set("min-width", "200px").set("flex", "1");
        
        FlexLayout metaRow3 = new FlexLayout(titolo, tags);
        metaRow3.setWidthFull();
        metaRow3.getStyle().set("gap", "1rem").set("flex-wrap", "wrap");
        titolo.setWidth("calc(50% - 0.5rem)");
        titolo.getStyle().set("min-width", "200px").set("flex", "1");
        tags.setWidth("calc(50% - 0.5rem)");
        tags.getStyle().set("min-width", "200px").set("flex", "1");
        
        // Layout metadati custom
        FlexLayout metaRow4 = new FlexLayout(metaKey, metaValue);
        metaRow4.setWidthFull();
        metaRow4.getStyle().set("gap", "1rem").set("flex-wrap", "wrap");
        metaKey.setWidth("calc(50% - 0.5rem)");
        metaKey.getStyle().set("min-width", "200px").set("flex", "1");
        metaValue.setWidth("calc(50% - 0.5rem)");
        metaValue.getStyle().set("min-width", "200px").set("flex", "1");

        VerticalLayout generalLayout = new VerticalLayout(generalRow1, generalRow2);
        generalLayout.setPadding(true);
        generalFiltersDetails.setSummaryText("Filtri Generali");
        generalFiltersDetails.setContent(generalLayout);
        generalFiltersDetails.setOpened(true);

        VerticalLayout metaLayout = new VerticalLayout(metaRow1, metaRow2, metaRow3, metaRow4);
        metaLayout.setPadding(true);
        metaLayout.setSpacing(true);
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
        if (!validateDateRange() || !validateSizeRange()) {
            return;
        }
        
        // Costruisce l'oggetto DTO con tutti i filtri
        DocumentSearchFilterDTO searchFilter = buildSearchFilter();
        
        // Log dell'oggetto per il test (in produzione sarà passato al service)
        log.info("=== RICERCA DOCUMENTI ===");
        log.info("Filtri di ricerca: {}", searchFilter);
        log.info("========================");
        
        // TODO: In produzione chiamare il service
        // List<FileResultDTO> results = documentService.searchDocuments(searchFilter);
        
        // 
        
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
        addFilterBadge("Autore", autore.getValue());
        addFilterBadge("Formato", formato.getValue());
        if (dimensioneMin.getValue() != null && !dimensioneMin.getValue().isEmpty()) {
            addFilterBadge("Dim. Min", dimensioneMin.getValue() + " KB");
        }
        if (dimensioneMax.getValue() != null && !dimensioneMax.getValue().isEmpty()) {
            addFilterBadge("Dim. Max", dimensioneMax.getValue() + " KB");
        }
        addFilterBadge("Titolo", titolo.getValue());
        addFilterBadge("Tags", tags.getValue());
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
    
    private DocumentSearchFilterDTO buildSearchFilter() {
        DocumentSearchFilterDTO filter = new DocumentSearchFilterDTO();
        
        // Filtri generali
        filter.setNomeFile(nomeFile.getValue());
        filter.setTipologia(tipologia.getValue());
        filter.setDataDa(dataDa.getValue());
        filter.setDataA(dataA.getValue());
        
        // Filtri metadati
        filter.setAutore(autore.getValue());
        filter.setFormato(formato.getValue());
        
        // Dimensioni con parsing
        String minStr = dimensioneMin.getValue();
        String maxStr = dimensioneMax.getValue();
        if (minStr != null && !minStr.isEmpty()) {
            try {
                filter.setDimensioneMinKB(Double.parseDouble(minStr));
            } catch (NumberFormatException e) {
                log.warn("Valore dimensione minima non valido: {}", minStr);
            }
        }
        if (maxStr != null && !maxStr.isEmpty()) {
            try {
                filter.setDimensioneMaxKB(Double.parseDouble(maxStr));
            } catch (NumberFormatException e) {
                log.warn("Valore dimensione massima non valido: {}", maxStr);
            }
        }
        
        filter.setTitolo(titolo.getValue());
        filter.setTags(tags.getValue());
        filter.setMetadataChiave(metaKey.getValue());
        filter.setMetadataValore(metaValue.getValue());
        
        // Filtro strutturato (tree)
        if (selectedTreeItem != null) {
            filter.setStrutturaCode(selectedTreeItem.getCode());
            filter.setStrutturaType(selectedTreeItem.getType());
            filter.setStrutturaDescrizione(selectedTreeItem.getDescrizione());
        }
        
        return filter;
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
        autore.clear();
        formato.clear();
        dimensioneMin.clear();
        dimensioneMax.clear();
        titolo.clear();
        tags.clear();
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
    
    private boolean validateSizeRange() {
        String minStr = dimensioneMin.getValue();
        String maxStr = dimensioneMax.getValue();
        
        if ((minStr != null && !minStr.isEmpty()) || (maxStr != null && !maxStr.isEmpty())) {
            try {
                Double min = (minStr != null && !minStr.isEmpty()) ? Double.parseDouble(minStr) : null;
                Double max = (maxStr != null && !maxStr.isEmpty()) ? Double.parseDouble(maxStr) : null;
                
                if (min != null && min < 0) {
                    dimensioneMin.setInvalid(true);
                    dimensioneMin.setErrorMessage("Valore non valido");
                    return false;
                }
                if (max != null && max < 0) {
                    dimensioneMax.setInvalid(true);
                    dimensioneMax.setErrorMessage("Valore non valido");
                    return false;
                }
                
                if (min != null && max != null && min > max) {
                    Notification notification = Notification.show(
                        "La dimensione minima non può essere maggiore della massima",
                        4000,
                        Notification.Position.MIDDLE
                    );
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    dimensioneMin.setInvalid(true);
                    dimensioneMax.setInvalid(true);
                    return false;
                }
                
            } catch (NumberFormatException e) {
                Notification notification = Notification.show(
                    "Inserire valori numerici validi per la dimensione",
                    4000,
                    Notification.Position.MIDDLE
                );
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                if (minStr != null && !minStr.isEmpty()) {
                    dimensioneMin.setInvalid(true);
                    dimensioneMin.setErrorMessage("Numero non valido");
                }
                if (maxStr != null && !maxStr.isEmpty()) {
                    dimensioneMax.setInvalid(true);
                    dimensioneMax.setErrorMessage("Numero non valido");
                }
                return false;
            }
        }
        
        dimensioneMin.setInvalid(false);
        dimensioneMax.setInvalid(false);
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