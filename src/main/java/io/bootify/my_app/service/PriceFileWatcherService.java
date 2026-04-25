package io.bootify.my_app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Servizio che monitora il file data/iphone14pro_price.txt con tecnica push:
 * ogni secondo controlla se il file è stato modificato e, in caso, notifica
 * tutte le view Vaadin registrate tramite ui.access() (server push).
 */
@Service
public class PriceFileWatcherService {

    private static final Logger log = LoggerFactory.getLogger(PriceFileWatcherService.class);
    private static final Path PRICE_FILE = Paths.get("data/iphone14pro_price.txt");

    private final List<Consumer<BigDecimal>> listeners = new CopyOnWriteArrayList<>();

    private volatile BigDecimal lastKnownPrice = null;
    private volatile long lastModifiedTime = 0L;

    /**
     * Legge il prezzo attuale dal file (usato all'avvio della view).
     */
    public BigDecimal getCurrentPrice() {
        if (lastKnownPrice == null) {
            readAndNotify(); // prima lettura sincrona
        }
        return lastKnownPrice;
    }

    /**
     * Registra una callback che viene invocata (con il nuovo prezzo) ogni volta
     * che il file cambia. Ogni UI Vaadin registra la propria callback che usa
     * ui.access() per aggiornare il DOM in modo thread-safe.
     */
    public void addListener(Consumer<BigDecimal> listener) {
        listeners.add(listener);
    }

    /**
     * Rimuove la callback quando la view viene staccata (onDetach).
     */
    public void removeListener(Consumer<BigDecimal> listener) {
        listeners.remove(listener);
    }

    /**
     * Schedulato ogni secondo: controlla lastModified del file.
     * Se cambiato, legge il nuovo valore e notifica tutti i listener.
     */
    @Scheduled(fixedDelay = 1000)
    public void checkFileForChanges() {
        if (!Files.exists(PRICE_FILE)) {
            return;
        }
        long currentModified = PRICE_FILE.toFile().lastModified();
        if (currentModified != lastModifiedTime) {
            lastModifiedTime = currentModified;
            readAndNotify();
        }
    }

    private void readAndNotify() {
        try {
            String content = Files.readString(PRICE_FILE).trim();
            BigDecimal newPrice = new BigDecimal(content);
            lastKnownPrice = newPrice;
            log.info("[PriceWatcher] Nuovo prezzo iPhone 14 Pro: {}", newPrice);
            listeners.forEach(listener -> {
                try {
                    listener.accept(newPrice);
                } catch (Exception e) {
                    log.warn("[PriceWatcher] Errore notifica listener: {}", e.getMessage());
                }
            });
        } catch (NumberFormatException e) {
            log.warn("[PriceWatcher] Valore non valido nel file: {}", e.getMessage());
        } catch (Exception e) {
            log.error("[PriceWatcher] Errore lettura file {}: {}", PRICE_FILE, e.getMessage());
        }
    }
}
