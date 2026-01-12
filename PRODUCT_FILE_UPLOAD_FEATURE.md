# Product File Upload Feature

## Panoramica

È stata implementata una nuova funzionalità di upload file per i prodotti in ProdottoManagementView. Questa feature permette di:

1. Caricare un file associato a un prodotto esistente
2. Salvare il file localmente nel database
3. Inviare il file a un servizio remoto via RestTemplate usando multipart/form-data

## Componenti Creati

### 1. ProductFileUploadService
**Path:** `src/main/java/io/bootify/my_app/service/ProductFileUploadService.java`

Servizio Spring che gestisce l'upload dei file per i prodotti:

- **uploadFileForProduct(Long productId, MultipartFile file, String uploadedBy)**: 
  - Salva il file nel database associato al prodotto
  - Invia il file al servizio remoto via RestTemplate
  - Gestisce gli errori senza bloccare il salvataggio locale

- **sendToRemoteService(Long productId, MultipartFile file)**:
  - Invia i dati multipart al servizio remoto
  - Include il file e l'ID del prodotto
  - Utilizza RestTemplate configurato con timeout

- **removeFileFromProduct(Long productId)**:
  - Rimuove il file dal prodotto

### 2. ProductFileUploadDialog
**Path:** `src/main/java/io/bootify/my_app/views/ProductFileUploadDialog.java`

Dialog Vaadin per l'interfaccia di upload:

- Upload component con drag & drop
- Validazione del tipo di file (PDF, immagini, documenti Office, txt)
- Limite dimensione file: 10MB
- Feedback visivo sullo stato dell'upload
- Conversione del file in MultipartFile per il service

### 3. RestTemplateConfig
**Path:** `src/main/java/io/bootify/my_app/config/RestTemplateConfig.java`

Configurazione Spring per il RestTemplate:

- Connection timeout: 10 secondi
- Read timeout: 30 secondi
- Bean condiviso nell'applicazione

### 4. Modifiche a ProdottoManagementView

Aggiunto nella colonna azioni:

- **Bottone Upload** (icona VaadinIcon.UPLOAD)
  - Colore verde (LUMO_SUCCESS)
  - Tooltip: "Carica file"
  - Apre il dialog di upload

## Configurazione

### application.yml

Aggiunta configurazione per l'URL del servizio remoto:

```yaml
product:
  upload:
    remote:
      url: ${PRODUCT_UPLOAD_REMOTE_URL:http://localhost:8081/api/upload}
```

L'URL può essere configurato tramite variabile d'ambiente `PRODUCT_UPLOAD_REMOTE_URL`.

## API del Servizio Remoto

Il servizio remoto deve accettare richieste POST con:

**Endpoint:** Configurabile (default: `http://localhost:8081/api/upload`)

**Content-Type:** `multipart/form-data`

**Parametri:**
- `file`: Il file binario (MultipartFile)
- `productId`: L'ID del prodotto (String)

**Risposta attesa:**
- Status 2xx per successo
- Qualsiasi altro status viene considerato errore

### Esempio di Controller Spring per il servizio remoto:

```java
@RestController
@RequestMapping("/api/upload")
public class RemoteUploadController {
    
    @PostMapping
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productId") String productId) {
        
        // Processa il file
        // ...
        
        return ResponseEntity.ok("File uploaded successfully");
    }
}
```

## Flusso di Upload

1. L'utente clicca sul bottone upload (icona verde) nella riga del prodotto
2. Si apre il dialog ProductFileUploadDialog
3. L'utente seleziona o trascina un file
4. L'utente clicca "Carica"
5. Il servizio ProductFileUploadService:
   - Salva il file nel database (Product entity)
   - Invia il file al servizio remoto via RestTemplate
6. Notifica di successo e aggiornamento della grid

## Gestione Errori

- **Errore locale**: Viene mostrato un errore e l'upload fallisce
- **Errore remoto**: Il file viene salvato localmente ma viene loggato l'errore del servizio remoto (non blocca l'operazione)

## Tipi di File Supportati

- PDF (`application/pdf`)
- Immagini: JPEG, PNG, GIF
- Microsoft Word (`.doc`, `.docx`)
- Microsoft Excel (`.xls`, `.xlsx`)
- File di testo (`.txt`)

## Note Tecniche

- Il file viene salvato come BLOB nel database
- I metadati includono: nome file, tipo MIME, dimensione, utente che ha caricato
- L'utente corrente è hardcoded come "admin" (TODO: integrazione con Spring Security)
- Il componente Upload ha auto-upload disabilitato per maggiore controllo
- La grid viene aggiornata automaticamente dopo l'upload

## Dipendenze

Assicurarsi che il `pom.xml` includa:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

Questa dipendenza fornisce RestTemplate e il supporto per multipart/form-data.
