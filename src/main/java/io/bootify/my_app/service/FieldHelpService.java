package io.bootify.my_app.service;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.bootify.my_app.config.FieldHelpProperties;
import org.springframework.stereotype.Service;

/**
 * Servizio generico per aggiungere un'icona "?" cliccabile in alto a destra
 * su qualsiasi componente Vaadin (campo, panel, card, sezione, ecc.).
 * I testi sono configurati in field-help.yml.
 */
@Service
public class FieldHelpService {

    private final FieldHelpProperties properties;

    public FieldHelpService(FieldHelpProperties properties) {
        this.properties = properties;
    }

    public String getHelp(String viewKey, String fieldKey) {
        return properties.getHelp(viewKey, fieldKey);
    }

    /**
     * Avvolge il componente in un Div con position:relative e aggiunge
     * un piccolo bottone "?" posizionato in alto a destra (position:absolute).
     * Al click apre un dialog con la spiegazione da field-help.yml.
     * Funziona su qualsiasi componente: TextField, ComboBox, TextArea, layout, ecc.
     */
    public Div wrapWithHelp(Component inner, String viewKey, String fieldKey) {
        Div wrapper = new Div(inner);
        wrapper.getStyle()
                .set("position", "relative")
                .set("width", "100%");

        Button btn = createHelpButton(viewKey, fieldKey);
        if (btn != null) {
            btn.getStyle()
                    .set("position", "absolute")
                    .set("top", "2px")
                    .set("right", "2px")
                    .set("z-index", "1")
                    .set("min-width", "20px")
                    .set("width", "20px")
                    .set("height", "20px")
                    .set("padding", "0");
            wrapper.add(btn);
        }
        return wrapper;
    }

    /**
     * Crea e restituisce il bottone "?" standalone.
     * Restituisce null se non c'e' testo configurato per quella chiave.
     */
    public Button createHelpButton(String viewKey, String fieldKey) {
        String help = getHelp(viewKey, fieldKey);
        if (help == null || help.isBlank()) {
            return null;
        }
        Button btn = new Button(VaadinIcon.QUESTION_CIRCLE_O.create());
        btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ICON);
        btn.getStyle()
                .set("color", "var(--lumo-primary-color)")
                .set("cursor", "pointer")
                .set("font-size", "13px");
        btn.setAriaLabel("Aiuto per " + fieldKey);
        btn.addClickListener(e -> openHelpDialog(help));
        return btn;
    }

    private void openHelpDialog(String help) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("\u2139\ufe0f Informazione sul campo");

        Paragraph text = new Paragraph(help);
        text.getStyle()
                .set("margin", "0")
                .set("line-height", "1.6");
        dialog.add(text);

        Button closeBtn = new Button("Chiudi", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(closeBtn);

        dialog.setWidth("380px");
        dialog.open();
    }
}
