package io.bootify.my_app.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulates a streaming AI remote call via Flux.
 * Each word (and space) is emitted individually with a small delay,
 * mimicking a real token-by-token LLM response stream.
 */
@Service
public class AiStreamingService {

    private static final Duration TOKEN_DELAY = Duration.ofMillis(80);

    /**
     * Streams an AI response about a specific document (Markdown format).
     */
    public Flux<String> streamDocumentResponse(String documentTitle, String question) {
        String fullResponse =
                "Ho analizzato il documento **\"" + documentTitle + "\"**.\n\n" +
                "Per la domanda *\"" + question + "\"*, ecco cosa ho trovato:\n\n" +
                "- La sezione principale tratta il tema in modo **approfondito**\n" +
                "- Sono presenti riferimenti a studi recenti e pratiche consolidate\n" +
                "- Il documento include esempi pratici e casi d'uso\n\n" +
                "> Ti consiglio di consultare la sezione dedicata per maggiori dettagli.\n\n" +
                "Se hai altre domande, sono a disposizione! ✅";
        return tokenize(fullResponse);
    }

    /**
     * Streams a generic AI response (Markdown format).
     */
    public Flux<String> streamGenericResponse(String question) {
        String fullResponse =
                "Ottima domanda su: *\"" + question + "\"*\n\n" +
                "Come assistente virtuale posso aiutarti con:\n\n" +
                "1. **Informazioni generali** su qualsiasi argomento\n" +
                "2. **Analisi di documenti** presenti nei risultati\n" +
                "3. **Risposte contestuali** basate sul contenuto\n\n" +
                "Per domande specifiche su un documento, apri il dettaglio di un risultato di ricerca! 🤖";
        return tokenize(fullResponse);
    }

    /**
     * Splits text into word/space tokens and emits them one at a time
     * with TOKEN_DELAY between each, simulating a Flux remote stream.
     */
    private Flux<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        StringBuilder word = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c == ' ') {
                if (word.length() > 0) {
                    tokens.add(word.toString());
                    word.setLength(0);
                }
                tokens.add(" ");
            } else {
                word.append(c);
            }
        }
        if (word.length() > 0) {
            tokens.add(word.toString());
        }

        return Flux.interval(TOKEN_DELAY)
                .take(tokens.size())
                .map(i -> tokens.get((int) (long) i));
    }
}
