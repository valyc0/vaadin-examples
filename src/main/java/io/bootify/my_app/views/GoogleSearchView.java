package io.bootify.my_app.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Route(value = "google-search", layout = MainLayout.class)
@PageTitle("Google Search")
public class GoogleSearchView extends Div {

    private final VerticalLayout searchResultsContainer;
    private final Div chatbotWindow;
    private final VerticalLayout chatContainer;
    private final TextField messageInput;
    private final Button chatbotFab;
    private boolean isChatOpen = false;
    private static final String AI_RESPONSE = "Ciao! Sono un assistente virtuale. Posso aiutarti con qualsiasi domanda! 🤖";

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

        // Search results
        searchResultsContainer = new VerticalLayout();
        searchResultsContainer.setPadding(true);
        searchResultsContainer.setSpacing(true);
        searchResultsContainer.getStyle()
                .set("max-width", "700px")
                .set("margin", "0 auto")
                .set("width", "100%");

        // Add simulated results
        addSimulatedResults();

        mainContent.add(searchResultsContainer);
        add(mainContent);

        // Chatbot FAB (Floating Action Button)
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
                .set("padding", "20px 40px");

        // Google-style logo
        H2 logo = new H2("Google");
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
                searchResultsContainer.removeAll();
                addSimulatedResults(query);
            }
        });

        searchField.addKeyPressListener(event -> {
            if (event.getKey().getKeys().contains("Enter")) {
                String query = searchField.getValue();
                if (!query.isEmpty()) {
                    searchResultsContainer.removeAll();
                    addSimulatedResults(query);
                }
            }
        });

        HorizontalLayout searchBar = new HorizontalLayout(searchField, searchButton);
        searchBar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        searchBar.setWidthFull();
        searchBar.getStyle().set("max-width", "650px");

        header.add(logo, searchBar);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(searchBar);

        return header;
    }

    private void addSimulatedResults() {
        addSimulatedResults("java programming");
    }

    private void addSimulatedResults(String query) {
        Paragraph resultsInfo = new Paragraph("Circa 1.250.000 risultati (0,45 secondi)");
        resultsInfo.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "14px")
                .set("margin-top", "10px");
        searchResultsContainer.add(resultsInfo);

        List<SearchResult> results = generateSearchResults(query);
        for (SearchResult result : results) {
            searchResultsContainer.add(createSearchResultCard(result));
        }
    }

    private List<SearchResult> generateSearchResults(String query) {
        List<SearchResult> results = new ArrayList<>();
        
        results.add(new SearchResult(
                "Java Programming - Official Documentation",
                "https://docs.oracle.com/javase/tutorial/",
                "The Java™ Tutorials are practical guides for programmers who want to use the Java programming language to create applications. They include hundreds of complete, working examples...",
                "Oracle"
        ));

        results.add(new SearchResult(
                "Learn " + query + " - Step by Step Guide",
                "https://www.example.com/guide",
                "Comprehensive guide to " + query + ". Start from basics and advance to expert level. Updated with latest features and best practices for 2025.",
                "Example Learning Platform"
        ));

        results.add(new SearchResult(
                query + " Tutorial for Beginners",
                "https://www.tutorial.com/" + query.replace(" ", "-"),
                "Free tutorial covering all aspects of " + query + ". Includes video lessons, code examples, and practical exercises. Perfect for beginners.",
                "Tutorial.com"
        ));

        results.add(new SearchResult(
                "Stack Overflow - " + query + " Questions",
                "https://stackoverflow.com/questions/tagged/" + query.replace(" ", "+"),
                "Browse thousands of questions and answers about " + query + ". Get help from the community and learn from real-world problems.",
                "Stack Overflow"
        ));

        results.add(new SearchResult(
                "GitHub - " + query + " Projects",
                "https://github.com/topics/" + query.replace(" ", "-"),
                "Explore open source " + query + " projects on GitHub. Browse repositories, contribute to projects, and learn from other developers' code.",
                "GitHub"
        ));

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
        Paragraph url = new Paragraph(result.site + " › " + result.url);
        url.getStyle()
                .set("margin", "0 0 4px 0")
                .set("font-size", "14px")
                .set("color", "var(--lumo-secondary-text-color)");

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

        card.add(url, title, description);
        return card;
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

        SearchResult(String title, String url, String description, String site) {
            this.title = title;
            this.url = url;
            this.description = description;
            this.site = site;
        }
    }
}
