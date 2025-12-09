package io.bootify.my_app.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Route(value = "chatbot", layout = MainLayout.class)
@PageTitle("AI Chatbot")
public class ChatbotView extends VerticalLayout {

    private final VerticalLayout chatContainer;
    private final TextField messageInput;
    private static final String AI_RESPONSE = "Ciao! Sono un chatbot di prova. Al momento rispondo sempre con lo stesso messaggio per testing. In futuro potrò essere integrato con una vera AI! 🤖";

    public ChatbotView() {
        setSpacing(false);
        setPadding(false);
        setSizeFull();

        // Header
        H2 title = new H2("💬 AI Chatbot");
        title.getStyle()
                .set("margin", "var(--lumo-space-m)")
                .set("color", "var(--lumo-primary-text-color)");

        // Chat container with scroll
        chatContainer = new VerticalLayout();
        chatContainer.setSpacing(true);
        chatContainer.setPadding(true);
        chatContainer.getStyle()
                .set("flex", "1")
                .set("overflow-y", "auto")
                .set("background-color", "var(--lumo-contrast-5pct)");

        // Welcome message
        addBotMessage("Benvenuto! Sono il tuo assistente AI. Come posso aiutarti oggi?");

        // Input area
        messageInput = new TextField();
        messageInput.setPlaceholder("Scrivi un messaggio...");
        messageInput.setWidthFull();
        messageInput.getStyle().set("flex", "1");

        Button sendButton = new Button("Invia", new Icon(VaadinIcon.PAPERPLANE));
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendButton.addClickListener(e -> sendMessage());

        // Allow Enter key to send message
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

        add(title, chatContainer, inputLayout);
        expand(chatContainer);
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
            messageWithIcon.setDefaultVerticalComponentAlignment(Alignment.START);
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

    private void scrollToBottom() {
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                "const container = $0; container.scrollTop = container.scrollHeight;",
                chatContainer.getElement()
        ));
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
        layout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
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
}
