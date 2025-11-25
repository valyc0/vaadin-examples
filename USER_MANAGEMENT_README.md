# Gestione Utenti e Profili

## Panoramica

Questa nuova funzionalità fornisce un sistema completo di gestione utenti con profili e permessi predefiniti. Il sistema è progettato per offrire la migliore esperienza utente possibile con un'interfaccia moderna e intuitiva.

## Caratteristiche Principali

### 🎯 Gestione Utenti
- **CRUD completo**: Crea, visualizza, modifica ed elimina utenti
- **Informazioni dettagliate**: Nome, cognome, username, email, telefono, dipartimento
- **Avatar automatici**: Iniziali generate automaticamente con colori distintivi
- **Ricerca avanzata**: Cerca per nome, email, username, dipartimento
- **Stato attivo/disattivo**: Gestisci lo stato degli utenti
- **Validazione**: Username ed email univoci con validazione in tempo reale

### 👥 Gestione Profili
- **Creazione profili personalizzati**: Gli utenti possono creare nuovi profili
- **Assegnazione permessi**: Seleziona permessi da categorie predefinite
- **Permessi predefiniti**: I permessi sono già definiti nel database
- **Visualizzazione chiara**: Vedi quanti utenti e permessi ha ogni profilo
- **Descrizioni**: Aggiungi descrizioni ai profili per chiarezza

### 🔐 Sistema Permessi

I permessi sono organizzati in 5 categorie:

#### 👥 USERS (Utenti)
- `USER_VIEW` - Visualizzare utenti
- `USER_CREATE` - Creare nuovi utenti
- `USER_EDIT` - Modificare utenti esistenti
- `USER_DELETE` - Eliminare utenti
- `USER_MANAGE_PROFILES` - Gestire profili utenti

#### 📦 PRODUCTS (Prodotti)
- `PRODUCT_VIEW` - Visualizzare prodotti
- `PRODUCT_CREATE` - Creare nuovi prodotti
- `PRODUCT_EDIT` - Modificare prodotti esistenti
- `PRODUCT_DELETE` - Eliminare prodotti
- `PRODUCT_EXPORT` - Esportare dati prodotti

#### 📁 FILES (File)
- `FILE_VIEW` - Visualizzare file
- `FILE_UPLOAD` - Caricare file
- `FILE_DOWNLOAD` - Scaricare file
- `FILE_DELETE` - Eliminare file
- `FILE_MANAGE_METADATA` - Gestire metadati file

#### 📊 REPORTS (Report)
- `REPORT_VIEW` - Visualizzare report
- `REPORT_CREATE` - Creare report
- `REPORT_EXPORT` - Esportare report

#### ⚙️ SYSTEM (Sistema)
- `SYSTEM_ADMIN` - Accesso amministratore sistema
- `SYSTEM_SETTINGS` - Modificare impostazioni sistema
- `SYSTEM_LOGS` - Visualizzare log di sistema
- `SYSTEM_BACKUP` - Gestire backup

## Struttura del Codice

### Entità (Domain)
- **User.java**: Entità utente con informazioni personali e profilo
- **Profile.java**: Entità profilo con relazione many-to-many con permessi
- **Permission.java**: Entità permesso con categoria e descrizione

### Repository
- **UserRepository.java**: Query per utenti con ricerca avanzata
- **ProfileRepository.java**: Query per profili con eager loading dei permessi
- **PermissionRepository.java**: Query per permessi con filtri per categoria

### Servizi
- **UserService.java**: Logica business per utenti con validazioni
- **ProfileService.java**: Logica business per profili
- **PermissionService.java**: Servizio read-only per permessi predefiniti

### Viste (Views)
- **UserManagementView.java**: Vista principale con interfaccia a tab
- **UserFormDialog.java**: Dialog per creare/modificare utenti con anteprima permessi
- **ProfileFormDialog.java**: Dialog per creare/modificare profili con selezione permessi

### Configurazione
- **PermissionDataInitializer.java**: Inizializza i permessi predefiniti al primo avvio

## Esperienza Utente (UX)

### 🎨 Design Moderno
- **Interfaccia a tab**: Passa facilmente tra utenti e profili
- **Avatar colorati**: Identificazione visiva immediata degli utenti
- **Badge e icone**: Elementi visivi per stato, profili e azioni
- **Colori semantici**: Verde per attivo, rosso per disattivato
- **Grid responsive**: Si adatta a diverse dimensioni dello schermo

### ✨ Funzionalità Avanzate
- **Anteprima permessi in tempo reale**: Quando selezioni un profilo per un utente, vedi immediatamente i permessi
- **Selezione permessi per categoria**: I permessi sono raggruppati per categoria con checkbox
- **Validazione in tempo reale**: Feedback immediato su username/email duplicati
- **Conferme di eliminazione**: Dialog di conferma con avvisi per profili assegnati
- **Ricerca istantanea**: Filtra utenti e profili mentre digiti
- **Statistiche live**: Vedi il numero totale e gli utenti attivi

### 🔄 Workflow Ottimizzato

#### Creazione Utente
1. Click su "Nuovo Utente"
2. Compila i dati personali
3. Seleziona un profilo dal dropdown
4. Visualizza automaticamente i permessi del profilo
5. Salva

#### Creazione Profilo
1. Click su "Nuovo Profilo"
2. Inserisci nome e descrizione
3. Seleziona permessi da categorie organizzate
4. Salva

#### Modifica
- Click sull'icona di modifica nella riga
- I dati vengono precaricati nel form
- Modifica e salva

## Accesso

La nuova funzionalità è accessibile dal menu principale:
- **Menu**: "Gestione Utenti"
- **URL**: `/users`

## Database

Le tabelle vengono create automaticamente da JPA:
- `users` - Tabella utenti
- `profiles` - Tabella profili
- `permissions` - Tabella permessi (popolata automaticamente)
- `profile_permissions` - Tabella di join per relazione many-to-many

I permessi vengono inizializzati automaticamente al primo avvio dell'applicazione tramite `PermissionDataInitializer`.

## Note Tecniche

- **Validazione**: Username ed email devono essere univoci
- **Cascade**: L'eliminazione di un profilo non elimina gli utenti associati (il profilo viene semplicemente rimosso)
- **Eager Loading**: I permessi vengono caricati insieme ai profili per evitare lazy loading issues
- **Transazioni**: Tutte le operazioni di scrittura sono transazionali
- **Date**: Tutte le date sono in formato OffsetDateTime per gestire i fusi orari

## Possibili Estensioni Future

- 🔒 Sistema di autenticazione e autorizzazione
- 📧 Invio email di benvenuto ai nuovi utenti
- 🔑 Reset password
- 📊 Dashboard con statistiche utenti
- 🔍 Audit log delle modifiche
- 👤 Gestione avatar personalizzati
- 🌍 Multi-tenancy
- 📱 Notifiche push
