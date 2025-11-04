package io.bootify.my_app.util;

import io.bootify.my_app.domain.FileUpload;
import io.bootify.my_app.service.FileUploadService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Generatore di dati di esempio per testare la visualizzazione
 * dei campi trascrizione e traduzione
 * 
 * Attivare con il profilo: --spring.profiles.active=sample-data
 */
@Configuration
@Profile("sample-data")
public class SampleDataGenerator {

    @Bean
    CommandLineRunner generateSampleData(FileUploadService fileUploadService) {
        return args -> {
            System.out.println("🎲 Generazione dati di esempio...");
            
            // Sample 1: Video con trascrizione e traduzione
            FileUpload video = new FileUpload();
            video.setFileName("meeting_recording_2024.mp4");
            video.setFileType("video/mp4");
            video.setFileSize(15728640L); // 15 MB
            video.setUploadedBy("Mario Rossi");
            video.setUploadDate(LocalDateTime.now().minusDays(2));
            video.setCategory("Video");
            video.setStatus("APPROVED");
            video.setDescription("Registrazione della riunione mensile del team");
            video.setUniqueFileName(UUID.randomUUID().toString() + ".mp4");
            video.setEtag("e-" + UUID.randomUUID().toString().substring(0, 16));
            video.setFileData(new byte[]{1, 2, 3}); // Dummy data
            
            // Trascrizione lunga
            video.setTrascrizione(generateLongTranscription());
            
            // Traduzione lunga
            video.setTraduzione(generateLongTranslation());
            
            fileUploadService.save(video);
            
            // Sample 2: Audio con solo trascrizione
            FileUpload audio = new FileUpload();
            audio.setFileName("interview_client_abc.mp3");
            audio.setFileType("audio/mp3");
            audio.setFileSize(5242880L); // 5 MB
            audio.setUploadedBy("Giulia Bianchi");
            audio.setUploadDate(LocalDateTime.now().minusDays(5));
            audio.setCategory("Audio");
            audio.setStatus("PENDING");
            audio.setDescription("Intervista con il cliente ABC per il nuovo progetto");
            audio.setUniqueFileName(UUID.randomUUID().toString() + ".mp3");
            audio.setEtag("e-" + UUID.randomUUID().toString().substring(0, 16));
            audio.setFileData(new byte[]{1, 2, 3}); // Dummy data
            
            audio.setTrascrizione(generateMediumTranscription());
            // Nessuna traduzione per questo
            
            fileUploadService.save(audio);
            
            // Sample 3: Documento senza trascrizione/traduzione
            FileUpload doc = new FileUpload();
            doc.setFileName("quarterly_report_Q4.pdf");
            doc.setFileType("application/pdf");
            doc.setFileSize(2097152L); // 2 MB
            doc.setUploadedBy("Luca Verdi");
            doc.setUploadDate(LocalDateTime.now().minusDays(1));
            doc.setCategory("Report");
            doc.setStatus("APPROVED");
            doc.setDescription("Report trimestrale Q4 2024");
            doc.setUniqueFileName(UUID.randomUUID().toString() + ".pdf");
            doc.setEtag("e-" + UUID.randomUUID().toString().substring(0, 16));
            doc.setFileData(new byte[]{1, 2, 3}); // Dummy data
            
            // Nessuna trascrizione o traduzione
            
            fileUploadService.save(doc);
            
            // Sample 4: Presentazione con traduzione
            FileUpload presentation = new FileUpload();
            presentation.setFileName("product_demo_2024.pptx");
            presentation.setFileType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
            presentation.setFileSize(8388608L); // 8 MB
            presentation.setUploadedBy("Anna Neri");
            presentation.setUploadDate(LocalDateTime.now().minusHours(6));
            presentation.setCategory("Presentazioni");
            presentation.setStatus("PENDING");
            presentation.setDescription("Demo del prodotto per il cliente internazionale");
            presentation.setUniqueFileName(UUID.randomUUID().toString() + ".pptx");
            presentation.setEtag("e-" + UUID.randomUUID().toString().substring(0, 16));
            presentation.setFileData(new byte[]{1, 2, 3}); // Dummy data
            
            presentation.setTrascrizione(generateShortTranscription());
            presentation.setTraduzione(generateShortTranslation());
            
            fileUploadService.save(presentation);
            
            System.out.println("✅ Generati 4 file di esempio con dati di test");
        };
    }
    
    private String generateLongTranscription() {
        StringBuilder sb = new StringBuilder();
        sb.append("TRASCRIZIONE COMPLETA - Riunione Mensile Team - 15 Novembre 2024\n\n");
        sb.append("=".repeat(80)).append("\n\n");
        
        sb.append("Partecipanti:\n");
        sb.append("- Mario Rossi (Project Manager)\n");
        sb.append("- Giulia Bianchi (Lead Developer)\n");
        sb.append("- Luca Verdi (Business Analyst)\n");
        sb.append("- Anna Neri (UX Designer)\n");
        sb.append("- Francesco Gialli (QA Engineer)\n\n");
        
        sb.append("Argomenti Discussi:\n\n");
        
        sb.append("1. REVISIONE SPRINT CORRENTE\n");
        sb.append("-".repeat(40)).append("\n");
        sb.append("Mario: Buongiorno a tutti, iniziamo con la revisione dello sprint corrente. ");
        sb.append("Abbiamo completato 23 delle 28 user story pianificate. Giulia, puoi darci un aggiornamento sullo sviluppo?\n\n");
        
        sb.append("Giulia: Certo Mario. Il team di sviluppo ha lavorato principalmente su tre aree chiave. ");
        sb.append("Primo, abbiamo completato l'integrazione del nuovo sistema di autenticazione OAuth2. ");
        sb.append("Questo ci permette ora di supportare login tramite Google, Microsoft e GitHub. ");
        sb.append("Secondo, abbiamo implementato il nuovo modulo di gestione file con supporto per upload streaming, ");
        sb.append("che ci consente di gestire file fino a 50GB senza problemi di memoria. ");
        sb.append("Terzo, abbiamo refactorizzato il layer di persistenza per migliorare le performance del 40%.\n\n");
        
        sb.append("Mario: Ottimo lavoro! E le 5 user story rimanenti?\n\n");
        
        sb.append("Giulia: Tre di queste sono bloccate per dipendenze esterne - stiamo aspettando l'API del sistema CRM. ");
        sb.append("Le altre due richiedono più tempo del previsto per la complessità tecnica emersa durante l'implementazione. ");
        sb.append("Propongo di spostarle al prossimo sprint.\n\n");
        
        sb.append("2. PIANO PER IL PROSSIMO SPRINT\n");
        sb.append("-".repeat(40)).append("\n");
        sb.append("Luca: Per quanto riguarda il prossimo sprint, ho preparato una lista di 25 user story prioritarie. ");
        sb.append("Le ho categorizzate in base al valore business e alla complessità tecnica. ");
        sb.append("Le principali includono: implementazione del dashboard analytics, ");
        sb.append("miglioramenti alla UX del modulo di reporting, e l'integrazione con il nuovo sistema di notifiche push.\n\n");
        
        sb.append("Anna: Dal punto di vista UX, ho alcuni feedback importanti dai test utente effettuati la scorsa settimana. ");
        sb.append("Gli utenti hanno trovato confusionale il flusso di caricamento file multipli. ");
        sb.append("Propongo di semplificare l'interfaccia e aggiungere più feedback visivo durante il processo di upload. ");
        sb.append("Ho preparato dei mockup che vorrei condividere con il team.\n\n");
        
        sb.append("Mario: Perfetto Anna, inviaci i mockup e organizziamo una sessione di design review. ");
        sb.append("Francesco, come sta andando il testing?\n\n");
        
        sb.append("Francesco: Abbiamo identificato 12 bug durante questa sprint, 10 dei quali sono già stati risolti. ");
        sb.append("I due rimanenti sono di priorità media e saranno inclusi nel prossimo sprint. ");
        sb.append("Ho anche aggiornato la test suite automatica con 45 nuovi test case, ");
        sb.append("portando la code coverage al 87%.\n\n");
        
        sb.append("3. DISCUSSIONE ARCHITETTURA\n");
        sb.append("-".repeat(40)).append("\n");
        sb.append("Giulia: Vorrei discutere un tema importante riguardo l'architettura del sistema. ");
        sb.append("Con la crescita del volume di dati, stiamo iniziando a vedere problemi di performance ");
        sb.append("sul database principale. Propongo di implementare una strategia di sharding ");
        sb.append("e di introdurre un layer di caching distribuito con Redis.\n\n");
        
        sb.append("Mario: È una proposta interessante. Quali sono i pro e contro?\n\n");
        
        sb.append("Giulia: Pro: miglioramento significativo delle performance, scalabilità orizzontale, ");
        sb.append("riduzione del carico sul database principale. Contro: aumento della complessità, ");
        sb.append("necessità di gestire la consistenza dei dati, costi infrastrutturali aggiuntivi. ");
        sb.append("Stimo che ci vorranno circa 3 sprint per implementare completamente questa soluzione.\n\n");
        
        sb.append("Luca: Dal punto di vista business, questa migrazione è fondamentale. ");
        sb.append("Abbiamo proiezioni di crescita del 200% nel prossimo trimestre. ");
        sb.append("Senza questi miglioramenti, rischieremmo di non poter gestire il carico.\n\n");
        
        sb.append("4. CONCLUSIONI E PROSSIMI PASSI\n");
        sb.append("-".repeat(40)).append("\n");
        sb.append("Mario: Bene, riassumendo:\n");
        sb.append("- Chiuderemo questo sprint con 23/28 story completate\n");
        sb.append("- Le 5 rimanenti vengono spostate al prossimo sprint\n");
        sb.append("- Prossimo sprint: 25 nuove user story + miglioramenti UX\n");
        sb.append("- Inizieremo la pianificazione della migrazione architetturale\n");
        sb.append("- Design review con Anna giovedì prossimo\n\n");
        
        sb.append("Anna: Vorrei aggiungere che ho bisogno del feedback del team sui mockup ");
        sb.append("entro mercoledì per poter preparare i design finali.\n\n");
        
        sb.append("Mario: Perfetto, tutti d'accordo? Bene, grazie a tutti per la partecipazione. ");
        sb.append("Ci vediamo domani per lo stand-up. Meeting concluso.\n\n");
        
        sb.append("=".repeat(80)).append("\n");
        sb.append("Fine trascrizione - Durata: 45 minuti\n");
        
        return sb.toString();
    }
    
    private String generateLongTranslation() {
        StringBuilder sb = new StringBuilder();
        sb.append("COMPLETE TRANSCRIPTION - Monthly Team Meeting - November 15, 2024\n\n");
        sb.append("=".repeat(80)).append("\n\n");
        
        sb.append("Participants:\n");
        sb.append("- Mario Rossi (Project Manager)\n");
        sb.append("- Giulia Bianchi (Lead Developer)\n");
        sb.append("- Luca Verdi (Business Analyst)\n");
        sb.append("- Anna Neri (UX Designer)\n");
        sb.append("- Francesco Gialli (QA Engineer)\n\n");
        
        sb.append("Topics Discussed:\n\n");
        
        sb.append("1. CURRENT SPRINT REVIEW\n");
        sb.append("-".repeat(40)).append("\n");
        sb.append("Mario: Good morning everyone, let's start with the current sprint review. ");
        sb.append("We completed 23 out of 28 planned user stories. Giulia, can you give us a development update?\n\n");
        
        sb.append("Giulia: Sure Mario. The development team mainly worked on three key areas. ");
        sb.append("First, we completed the integration of the new OAuth2 authentication system. ");
        sb.append("This now allows us to support login via Google, Microsoft and GitHub. ");
        sb.append("Second, we implemented the new file management module with streaming upload support, ");
        sb.append("which allows us to handle files up to 50GB without memory issues. ");
        sb.append("Third, we refactored the persistence layer to improve performance by 40%.\n\n");
        
        sb.append("Mario: Great work! What about the 5 remaining user stories?\n\n");
        
        sb.append("Giulia: Three of these are blocked due to external dependencies - we're waiting for the CRM system API. ");
        sb.append("The other two require more time than expected due to technical complexity that emerged during implementation. ");
        sb.append("I propose moving them to the next sprint.\n\n");
        
        sb.append("2. NEXT SPRINT PLAN\n");
        sb.append("-".repeat(40)).append("\n");
        sb.append("Luca: Regarding the next sprint, I've prepared a list of 25 priority user stories. ");
        sb.append("I've categorized them based on business value and technical complexity. ");
        sb.append("The main ones include: implementation of the analytics dashboard, ");
        sb.append("UX improvements to the reporting module, and integration with the new push notification system.\n\n");
        
        sb.append("Anna: From a UX perspective, I have some important feedback from user tests conducted last week. ");
        sb.append("Users found the multiple file upload flow confusing. ");
        sb.append("I propose simplifying the interface and adding more visual feedback during the upload process. ");
        sb.append("I've prepared mockups that I'd like to share with the team.\n\n");
        
        sb.append("Mario: Perfect Anna, send us the mockups and we'll organize a design review session. ");
        sb.append("Francesco, how is testing going?\n\n");
        
        sb.append("Francesco: We identified 12 bugs during this sprint, 10 of which have already been resolved. ");
        sb.append("The remaining two are medium priority and will be included in the next sprint. ");
        sb.append("I also updated the automated test suite with 45 new test cases, ");
        sb.append("bringing code coverage to 87%.\n\n");
        
        sb.append("3. ARCHITECTURE DISCUSSION\n");
        sb.append("-".repeat(40)).append("\n");
        sb.append("Giulia: I'd like to discuss an important topic regarding system architecture. ");
        sb.append("With the growth in data volume, we're starting to see performance issues ");
        sb.append("on the main database. I propose implementing a sharding strategy ");
        sb.append("and introducing a distributed caching layer with Redis.\n\n");
        
        sb.append("Mario: That's an interesting proposal. What are the pros and cons?\n\n");
        
        sb.append("Giulia: Pros: significant performance improvement, horizontal scalability, ");
        sb.append("reduced load on the main database. Cons: increased complexity, ");
        sb.append("need to manage data consistency, additional infrastructure costs. ");
        sb.append("I estimate it will take about 3 sprints to fully implement this solution.\n\n");
        
        sb.append("Luca: From a business perspective, this migration is fundamental. ");
        sb.append("We have growth projections of 200% in the next quarter. ");
        sb.append("Without these improvements, we risk not being able to handle the load.\n\n");
        
        sb.append("4. CONCLUSIONS AND NEXT STEPS\n");
        sb.append("-".repeat(40)).append("\n");
        sb.append("Mario: Well, to summarize:\n");
        sb.append("- We'll close this sprint with 23/28 stories completed\n");
        sb.append("- The remaining 5 are moved to the next sprint\n");
        sb.append("- Next sprint: 25 new user stories + UX improvements\n");
        sb.append("- We'll start planning the architectural migration\n");
        sb.append("- Design review with Anna next Thursday\n\n");
        
        sb.append("Anna: I'd like to add that I need team feedback on the mockups ");
        sb.append("by Wednesday to prepare the final designs.\n\n");
        
        sb.append("Mario: Perfect, everyone agree? Good, thanks everyone for participating. ");
        sb.append("See you tomorrow for stand-up. Meeting concluded.\n\n");
        
        sb.append("=".repeat(80)).append("\n");
        sb.append("End of transcription - Duration: 45 minutes\n");
        
        return sb.toString();
    }
    
    private String generateMediumTranscription() {
        StringBuilder sb = new StringBuilder();
        sb.append("TRASCRIZIONE INTERVISTA - Cliente ABC - 10 Novembre 2024\n\n");
        sb.append("Intervistatore: Giulia Bianchi\n");
        sb.append("Cliente: Marco Ferrari (CTO, ABC Corporation)\n");
        sb.append("Durata: 30 minuti\n\n");
        sb.append("-".repeat(60)).append("\n\n");
        
        sb.append("Giulia: Buongiorno Marco, grazie per aver accettato questa intervista. ");
        sb.append("Vorrei iniziare chiedendoti quali sono le principali sfide che state affrontando ");
        sb.append("con il vostro attuale sistema di gestione documentale.\n\n");
        
        sb.append("Marco: Ciao Giulia, grazie a te. Le sfide sono principalmente tre. ");
        sb.append("Primo, il sistema attuale non scala bene. Abbiamo oltre 2 milioni di documenti ");
        sb.append("e le ricerche stanno diventando sempre più lente. Secondo, manca un sistema di ");
        sb.append("versioning robusto - abbiamo perso documenti importanti in passato. ");
        sb.append("Terzo, l'interfaccia utente è datata e poco intuitiva.\n\n");
        
        sb.append("Giulia: Capisco. Parliamo dei requisiti per il nuovo sistema. ");
        sb.append("Quali sono le funzionalità che considerate assolutamente necessarie?\n\n");
        
        sb.append("Marco: Allora, in ordine di priorità: primo, un motore di ricerca performante ");
        sb.append("con supporto per ricerca full-text e filtri avanzati. Secondo, gestione delle versioni ");
        sb.append("con possibilità di rollback. Terzo, un sistema di permessi granulare a livello di ");
        sb.append("documento e cartella. Quarto, integrazione con i nostri sistemi esistenti via API REST. ");
        sb.append("Quinto, dashboard analytics per monitorare l'utilizzo del sistema.\n\n");
        
        sb.append("Giulia: Perfetto. E per quanto riguarda i volumi di dati?\n\n");
        
        sb.append("Marco: Attualmente abbiamo circa 8TB di documenti, ma prevediamo una crescita ");
        sb.append("del 50% annuo nei prossimi 3 anni. Quindi il sistema deve essere progettato ");
        sb.append("per scalare fino a circa 20-25TB.\n\n");
        
        sb.append("Giulia: E gli utenti? Quanti ne avete e quali sono i pattern di utilizzo?\n\n");
        
        sb.append("Marco: Abbiamo 500 utenti attivi. La maggior parte accede al sistema quotidianamente, ");
        sb.append("con picchi di utilizzo tra le 9 e le 11 del mattino. In media, ogni utente ");
        sb.append("carica 10-15 documenti al giorno e effettua 30-40 ricerche.\n\n");
        
        sb.append("Giulia: Ci sono requisiti specifici di sicurezza o compliance?\n\n");
        
        sb.append("Marco: Sì, assolutamente. Dobbiamo essere conformi al GDPR per i dati personali ");
        sb.append("e abbiamo certificazione ISO 27001 che dobbiamo mantenere. Inoltre, alcuni documenti ");
        sb.append("contengono informazioni sensibili che devono essere criptate at-rest e in-transit.\n\n");
        
        sb.append("Giulia: Grazie Marco, queste informazioni sono molto utili. ");
        sb.append("Prepareremo una proposta tecnica basata su questi requisiti.\n\n");
        
        sb.append("Marco: Perfetto, aspetto con interesse la vostra proposta. Grazie Giulia!\n\n");
        
        sb.append("-".repeat(60)).append("\n");
        sb.append("Fine intervista\n");
        
        return sb.toString();
    }
    
    private String generateShortTranscription() {
        return "TRASCRIZIONE NOTE PRESENTAZIONE\n\n" +
               "Slide 1: Introduzione al Prodotto\n" +
               "- Panoramica delle funzionalità principali\n" +
               "- Target di mercato e casi d'uso\n\n" +
               "Slide 2-5: Demo Funzionalità Core\n" +
               "- Upload e gestione file\n" +
               "- Ricerca avanzata\n" +
               "- Collaborazione in tempo reale\n" +
               "- Dashboard analytics\n\n" +
               "Slide 6-8: Architettura e Sicurezza\n" +
               "- Stack tecnologico\n" +
               "- Scalabilità e performance\n" +
               "- Sicurezza e compliance\n\n" +
               "Slide 9-10: Pricing e Roadmap\n" +
               "- Piani tariffari\n" +
               "- Funzionalità future\n" +
               "- Timeline di rilascio";
    }
    
    private String generateShortTranslation() {
        return "PRESENTATION NOTES TRANSCRIPTION\n\n" +
               "Slide 1: Product Introduction\n" +
               "- Overview of main features\n" +
               "- Target market and use cases\n\n" +
               "Slide 2-5: Core Features Demo\n" +
               "- File upload and management\n" +
               "- Advanced search\n" +
               "- Real-time collaboration\n" +
               "- Analytics dashboard\n\n" +
               "Slide 6-8: Architecture and Security\n" +
               "- Technology stack\n" +
               "- Scalability and performance\n" +
               "- Security and compliance\n\n" +
               "Slide 9-10: Pricing and Roadmap\n" +
               "- Pricing plans\n" +
               "- Future features\n" +
               "- Release timeline";
    }
}
