package io.bootify.my_app.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.bootify.my_app.component.StructuredTree;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "google-search", layout = MainLayout.class)
@PageTitle("Google Search")
public class GoogleSearchView extends Div {

    private final VerticalLayout searchResultsContainer;
    private final Div chatbotWindow;
    private final VerticalLayout chatContainer;
    private final TextField messageInput;
    private final Button chatbotFab;
    private boolean isChatOpen = false;
    private int currentPage = 1;
    private String currentQuery = "java programming";
    private int resultsPerPage = 6;
    private static final String AI_RESPONSE = "Ciao! Sono un assistente virtuale. Posso aiutarti con qualsiasi domanda! 🤖";
    private boolean hasSearched = false;

    // Filter state
    private Set<String> selectedFileTypes = new HashSet<>();
    private String selectedDateRange = "Qualsiasi data";
    private LocalDate customDateFrom = null;
    private LocalDate customDateTo = null;

    // Filter components
    private Details filtersDetails;
    private StructuredTree structuredTree;
    private MultiSelectComboBox<String> fileTypeFilter;
    private ComboBox<String> dateRangeFilter;
    private DatePicker dateFromPicker;
    private DatePicker dateToPicker;
    private HorizontalLayout customDateContainer;
    private HorizontalLayout activeFiltersBar;

    public GoogleSearchView() {
        addClassName("google-search-view");
        setSizeFull();
        getStyle()
                .set("position", "relative")
                .set("overflow", "auto");

        // Main content
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setSizeFull();
        mainContent.setPadding(false);
        mainContent.setSpacing(false);

        // Google-style header
        mainContent.add(createHeader());

        // Filters bar (hidden initially)
        filtersDetails = createFiltersBar();
        filtersDetails.setVisible(false);
        mainContent.add(filtersDetails);

        // Active filters chips
        activeFiltersBar = new HorizontalLayout();
        activeFiltersBar.setSpacing(true);
        activeFiltersBar.setPadding(false);
        activeFiltersBar.getStyle()
                .set("max-width", "700px")
                .set("margin", "0 auto")
                .set("width", "100%")
                .set("padding", "0 16px")
                .set("flex-wrap", "wrap");
        activeFiltersBar.setVisible(false);
        mainContent.add(activeFiltersBar);

        // Search results
        searchResultsContainer = new VerticalLayout();
        searchResultsContainer.setPadding(true);
        searchResultsContainer.setSpacing(true);
        searchResultsContainer.getStyle()
                .set("max-width", "700px")
                .set("margin", "0 auto")
                .set("width", "100%");

        // Pagina iniziale pulita - i risultati appaiono dopo la prima ricerca

        mainContent.add(searchResultsContainer);
        add(mainContent);

        // Chatbot FAB
        chatbotFab = createChatbotFab();
        add(chatbotFab);

        // Chatbot window
        chatContainer = new VerticalLayout();
        chatContainer.setSpacing(true);
        chatContainer.setPadding(true);
        chatContainer.getStyle()
                .set("flex", "1")
                .set("overflow-y", "auto")
                .set("background-color", "var(--lumo-contrast-5pct)");

        messageInput = new TextField();
        messageInput.setPlaceholder("Scrivi un messaggio...");
        messageInput.setWidthFull();

        Button sendButton = new Button("Invia", new Icon(VaadinIcon.PAPERPLANE));
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendButton.addClickListener(e -> sendMessage());

        messageInput.addKeyPressListener(event -> {
            if (event.getKey().getKeys().contains("Enter")) {
                sendMessage();
            }
        });

        HorizontalLayout inputLayout = new HorizontalLayout(messageInput, sendButton);
        inputLayout.setSpacing(true);
        inputLayout.setPadding(true);
        inputLayout.setWidthFull();
        inputLayout.getStyle()
                .set("background-color", "var(--lumo-base-color)")
                .set("border-top", "1px solid var(--lumo-contrast-10pct)");

        chatbotWindow = createChatbotWindow(chatContainer, inputLayout);
        add(chatbotWindow);

        // Welcome message
        addBotMessage("Ciao! Come posso aiutarti oggi?");
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setSpacing(true);
        header.getStyle()
                .set("background-color", "white")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
                .set("padding", "20px 200px");

        // Google-style logo
        H2 logo = new H2();
        logo.getStyle()
                .set("margin", "0")
                .set("color", "#4285f4")
                .set("font-weight", "bold")
                .set("font-size", "28px");

        // Search bar
        TextField searchField = new TextField();
        searchField.setPlaceholder("Cerca...");
        searchField.setWidthFull();
        searchField.getStyle()
                .set("max-width", "600px")
                .set("border-radius", "24px");

        Button searchButton = new Button(new Icon(VaadinIcon.SEARCH));
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        searchButton.addClickListener(e -> {
            String query = searchField.getValue();
            if (!query.isEmpty()) {
                hasSearched = true;
                filtersDetails.setVisible(true);
                searchResultsContainer.removeAll();
                addSimulatedResults(query, 1);
            }
        });

        searchField.addKeyPressListener(event -> {
            if (event.getKey().getKeys().contains("Enter")) {
                String query = searchField.getValue();
                if (!query.isEmpty()) {
                    hasSearched = true;
                    filtersDetails.setVisible(true);
                    searchResultsContainer.removeAll();
                    addSimulatedResults(query, 1);
                }
            }
        });

        HorizontalLayout searchBar = new HorizontalLayout(searchField, searchButton);
        searchBar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        searchBar.setWidthFull();
        searchBar.getStyle().set("max-width", "700px");

        header.add(logo, searchBar);
        header.expand(searchBar);

        return header;
    }

    private Details createFiltersBar() {
        // Container for filters
        HorizontalLayout filtersRow = new HorizontalLayout();
        filtersRow.setSpacing(true);
        filtersRow.setPadding(false);
        filtersRow.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        filtersRow.getStyle()
                .set("padding", "8px 0")
            .set("flex-wrap", "wrap")
            .set("flex", "1");

        // --- File Type Filter (più compatto) - Multiselezione ---
        fileTypeFilter = new MultiSelectComboBox<>("Tipo");
        fileTypeFilter.setItems("PDF", "DOC/DOCX", "Video", "Audio", "Archivio (ZIP/RAR)");
        fileTypeFilter.setWidth("200px");
        fileTypeFilter.setPlaceholder("Tutti i tipi");
        fileTypeFilter.getStyle()
                .set("--vaadin-combo-box-overlay-width", "180px")
                .set("font-size", "13px");

        fileTypeFilter.addValueChangeListener(e -> {
            selectedFileTypes = new HashSet<>(e.getValue());
            applyFilters();
        });

        // --- Date Range Filter (più compatto) ---
        dateRangeFilter = new ComboBox<>("Data");
        dateRangeFilter.setItems("Qualsiasi data", "Ultimo giorno", "Ultima settimana", "Ultimo mese", "Ultimo anno", "Intervallo personalizzato");
        dateRangeFilter.setValue("Qualsiasi data");
        dateRangeFilter.setWidth("180px");
        dateRangeFilter.setClearButtonVisible(true);
        dateRangeFilter.getStyle()
                .set("--vaadin-combo-box-overlay-width", "200px")
                .set("font-size", "13px");

        // Custom date pickers (più compatti)
        dateFromPicker = new DatePicker("Da");
        dateFromPicker.setLocale(Locale.ITALY);
        dateFromPicker.setWidth("140px");
        dateFromPicker.setClearButtonVisible(true);
        dateFromPicker.getStyle().set("font-size", "13px");

        dateToPicker = new DatePicker("A");
        dateToPicker.setLocale(Locale.ITALY);
        dateToPicker.setWidth("140px");
        dateToPicker.setClearButtonVisible(true);
        dateToPicker.setValue(LocalDate.now());
        dateToPicker.getStyle().set("font-size", "13px");

        Button applyCustomDateBtn = new Button("Applica", new Icon(VaadinIcon.CHECK));
        applyCustomDateBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        applyCustomDateBtn.getStyle()
                .set("margin-top", "auto")
                .set("font-size", "12px");
        applyCustomDateBtn.addClickListener(e -> {
            customDateFrom = dateFromPicker.getValue();
            customDateTo = dateToPicker.getValue();
            applyFilters();
        });

        customDateContainer = new HorizontalLayout(dateFromPicker, dateToPicker, applyCustomDateBtn);
        customDateContainer.setSpacing(true);
        customDateContainer.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        customDateContainer.setVisible(false);

        dateRangeFilter.addValueChangeListener(e -> {
            String value = e.getValue() != null ? e.getValue() : "Qualsiasi data";
            selectedDateRange = value;
            boolean isCustom = "Intervallo personalizzato".equals(value);
            customDateContainer.setVisible(isCustom);
            if (!isCustom) {
                customDateFrom = null;
                customDateTo = null;
                applyFilters();
            }
        });

        // Reset all filters button (più compatto)
        Button resetFiltersBtn = new Button("Reset", new Icon(VaadinIcon.CLOSE_SMALL));
        resetFiltersBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        resetFiltersBtn.getStyle()
                .set("margin-top", "auto")
                .set("font-size", "12px");
        resetFiltersBtn.addClickListener(e -> {
            fileTypeFilter.clear();
            dateRangeFilter.setValue("Qualsiasi data");
            dateFromPicker.clear();
            dateToPicker.setValue(LocalDate.now());
            customDateContainer.setVisible(false);
            selectedFileTypes.clear();
            selectedDateRange = "Qualsiasi data";
            customDateFrom = null;
            customDateTo = null;
            applyFilters();
        });

        filtersRow.add(fileTypeFilter, dateRangeFilter, customDateContainer, resetFiltersBtn);

        // Pannello sinistro: filtri standard
        VerticalLayout leftPanel = new VerticalLayout(filtersRow);
        leftPanel.setPadding(false);
        leftPanel.setSpacing(false);
        leftPanel.setWidthFull();
        leftPanel.getStyle()
            .set("flex", "1 1 520px")
            .set("min-width", "420px");

        // Struttura ad albero — card laterale dedicata
        structuredTree = new StructuredTree();
        structuredTree.getStyle()
            .set("width", "100%")
            .set("background", "white")
            .set("border-radius", "12px")
            .set("padding", "0")
            .set("box-sizing", "border-box");

        H4 treeTitle = new H4("Filtro Strutturato");
        treeTitle.getStyle()
            .set("margin", "0")
            .set("font-size", "16px")
            .set("color", "#16325c");

        Span treeDescription = new Span("Seleziona rapidamente area, complesso o trattazione senza comprimere l'albero.");
        treeDescription.getStyle()
            .set("font-size", "12px")
            .set("line-height", "1.4")
            .set("color", "#5f6b7a");

        Div treeHeader = new Div(treeTitle, treeDescription);
        treeHeader.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "4px");

        VerticalLayout treeCard = new VerticalLayout(treeHeader, structuredTree);
        treeCard.setPadding(false);
        treeCard.setSpacing(true);
        treeCard.getStyle()
            .set("flex", "0 1 380px")
            .set("min-width", "340px")
            .set("max-width", "420px")
            .set("padding", "16px")
            .set("border-radius", "18px")
            .set("background", "linear-gradient(180deg, #f7faff 0%, #eef4ff 100%)")
            .set("border", "1px solid #d7e3f7")
            .set("box-shadow", "0 10px 24px rgba(15, 23, 42, 0.08)");

        // Layout contenuto: sinistra filtri, destra albero strutturato
        HorizontalLayout filtersContent = new HorizontalLayout(leftPanel, treeCard);
        filtersContent.setPadding(false);
        filtersContent.setSpacing(true);
        filtersContent.setWidthFull();
        filtersContent.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.START);
        filtersContent.getStyle()
            .set("flex-wrap", "wrap")
            .set("gap", "20px");

        // Crea Details collassabile
        Details details = new Details("Filtri di ricerca", filtersContent);
        details.setOpened(false);
        details.getStyle()
                .set("background-color", "#f8f9fa")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
            .set("padding", "8px 14px")
                .set("max-width", "960px")
                .set("margin", "0 auto")
                .set("width", "100%")
                .set("font-size", "13px");

        return details;
    }

    private void updateActiveFiltersBar() {
        activeFiltersBar.removeAll();
        boolean hasActiveFilter = false;

        if (!selectedFileTypes.isEmpty()) {
            hasActiveFilter = true;
            String typesLabel = "Tipo: " + String.join(", ", selectedFileTypes);
            activeFiltersBar.add(createFilterChip(typesLabel, () -> {
                fileTypeFilter.clear();
                selectedFileTypes.clear();
                applyFilters();
            }));
        }

        if (!"Qualsiasi data".equals(selectedDateRange)) {
            hasActiveFilter = true;
            String dateLabel = "Data: " + selectedDateRange;
            if ("Intervallo personalizzato".equals(selectedDateRange) && customDateFrom != null && customDateTo != null) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dateLabel = "Data: " + customDateFrom.format(fmt) + " - " + customDateTo.format(fmt);
            }
            activeFiltersBar.add(createFilterChip(dateLabel, () -> {
                dateRangeFilter.setValue("Qualsiasi data");
                selectedDateRange = "Qualsiasi data";
                customDateContainer.setVisible(false);
                customDateFrom = null;
                customDateTo = null;
                applyFilters();
            }));
        }

        activeFiltersBar.setVisible(hasActiveFilter);
    }

    private Component createFilterChip(String label, Runnable onRemove) {
        HorizontalLayout chip = new HorizontalLayout();
        chip.setSpacing(false);
        chip.setPadding(false);
        chip.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        chip.getStyle()
                .set("background-color", "#e8f0fe")
                .set("color", "#1a73e8")
                .set("border-radius", "16px")
                .set("padding", "4px 12px")
                .set("font-size", "13px")
                .set("font-weight", "500")
                .set("display", "inline-flex")
                .set("align-items", "center")
                .set("gap", "6px");

        Span text = new Span(label);
        Button removeBtn = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
        removeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        removeBtn.getStyle()
                .set("min-width", "20px")
                .set("width", "20px")
                .set("height", "20px")
                .set("padding", "0")
                .set("color", "#1a73e8")
                .set("cursor", "pointer");
        removeBtn.addClickListener(e -> onRemove.run());

        chip.add(text, removeBtn);
        return chip;
    }

    private void applyFilters() {
        updateActiveFiltersBar();
        searchResultsContainer.removeAll();
        addSimulatedResults(currentQuery, 1);
    }

    private void addSimulatedResults() {
        addSimulatedResults("java programming", 1);
    }

    private void addSimulatedResults(String query, int page) {
        currentQuery = query;
        currentPage = page;

        List<SearchResult> allResults = generateSearchResults(query, page);

        // Apply file type filter
        List<SearchResult> filteredResults = allResults.stream()
                .filter(this::matchesFileTypeFilter)
                .collect(Collectors.toList());

        // Apply date filter
        filteredResults = filteredResults.stream()
                .filter(this::matchesDateFilter)
                .collect(Collectors.toList());

        // Results info
        int totalEstimated = filteredResults.size() * 100;
        Paragraph resultsInfo = new Paragraph(
                "Circa " + String.format("%,d", totalEstimated) + " risultati (0,45 secondi)"
                        + (!selectedFileTypes.isEmpty() ? " — Filtro tipo: " + String.join(", ", selectedFileTypes) : "")
                        + (!"Qualsiasi data".equals(selectedDateRange) ? " — Filtro data: " + selectedDateRange : "")
        );
        resultsInfo.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "14px")
                .set("margin-top", "10px");
        searchResultsContainer.add(resultsInfo);

        if (filteredResults.isEmpty()) {
            Div noResults = new Div();
            noResults.getStyle()
                    .set("text-align", "center")
                    .set("padding", "40px 0");

            Icon sadIcon = new Icon(VaadinIcon.FROWN_O);
            sadIcon.setSize("48px");
            sadIcon.getStyle().set("color", "var(--lumo-secondary-text-color)");

            H3 noResultsTitle = new H3("Nessun risultato trovato");
            noResultsTitle.getStyle().set("color", "var(--lumo-secondary-text-color)");

            Paragraph suggestion = new Paragraph("Prova a modificare i filtri o la query di ricerca.");
            suggestion.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-size", "14px");

            noResults.add(sadIcon, noResultsTitle, suggestion);
            searchResultsContainer.add(noResults);
        } else {
            for (SearchResult result : filteredResults) {
                searchResultsContainer.add(createSearchResultCard(result));
            }
            searchResultsContainer.add(createPaginationControls());
        }
    }

    private boolean matchesFileTypeFilter(SearchResult result) {
        if (selectedFileTypes.isEmpty()) {
            return true;
        }
        
        // Il risultato deve soddisfare almeno uno dei tipi selezionati
        for (String selectedType : selectedFileTypes) {
            switch (selectedType) {
                case "PDF":
                    if (result.files != null && result.files.stream().anyMatch(f -> "PDF".equalsIgnoreCase(f.type))) {
                        return true;
                    }
                    break;
                case "DOC/DOCX":
                    if (result.files != null && result.files.stream()
                            .anyMatch(f -> "DOC".equalsIgnoreCase(f.type) || "DOCX".equalsIgnoreCase(f.type))) {
                        return true;
                    }
                    break;
                case "Video":
                    if (result.videoUrl != null || (result.files != null && result.files.stream()
                            .anyMatch(f -> "MP4".equalsIgnoreCase(f.type) || "AVI".equalsIgnoreCase(f.type) || "MOV".equalsIgnoreCase(f.type)))) {
                        return true;
                    }
                    break;
                case "Audio":
                    if (result.files != null && result.files.stream()
                            .anyMatch(f -> "MP3".equalsIgnoreCase(f.type) || "WAV".equalsIgnoreCase(f.type))) {
                        return true;
                    }
                    break;
                case "Archivio (ZIP/RAR)":
                    if (result.files != null && result.files.stream()
                            .anyMatch(f -> "ZIP".equalsIgnoreCase(f.type) || "RAR".equalsIgnoreCase(f.type))) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    private boolean matchesDateFilter(SearchResult result) {
        if ("Qualsiasi data".equals(selectedDateRange)) {
            return true;
        }
        if (result.lastUpdated == null) {
            return true;
        }

        LocalDate now = LocalDate.now();
        LocalDate resultDate = result.lastUpdated;

        switch (selectedDateRange) {
            case "Ultimo giorno":
                return !resultDate.isBefore(now.minusDays(1));
            case "Ultima settimana":
                return !resultDate.isBefore(now.minusWeeks(1));
            case "Ultimo mese":
                return !resultDate.isBefore(now.minusMonths(1));
            case "Ultimo anno":
                return !resultDate.isBefore(now.minusYears(1));
            case "Intervallo personalizzato":
                if (customDateFrom != null && customDateTo != null) {
                    return !resultDate.isBefore(customDateFrom) && !resultDate.isAfter(customDateTo);
                } else if (customDateFrom != null) {
                    return !resultDate.isBefore(customDateFrom);
                } else if (customDateTo != null) {
                    return !resultDate.isAfter(customDateTo);
                }
                return true;
            default:
                return true;
        }
    }

    private List<SearchResult> generateSearchResults(String query, int page) {
        List<SearchResult> results = new ArrayList<>();

        int startIndex = (page - 1) * resultsPerPage;
        LocalDate now = LocalDate.now();

        for (int i = 0; i < resultsPerPage; i++) {
            int resultNumber = startIndex + i + 1;

            if (i == 0) {
                results.add(new SearchResult(
                        "[Pagina " + page + ", Risultato " + resultNumber + "] Java Programming - Official Documentation",
                        "https://docs.oracle.com/javase/tutorial/?page=" + page,
                        "The Java™ Tutorials are practical guides for programmers who want to use the Java programming language to create applications. They include hundreds of complete, working examples... (Risultato #" + resultNumber + " della pagina " + page + ")",
                        "Oracle",
                        null,
                        null,
                        now.minusDays(1)
                ));
            } else if (i == 1) {
                results.add(new SearchResult(
                        "Learn " + query + " - Step by Step Guide (Risultato #" + resultNumber + ")",
                        "https://www.example.com/guide?page=" + page,
                        "Comprehensive guide to " + query + ". Start from basics and advance to expert level. Updated with latest features and best practices for 2025. (Pagina " + page + ", risultato " + resultNumber + ")",
                        "Example Learning Platform",
                        null,
                        null,
                        now.minusWeeks(2)
                ));
            } else if (i == 2) {
                // File results with PDF + DOCX
                List<FileAttachment> pdfFiles = new ArrayList<>();
                pdfFiles.add(new FileAttachment("Introduction_to_" + query.replace(" ", "_") + ".pdf", "PDF", "2.4 MB"));
                pdfFiles.add(new FileAttachment("Advanced_" + query.replace(" ", "_") + "_Guide.pdf", "PDF", "5.1 MB"));
                pdfFiles.add(new FileAttachment(query.replace(" ", "_") + "_Best_Practices.docx", "DOCX", "1.8 MB"));
                pdfFiles.add(new FileAttachment(query.replace(" ", "_") + "_Cheat_Sheet.pdf", "PDF", "850 KB"));

                results.add(new SearchResult(
                        query + " - Documentation and Resources (#" + resultNumber + ")",
                        "https://www.resources.com/" + query.replace(" ", "-"),
                        "Comprehensive collection of documentation, tutorials, and reference materials for " + query + ". Download PDF guides, watch video tutorials, and access code examples. (Risultato " + resultNumber + ")",
                        "Resources.com",
                        pdfFiles,
                        null,
                        now.minusDays(3)
                ));
            } else if (i == 3) {
                // Video result
                results.add(new SearchResult(
                        query + " - Complete Video Tutorial (Risultato #" + resultNumber + ")",
                        "https://www.example.com/video",
                        "Watch this comprehensive video tutorial covering all aspects of " + query + ". Duration: 45:30. Perfect for visual learners. (Risultato " + resultNumber + " - Pagina " + page + ")",
                        "Video Platform",
                        null,
                        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                        now.minusMonths(1)
                ));
            } else if (i == 4) {
                results.add(new SearchResult(
                        "Stack Overflow - " + query + " Questions (Risultato #" + resultNumber + ")",
                        "https://stackoverflow.com/questions/tagged/" + query.replace(" ", "+"),
                        "Browse thousands of questions and answers about " + query + ". Get help from the community and learn from real-world problems. (Risultato " + resultNumber + ", Pagina " + page + ")",
                        "Stack Overflow",
                        null,
                        null,
                        now.minusDays(5)
                ));
            } else {
                // Additional generic results with varied dates
                LocalDate resultDate;
                if (page == 1) {
                    resultDate = now.minusMonths(3 + i);
                } else {
                    resultDate = now.minusMonths(page + i);
                }

                results.add(new SearchResult(
                        query + " - Resource #" + resultNumber + " (Page " + page + ")",
                        "https://www.example" + i + ".com/" + query.replace(" ", "-"),
                        "Additional resource for " + query + ". This is result number " + resultNumber + " on page " + page + ". Learn more about " + query + " with this comprehensive guide and examples.",
                        "Example Resource " + i,
                        null,
                        null,
                        resultDate
                ));
            }
        }

        return results;
    }

    private Component createSearchResultCard(SearchResult result) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(false);
        card.setSpacing(false);
        card.getStyle()
                .set("padding", "12px 0")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        // URL and site name
        HorizontalLayout urlRow = new HorizontalLayout();
        urlRow.setSpacing(true);
        urlRow.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        urlRow.getStyle().set("margin", "0 0 4px 0");

        Paragraph url = new Paragraph(result.site + " › " + result.url);
        url.getStyle()
                .set("margin", "0")
                .set("font-size", "14px")
                .set("color", "var(--lumo-secondary-text-color)");

        urlRow.add(url);

        // Date badge
        if (result.lastUpdated != null) {
            Span dateBadge = new Span(result.lastUpdated.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ITALY)));
            dateBadge.getStyle()
                    .set("font-size", "11px")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("background-color", "var(--lumo-contrast-5pct)")
                    .set("padding", "2px 8px")
                    .set("border-radius", "10px")
                    .set("white-space", "nowrap");
            urlRow.add(dateBadge);
        }

        card.add(urlRow);

        // Title
        Anchor title = new Anchor(result.url, result.title);
        title.getStyle()
                .set("font-size", "20px")
                .set("color", "#1a0dab")
                .set("text-decoration", "none")
                .set("margin", "0 0 8px 0")
                .set("display", "block")
                .set("font-weight", "400");
        title.getElement().setAttribute("target", "_blank");

        // Description
        Paragraph description = new Paragraph(result.description);
        description.getStyle()
                .set("margin", "0")
                .set("font-size", "14px")
                .set("color", "var(--lumo-body-text-color)")
                .set("line-height", "1.6");

        card.add(title, description);

        // File type badges
        if (result.files != null && !result.files.isEmpty()) {
            HorizontalLayout typeBadges = new HorizontalLayout();
            typeBadges.setSpacing(true);
            typeBadges.getStyle().set("margin-top", "8px");

            result.files.stream()
                    .map(f -> f.type.toUpperCase())
                    .distinct()
                    .forEach(type -> {
                        Span badge = new Span(type);
                        badge.getStyle()
                                .set("font-size", "11px")
                                .set("font-weight", "600")
                                .set("color", "white")
                                .set("background-color", getFileColor(type))
                                .set("padding", "2px 8px")
                                .set("border-radius", "4px");
                        typeBadges.add(badge);
                    });

            card.add(typeBadges);
        }

        if (result.videoUrl != null) {
            Span videoBadge = new Span("VIDEO");
            videoBadge.getStyle()
                    .set("font-size", "11px")
                    .set("font-weight", "600")
                    .set("color", "white")
                    .set("background-color", "#7b1fa2")
                    .set("padding", "2px 8px")
                    .set("border-radius", "4px")
                    .set("margin-top", "8px")
                    .set("display", "inline-block");
            card.add(videoBadge);
        }

        // Add file attachments if present
        if (result.files != null && !result.files.isEmpty()) {
            VerticalLayout filesContainer = new VerticalLayout();
            filesContainer.setPadding(false);
            filesContainer.setSpacing(true);
            filesContainer.getStyle()
                    .set("margin-top", "12px")
                    .set("padding", "12px")
                    .set("background-color", "var(--lumo-contrast-5pct)")
                    .set("border-radius", "8px");

            for (FileAttachment file : result.files) {
                HorizontalLayout fileRow = new HorizontalLayout();
                fileRow.setDefaultVerticalComponentAlignment(Alignment.CENTER);
                fileRow.setSpacing(true);
                fileRow.getStyle().set("padding", "4px 0");

                Icon fileIcon = getFileIcon(file.type);
                fileIcon.setSize("20px");
                fileIcon.getStyle().set("color", getFileColor(file.type));

                Span fileName = new Span(file.name);
                fileName.getStyle()
                        .set("font-size", "14px")
                        .set("color", "#1a0dab");

                Span fileSize = new Span("(" + file.size + ")");
                fileSize.getStyle()
                        .set("font-size", "12px")
                        .set("color", "var(--lumo-secondary-text-color)")
                        .set("margin-left", "8px");

                fileRow.add(fileIcon, fileName, fileSize);
                filesContainer.add(fileRow);
            }

            card.add(filesContainer);
        }

        // Add video player if video URL is present
        if (result.videoUrl != null) {
            Div videoContainer = new Div();
            videoContainer.getStyle()
                    .set("margin-top", "12px")
                    .set("background-color", "#000")
                    .set("border-radius", "8px")
                    .set("overflow", "hidden");

            Div videoPlayer = new Div();
            videoPlayer.setId("video-player-" + result.hashCode());
            videoPlayer.getStyle()
                    .set("width", "100%")
                    .set("max-width", "640px")
                    .set("aspect-ratio", "16/9");

            Button playButton = new Button("▶ Riproduci Video", new Icon(VaadinIcon.PLAY));
            playButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
            playButton.getStyle()
                    .set("margin", "12px");

            playButton.addClickListener(e -> {
                videoContainer.removeAll();
                getUI().ifPresent(ui -> ui.getPage().executeJs(
                        "const container = $0;" +
                                "container.innerHTML = '<video controls autoplay style=\"width: 100%; max-width: 640px; display: block;\">" +
                                "<source src=\"" + result.videoUrl + "\" type=\"video/mp4\">" +
                                "Your browser does not support the video tag.</video>';" +
                                "container.querySelector('video').play();",
                        videoPlayer.getElement()
                ));
                videoContainer.add(videoPlayer);
            });

            videoContainer.add(playButton);
            card.add(videoContainer);
        }

        return card;
    }

    private Icon getFileIcon(String type) {
        return switch (type.toUpperCase()) {
            case "PDF" -> new Icon(VaadinIcon.FILE_TEXT);
            case "DOCX", "DOC" -> new Icon(VaadinIcon.FILE_TEXT_O);
            case "MP4", "AVI", "MOV" -> new Icon(VaadinIcon.FILE_MOVIE);
            case "MP3", "WAV" -> new Icon(VaadinIcon.FILE_SOUND);
            case "ZIP", "RAR" -> new Icon(VaadinIcon.FILE_ZIP);
            default -> new Icon(VaadinIcon.FILE);
        };
    }

    private String getFileColor(String type) {
        return switch (type.toUpperCase()) {
            case "PDF" -> "#d32f2f";
            case "DOCX", "DOC" -> "#1976d2";
            case "MP4", "AVI", "MOV" -> "#7b1fa2";
            case "MP3", "WAV" -> "#f57c00";
            case "ZIP", "RAR" -> "#616161";
            default -> "var(--lumo-secondary-text-color)";
        };
    }

    private Component createPaginationControls() {
        HorizontalLayout pagination = new HorizontalLayout();
        pagination.setSpacing(true);
        pagination.setPadding(true);
        pagination.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        pagination.getStyle()
                .set("margin-top", "20px")
                .set("margin-bottom", "20px");

        // Previous button
        Button prevButton = new Button("Indietro");
        prevButton.setEnabled(currentPage > 1);
        prevButton.addClickListener(e -> {
            if (currentPage > 1) {
                searchResultsContainer.removeAll();
                addSimulatedResults(currentQuery, currentPage - 1);
                scrollToTop();
            }
        });

        // Page numbers
        HorizontalLayout pageNumbers = new HorizontalLayout();
        pageNumbers.setSpacing(false);

        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(10, currentPage + 2);

        for (int i = startPage; i <= endPage; i++) {
            final int pageNum = i;
            Button pageButton = new Button(String.valueOf(i));

            if (i == currentPage) {
                pageButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            } else {
                pageButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            }

            pageButton.getStyle()
                    .set("min-width", "40px")
                    .set("margin", "0 2px");

            pageButton.addClickListener(e -> {
                searchResultsContainer.removeAll();
                addSimulatedResults(currentQuery, pageNum);
                scrollToTop();
            });

            pageNumbers.add(pageButton);
        }

        // Next button
        Button nextButton = new Button("Avanti");
        nextButton.setEnabled(currentPage < 10);
        nextButton.addClickListener(e -> {
            if (currentPage < 10) {
                searchResultsContainer.removeAll();
                addSimulatedResults(currentQuery, currentPage + 1);
                scrollToTop();
            }
        });

        pagination.add(prevButton, pageNumbers, nextButton);
        return pagination;
    }

    private void scrollToTop() {
        getUI().ifPresent(ui -> {
            ui.beforeClientResponse(this, context -> {
                ui.getPage().executeJs(
                        "const container = document.querySelector('.google-search-view');" +
                                "if (container) { container.scrollTo({top: 0, behavior: 'smooth'}); }" +
                                "window.scrollTo({top: 0, behavior: 'smooth'});"
                );
            });
        });
    }

    private Button createChatbotFab() {
        Button fab = new Button(new Icon(VaadinIcon.CHAT));
        fab.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        fab.getStyle()
                .set("position", "fixed")
                .set("bottom", "24px")
                .set("right", "24px")
                .set("border-radius", "50%")
                .set("width", "56px")
                .set("height", "56px")
                .set("box-shadow", "0 4px 8px rgba(0,0,0,0.2)")
                .set("z-index", "1000")
                .set("cursor", "pointer");

        fab.addClickListener(e -> toggleChatbot());
        return fab;
    }

    private Div createChatbotWindow(Component chatContainer, Component inputLayout) {
        Div window = new Div();
        window.getStyle()
                .set("position", "fixed")
                .set("bottom", "24px")
                .set("right", "24px")
                .set("width", "400px")
                .set("height", "600px")
                .set("background-color", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 8px 24px rgba(0,0,0,0.15)")
                .set("display", "none")
                .set("flex-direction", "column")
                .set("z-index", "999")
                .set("resize", "both")
                .set("overflow", "hidden")
                .set("min-width", "300px")
                .set("min-height", "400px")
                .set("max-width", "600px")
                .set("max-height", "80vh");

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.getStyle()
                .set("background-color", "var(--lumo-primary-color)")
                .set("color", "white")
                .set("border-radius", "12px 12px 0 0");

        H3 headerTitle = new H3("💬 Assistente AI");
        headerTitle.getStyle()
                .set("margin", "0")
                .set("color", "white")
                .set("font-size", "18px");

        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_CONTRAST);
        closeButton.getStyle().set("color", "white");
        closeButton.addClickListener(e -> toggleChatbot());

        header.add(headerTitle, closeButton);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(headerTitle);

        VerticalLayout content = new VerticalLayout(chatContainer, inputLayout);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.expand(chatContainer);

        window.add(header, content);
        return window;
    }

    private void toggleChatbot() {
        isChatOpen = !isChatOpen;
        if (isChatOpen) {
            chatbotWindow.getStyle().set("display", "flex");
            chatbotFab.getStyle().set("display", "none");
            messageInput.focus();
        } else {
            chatbotWindow.getStyle().set("display", "none");
            chatbotFab.getStyle().set("display", "block");
        }
    }

    private void sendMessage() {
        String message = messageInput.getValue().trim();
        if (!message.isEmpty()) {
            addUserMessage(message);
            messageInput.clear();
            messageInput.focus();

            // Show typing indicator
            Div typingIndicator = createTypingIndicator();
            chatContainer.add(typingIndicator);
            scrollToBottom();

            // Simulate 2 seconds delay
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                getUI().ifPresent(ui -> ui.access(() -> {
                    chatContainer.remove(typingIndicator);
                    addBotMessage(AI_RESPONSE);
                }));
            }).start();
        }
    }

    private void addUserMessage(String message) {
        Div messageDiv = createMessageBubble(message, true);
        chatContainer.add(messageDiv);
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        Div messageDiv = createMessageBubble(message, false);
        chatContainer.add(messageDiv);
        scrollToBottom();
    }

    private Div createMessageBubble(String message, boolean isUser) {
        Div bubble = new Div();

        Paragraph messageText = new Paragraph(message);
        messageText.getStyle()
                .set("margin", "0")
                .set("padding", "var(--lumo-space-s)")
                .set("white-space", "pre-wrap")
                .set("word-wrap", "break-word");

        Paragraph timestamp = new Paragraph(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        timestamp.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("text-align", isUser ? "right" : "left");

        Div container = new Div(messageText, timestamp);
        container.getStyle()
                .set("max-width", "70%")
                .set("padding", "var(--lumo-space-m)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("background-color", isUser ? "var(--lumo-primary-color)" : "var(--lumo-contrast-10pct)")
                .set("color", isUser ? "var(--lumo-primary-contrast-color)" : "var(--lumo-body-text-color)");

        if (!isUser) {
            Icon icon = new Icon(VaadinIcon.AUTOMATION);
            icon.setSize("20px");
            icon.getStyle().set("margin-right", "var(--lumo-space-s)");

            HorizontalLayout messageWithIcon = new HorizontalLayout(icon, container);
            messageWithIcon.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.START);
            messageWithIcon.setSpacing(false);
            bubble.add(messageWithIcon);
        } else {
            bubble.add(container);
        }

        bubble.getStyle()
                .set("display", "flex")
                .set("justify-content", isUser ? "flex-end" : "flex-start")
                .set("margin-bottom", "var(--lumo-space-s)");

        return bubble;
    }

    private Div createTypingIndicator() {
        Div indicator = new Div();

        Div dot1 = new Div();
        Div dot2 = new Div();
        Div dot3 = new Div();

        String dotStyle = "width: 8px; height: 8px; margin: 0 2px; background-color: var(--lumo-contrast-50pct); border-radius: 50%; animation: typing 1.4s infinite";

        dot1.getElement().setAttribute("style", dotStyle + "; animation-delay: 0s");
        dot2.getElement().setAttribute("style", dotStyle + "; animation-delay: 0.2s");
        dot3.getElement().setAttribute("style", dotStyle + "; animation-delay: 0.4s");

        Div dotsContainer = new Div(dot1, dot2, dot3);
        dotsContainer.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("padding", "var(--lumo-space-m)")
                .set("background-color", "var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("max-width", "70px");

        Icon icon = new Icon(VaadinIcon.AUTOMATION);
        icon.setSize("20px");
        icon.getStyle().set("margin-right", "var(--lumo-space-s)");

        HorizontalLayout layout = new HorizontalLayout(icon, dotsContainer);
        layout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        layout.setSpacing(false);

        indicator.add(layout);
        indicator.getStyle()
                .set("display", "flex")
                .set("justify-content", "flex-start")
                .set("margin-bottom", "var(--lumo-space-s)");

        // Add CSS animation
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                "if (!document.getElementById('typing-animation')) {" +
                        "  const style = document.createElement('style');" +
                        "  style.id = 'typing-animation';" +
                        "  style.innerHTML = '@keyframes typing { 0%, 60%, 100% { opacity: 0.3; transform: translateY(0); } 30% { opacity: 1; transform: translateY(-5px); } }';" +
                        "  document.head.appendChild(style);" +
                        "}"
        ));

        return indicator;
    }

    private void scrollToBottom() {
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                "const container = $0; container.scrollTop = container.scrollHeight;",
                chatContainer.getElement()
        ));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Add resize observer for chatbot window
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                "const observer = new ResizeObserver(entries => {" +
                        "  for (let entry of entries) {" +
                        "    const chatContainer = entry.target.querySelector('.v-vertical-layout');" +
                        "    if (chatContainer) {" +
                        "      chatContainer.scrollTop = chatContainer.scrollHeight;" +
                        "    }" +
                        "  }" +
                        "});" +
                        "observer.observe($0);",
                chatbotWindow.getElement()
        ));
    }

    private static class SearchResult {
        String title;
        String url;
        String description;
        String site;
        List<FileAttachment> files;
        String videoUrl;
        LocalDate lastUpdated;

        SearchResult(String title, String url, String description, String site,
                     List<FileAttachment> files, String videoUrl, LocalDate lastUpdated) {
            this.title = title;
            this.url = url;
            this.description = description;
            this.site = site;
            this.files = files;
            this.videoUrl = videoUrl;
            this.lastUpdated = lastUpdated;
        }
    }

    private static class FileAttachment {
        String name;
        String type;
        String size;

        FileAttachment(String name, String type, String size) {
            this.name = name;
            this.type = type;
            this.size = size;
        }
    }
}