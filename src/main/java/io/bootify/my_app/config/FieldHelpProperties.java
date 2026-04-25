package io.bootify.my_app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Legge le descrizioni di aiuto per i campi da field-help.yml.
 *
 * Struttura YAML attesa:
 * <pre>
 * field-help:
 *   views:
 *     <chiave-view>:
 *       <chiave-campo>: "Testo di aiuto"
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "field-help")
public class FieldHelpProperties {

    /**
     * Mappa: chiave-view → (chiave-campo → testo di aiuto).
     * Esempio: views.get("enhanced-file-management").get("status")
     */
    private Map<String, Map<String, String>> views = new HashMap<>();

    public Map<String, Map<String, String>> getViews() {
        return views;
    }

    public void setViews(Map<String, Map<String, String>> views) {
        this.views = views;
    }

    /**
     * Restituisce il testo di aiuto per una view e un campo specifici.
     * Ritorna stringa vuota se non trovato.
     */
    public String getHelp(String viewKey, String fieldKey) {
        Map<String, String> fields = views.getOrDefault(viewKey, Collections.emptyMap());
        return fields.getOrDefault(fieldKey, "");
    }
}
