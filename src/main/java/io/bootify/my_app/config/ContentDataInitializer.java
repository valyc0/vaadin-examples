package io.bootify.my_app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.bootify.my_app.domain.Content;
import io.bootify.my_app.repos.ContentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class ContentDataInitializer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public CommandLineRunner initContentDatabase(ContentRepository contentRepository) {
        return args -> {
            // Check if data already exists
            if (contentRepository.count() > 0) {
                return;
            }

            List<Content> contents = Arrays.asList(
                createContent("documento-progetto-2024.pdf", 2458624L, "PDF", "Documenti",
                    "Documento di progetto completo con specifiche tecniche",
                    "hash123456abc", "/uploads/docs/", "application/pdf", "admin",
                    "progetto, specifiche, 2024",
                    createMetadata("Autore", "Mario Rossi", "Versione", "1.2", "Status", "Approvato")),
                
                createContent("presentazione-vendite-Q4.pptx", 8945632L, "DOCUMENT", "Documenti",
                    "Presentazione risultati vendite quarto trimestre",
                    "hash789def012", "/uploads/presentations/", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "sales_team",
                    "vendite, Q4, presentazione",
                    createMetadata("Dipartimento", "Vendite", "Trimestre", "Q4", "Anno", "2024")),
                
                createContent("logo-aziendale.png", 156789L, "IMAGE", "Immagini",
                    "Logo aziendale in alta risoluzione",
                    "hash345ghi678", "/uploads/images/", "image/png", "marketing",
                    "logo, branding, aziendale",
                    createMetadata("Risoluzione", "2048x2048", "Formato", "PNG", "Trasparenza", "Si")),
                
                createContent("video-tutorial-prodotto.mp4", 45678912L, "VIDEO", "Video",
                    "Video tutorial sull'utilizzo del prodotto",
                    "hash901jkl234", "/uploads/videos/", "video/mp4", "support",
                    "tutorial, prodotto, guida",
                    createMetadata("Durata", "15:30", "Qualità", "1080p", "Lingua", "Italiano")),
                
                createContent("database-backup-12-12-2024.zip", 123456789L, "ARCHIVE", "Archivi",
                    "Backup completo database di produzione",
                    "hash567mno890", "/backups/", "application/zip", "sysadmin",
                    "backup, database, produzione",
                    createMetadata("Tipo", "Full Backup", "Compressione", "ZIP", "Encrypted", "Yes")),
                
                createContent("foto-evento-azienda.jpg", 3456789L, "IMAGE", "Immagini",
                    "Fotografia dell'evento aziendale annuale",
                    "hash234pqr567", "/uploads/events/", "image/jpeg", "hr_team",
                    "evento, aziendale, team building",
                    createMetadata("Data Evento", "10/12/2024", "Fotografo", "Studio Luce", "Persone", "85")),
                
                createContent("manuale-utente.pdf", 1234567L, "PDF", "Documenti",
                    "Manuale utente completo del software",
                    "hash678stu901", "/uploads/manuals/", "application/pdf", "tech_writer",
                    "manuale, software, utente",
                    createMetadata("Versione Software", "3.5", "Pagine", "124", "Lingua", "Italiano")),
                
                createContent("registrazione-webinar.mp4", 89456123L, "VIDEO", "Video",
                    "Registrazione webinar formazione prodotto",
                    "hash345vwx678", "/uploads/webinars/", "video/mp4", "training",
                    "webinar, formazione, registrazione",
                    createMetadata("Relatore", "Luca Bianchi", "Partecipanti", "156", "Data", "05/12/2024")),
                
                createContent("contratto-fornitore.pdf", 567890L, "PDF", "Documenti",
                    "Contratto di fornitura servizi IT",
                    "hash901yzA234", "/uploads/contracts/", "application/pdf", "legal",
                    "contratto, fornitore, legale",
                    createMetadata("Fornitore", "Tech Solutions Srl", "Validità", "2024-2026", "Confidenziale", "Si")),
                
                createContent("campagna-marketing.zip", 23456789L, "ARCHIVE", "Archivi",
                    "Materiali completi campagna marketing Q1",
                    "hashBCD567efg", "/uploads/marketing/", "application/zip", "marketing",
                    "marketing, campagna, materiali",
                    createMetadata("Campagna", "Spring Launch", "Budget", "50k EUR", "Canali", "Digital+Print")),
                
                createContent("screenshot-bug-report.png", 234567L, "IMAGE", "Immagini",
                    "Screenshot per segnalazione bug applicazione",
                    "hash890HIJ123", "/uploads/bugs/", "image/png", "developer",
                    "bug, screenshot, segnalazione",
                    createMetadata("Bug ID", "BUG-2024-456", "Priorità", "Alta", "Status", "In Progress")),
                
                createContent("podcast-intervista-ceo.mp3", 12345678L, "AUDIO", "Audio",
                    "Podcast intervista al CEO aziendale",
                    "hashKLM456nop", "/uploads/audio/", "audio/mpeg", "communications",
                    "podcast, intervista, CEO",
                    createMetadata("Durata", "45:20", "Interviewer", "Radio Business", "Data", "01/12/2024")),
                
                createContent("template-fattura.docx", 89123L, "DOCUMENT", "Documenti",
                    "Template documento per emissione fatture",
                    "hashQRS789tuv", "/uploads/templates/", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "accounting",
                    "template, fattura, documento",
                    createMetadata("Versione", "2.0", "Ultimo Aggiornamento", "15/11/2024", "Tipo", "Template")),
                
                createContent("infografica-statistiche.svg", 456789L, "IMAGE", "Immagini",
                    "Infografica con statistiche aziendali 2024",
                    "hashWXY012zab", "/uploads/infographics/", "image/svg+xml", "analyst",
                    "infografica, statistiche, dati",
                    createMetadata("Anno", "2024", "Designer", "Creative Studio", "Formato", "Vettoriale")),
                
                createContent("codice-sorgente-modulo.zip", 5678901L, "ARCHIVE", "Archivi",
                    "Codice sorgente modulo autenticazione",
                    "hashCDE345fgh", "/repos/modules/", "application/zip", "developer",
                    "codice, sorgente, modulo",
                    createMetadata("Linguaggio", "Java", "Framework", "Spring Boot", "Version", "1.0.0")),
                
                createContent("catalogo-prodotti-2024.pdf", 12345678L, "PDF", "Documenti",
                    "Catalogo completo prodotti per l'anno 2024",
                    "hashIJK678lmn", "/uploads/catalogs/", "application/pdf", "product_manager",
                    "catalogo, prodotti, 2024",
                    createMetadata("Prodotti", "340", "Pagine", "180", "Formato", "A4")),
                
                createContent("video-onboarding-dipendenti.mp4", 34567890L, "VIDEO", "Video",
                    "Video introduttivo per nuovi dipendenti",
                    "hashOPQ901rst", "/uploads/hr/", "video/mp4", "hr_team",
                    "onboarding, dipendenti, introduzione",
                    createMetadata("Durata", "22:15", "Ultimo Update", "10/11/2024", "Lingua", "Italiano")),
                
                createContent("rapporto-finanziario-annuale.xlsx", 2345678L, "DOCUMENT", "Documenti",
                    "Rapporto finanziario completo anno fiscale 2024",
                    "hashUVW234xyz", "/uploads/finance/", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "cfo",
                    "finanziario, rapporto, annuale",
                    createMetadata("Anno Fiscale", "2024", "Fogli", "15", "Confidenziale", "Si")),
                
                createContent("sfondo-desktop-aziendale.jpg", 4567890L, "IMAGE", "Immagini",
                    "Sfondo desktop con branding aziendale",
                    "hashABC567def", "/uploads/wallpapers/", "image/jpeg", "it_support",
                    "sfondo, desktop, branding",
                    createMetadata("Risoluzione", "3840x2160", "Formato", "4K", "Uso", "Aziendale")),
                
                createContent("guida-policy-sicurezza.pdf", 1567890L, "PDF", "Documenti",
                    "Guida completa policy di sicurezza informatica",
                    "hashGHI890jkl", "/uploads/security/", "application/pdf", "security_officer",
                    "sicurezza, policy, guida",
                    createMetadata("Versione", "3.1", "Approvazione", "CDA 20/11/2024", "Obbligatoria", "Si"))
            );

            contentRepository.saveAll(contents);
            System.out.println("✅ Inizializzati " + contents.size() + " contenuti nel database");
        };
    }

    private Content createContent(String fileName, Long fileSize, String fileType, String category,
                                  String description, String fileHash, String originalPath, 
                                  String mimeType, String uploadUser, String tags,
                                  String customMetadata) {
        Content content = new Content();
        content.setFileName(fileName);
        content.setFileSize(fileSize);
        content.setFileType(fileType);
        content.setCategory(category);
        content.setDescription(description);
        content.setFileHash(fileHash);
        content.setOriginalPath(originalPath);
        content.setMimeType(mimeType);
        content.setUploadUser(uploadUser);
        content.setTags(tags);
        content.setCustomMetadata(customMetadata);
        content.setCreationDate(LocalDateTime.now().minusDays((long)(Math.random() * 30)));
        content.setLastModified(content.getCreationDate());
        return content;
    }

    private String createMetadata(String... keyValues) {
        try {
            Map<String, String> metadata = new HashMap<>();
            for (int i = 0; i < keyValues.length; i += 2) {
                if (i + 1 < keyValues.length) {
                    metadata.put(keyValues[i], keyValues[i + 1]);
                }
            }
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            return "{}";
        }
    }
}
