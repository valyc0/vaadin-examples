# Gestione File Avanzata - Documentazione

## 📋 Panoramica

Sistema avanzato di gestione file con supporto per:
- **Trascrizioni** di contenuti audio/video
- **Traduzioni** in diverse lingue
- **E-tag** per il controllo delle versioni
- **Nome univoco** per l'identificazione sicura dei file
- **Visualizzazione elegante** con componenti Vaadin Details

## 🆕 Nuovi Campi Aggiunti

### 1. Trascrizione (`trascrizione`)
- **Tipo**: TEXT (CLOB)
- **Scopo**: Memorizza la trascrizione completa di file audio/video
- **Caratteristiche**: 
  - Testo potenzialmente molto lungo (migliaia di caratteri)
  - Visualizzazione con anteprima (primi 500 caratteri)
  - Download del testo completo in formato .txt

### 2. Traduzione (`traduzione`)
- **Tipo**: TEXT (CLOB)
- **Scopo**: Memorizza la traduzione della trascrizione in altra lingua
- **Caratteristiche**:
  - Testo potenzialmente molto lungo
  - Visualizzazione con anteprima
  - Download del testo completo

### 3. E-Tag (`etag`)
- **Tipo**: VARCHAR
- **Scopo**: Identificatore univoco per il controllo delle versioni
- **Esempio**: `e-a3f2c1b9d4e5f6g7`
- **Uso**: Utile per caching e sincronizzazione

### 4. Nome File Univoco (`uniqueFileName`)
- **Tipo**: VARCHAR
- **Scopo**: Nome univoco generato dal sistema per evitare collisioni
- **Esempio**: `550e8400-e29b-41d4-a716-446655440000.pdf`
- **Uso**: Riferimento sicuro per storage e retrieval

## 🎨 Nuova Vista: FileDetailView

### URL di Accesso
```
http://localhost:8080/file-details
```

### Caratteristiche Principali

#### 1. **Grid di Selezione Compatta**
- Visualizzazione lista file con icone tipizzate
- Selezione singola per visualizzare i dettagli
- Colonne: Nome File, Dimensione, Data Upload, Caricato da

#### 2. **Pannello Dettagli con Components Details**
Quando si seleziona un file, vengono mostrati:

##### ℹ️ Informazioni Base (sempre aperto)
- Nome file, dimensione, tipo MIME
- Proprietario e data di upload
- Categoria con badge colorato
- Status con badge colorato
- Descrizione

##### ⚙️ Metadati Tecnici (espandibile)
- ID database
- Nome univoco generato
- E-Tag per versioning
- Metadata aggiuntivi
- Dimensione blob

##### 📝 Trascrizione (espandibile, se presente)
- Anteprima primi 500 caratteri
- Indicatore lunghezza totale
- **Pulsante download** per testo completo
- Visualizzazione in font monospace
- Area scrollabile con max-height

##### 🌐 Traduzione (espandibile, se presente)
- Anteprima primi 500 caratteri
- Indicatore lunghezza totale
- **Pulsante download** per testo completo
- Visualizzazione in font monospace
- Area scrollabile con max-height

#### 3. **Design Pattern Utilizzati**

##### Vaadin Details Component
- **Perché Details invece di Accordion?**
  - Accordion: solo un pannello aperto alla volta
  - Details: multipli pannelli aperti simultaneamente
  - Migliore per visualizzare informazioni correlate
  
- **Theme Variants**:
  - `DetailsVariant.FILLED`: Bordi visibili per separazione chiara
  - Migliora la leggibilità e l'organizzazione visiva

##### Card Pattern
- Utilizzo di card styling per il pannello principale
- Bordi, ombre e spaziatura per gerarchia visiva

##### Download Pattern
- StreamResource per generazione on-demand
- Anchor con attributo `download`
- Notifiche di feedback all'utente

## 🚀 Come Testare

### 1. Generare Dati di Esempio

Il sistema include un generatore di dati di esempio che crea:
- 4 file di test
- Testi lunghi per trascrizione (3000+ caratteri)
- Testi lunghi per traduzione (3000+ caratteri)
- Diversi scenari (con/senza trascrizione, con/senza traduzione)

**Avviare con dati di esempio:**

```bash
cd /home/valyc-pc/lavoro/vaadin-examples

# Opzione 1: Via parametro JVM
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=sample-data"

# Opzione 2: Via environment variable
export SPRING_PROFILES_ACTIVE=sample-data
mvn spring-boot:run

# Opzione 3: Modificare application.properties
echo "spring.profiles.active=sample-data" >> src/main/resources/application.properties
mvn spring-boot:run
```

### 2. Accedere alla Vista

1. Avviare l'applicazione
2. Navigare a: `http://localhost:8080/file-details`
3. Selezionare un file dalla grid
4. Espandere i pannelli Details per vedere i dettagli
5. Testare i download di trascrizione e traduzione

### 3. Dati di Test Generati

Il generatore crea questi file:

| File | Trascrizione | Traduzione | Descrizione |
|------|--------------|------------|-------------|
| `meeting_recording_2024.mp4` | ✅ Sì (lungo) | ✅ Sì (lungo) | Video di riunione con trascrizione e traduzione complete |
| `interview_client_abc.mp3` | ✅ Sì (medio) | ❌ No | Audio intervista con solo trascrizione |
| `quarterly_report_Q4.pdf` | ❌ No | ❌ No | Documento senza trascrizione/traduzione |
| `product_demo_2024.pptx` | ✅ Sì (corto) | ✅ Sì (corto) | Presentazione con note brevi |

## 🔧 Integrazione con Server Esterno

### Scenario Reale: Dati dal Server

Nel caso reale, i campi `trascrizione` e `traduzione` verranno popolati da un servizio esterno:

```java
// Esempio di integrazione
@Service
public class TranscriptionService {
    
    @Autowired
    private RestClient restClient;
    
    public void processFileWithTranscription(FileUpload file) {
        // 1. Inviare file al servizio di trascrizione
        TranscriptionResponse response = restClient.post()
            .uri("https://api.transcription-service.com/transcribe")
            .body(file.getFileData())
            .retrieve()
            .body(TranscriptionResponse.class);
        
        // 2. Salvare trascrizione e metadati
        file.setTrascrizione(response.getTranscription());
        file.setTraduzione(response.getTranslation());
        file.setEtag(response.getEtag());
        file.setUniqueFileName(response.getUniqueId());
        
        // 3. Persistere nel database
        fileUploadService.save(file);
    }
}
```

## 📊 Componenti Vaadin Utilizzati

### Details
- **Documentazione**: https://vaadin.com/docs/latest/components/details
- **Pro**: Espandibile/collassabile, accessibile, multipli aperti contemporaneamente
- **Uso**: Organizzazione gerarchica delle informazioni

### Grid
- **Documentazione**: https://vaadin.com/docs/latest/components/grid
- **Pro**: Performance, sorting, filtering
- **Uso**: Lista file con selezione

### Button + Anchor (Download)
- **Pattern**: StreamResource con Anchor
- **Pro**: Download nativo browser, nessun round-trip server
- **Uso**: Download testi lunghi

### Icon (VaadinIcon)
- **Documentazione**: https://vaadin.com/docs/latest/components/icons
- **Pro**: 600+ icone integrate, consistenza visiva
- **Uso**: Identificazione visiva tipo file

## 🎯 Best Practices Implementate

### 1. Performance
- ✅ Anteprima limitata (primi 500 caratteri)
- ✅ Download on-demand (non rendering completo)
- ✅ Lazy loading dei dettagli (solo quando selezionato)

### 2. UX/UI
- ✅ Feedback visivo immediato (notifiche)
- ✅ Indicatori di lunghezza testo
- ✅ Icone contestuali per tipo file
- ✅ Badge colorati per status
- ✅ Area scrollabile per contenuti lunghi

### 3. Accessibilità
- ✅ Componenti Vaadin nativamente accessibili
- ✅ Screen reader friendly (Details component)
- ✅ Keyboard navigation supportata

### 4. Manutenibilità
- ✅ Codice modulare con metodi privati
- ✅ Costanti per configurazione
- ✅ Commenti descrittivi
- ✅ Pattern riutilizzabili

## 🔄 Migrazione Database

Se il database esiste già, eseguire la migrazione:

```sql
-- Aggiungere nuove colonne
ALTER TABLE file_uploads ADD COLUMN trascrizione TEXT;
ALTER TABLE file_uploads ADD COLUMN traduzione TEXT;
ALTER TABLE file_uploads ADD COLUMN etag VARCHAR(255);
ALTER TABLE file_uploads ADD COLUMN unique_file_name VARCHAR(255);

-- Opzionale: Aggiungere indici per performance
CREATE INDEX idx_file_uploads_etag ON file_uploads(etag);
CREATE INDEX idx_file_uploads_unique_name ON file_uploads(unique_file_name);
```

## 📝 TODO Futuri

- [ ] Implementare download in formati multipli (TXT, PDF, DOCX)
- [ ] Aggiungere syntax highlighting per trascrizioni tecniche
- [ ] Implementare search all'interno delle trascrizioni
- [ ] Aggiungere timeline per visualizzare trascrizioni video sincronizzate
- [ ] Implementare confronto side-by-side trascrizione/traduzione
- [ ] Aggiungere export batch di trascrizioni multiple

## 🐛 Troubleshooting

### Problema: Dati di esempio non generati
**Soluzione**: Verificare che il profilo `sample-data` sia attivo:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=sample-data"
```

### Problema: Pannello dettagli non si apre
**Soluzione**: Verificare che il file sia selezionato nella grid (highlight visibile)

### Problema: Download non funziona
**Soluzione**: Verificare i permessi del browser per i download automatici

## 📚 Riferimenti

- [Vaadin Components Documentation](https://vaadin.com/docs/latest/components)
- [Details Component](https://vaadin.com/docs/latest/components/details)
- [Accordion Component](https://vaadin.com/docs/latest/components/accordion)
- [Card Component](https://vaadin.com/docs/latest/components/card)
- [Grid Component](https://vaadin.com/docs/latest/components/grid)

---

**Versione**: 1.0  
**Data**: Novembre 2024  
**Autore**: Sistema di Gestione File Avanzata
