# Migrazione: Profili Multipli per Utente

## Panoramica
L'applicazione è stata aggiornata per permettere agli utenti di avere **multipli profili** invece di un singolo profilo.

## Modifiche al Database

### Schema Precedente
```sql
-- Relazione ManyToOne: Un utente aveva UN profilo
users.profile_id -> profiles.id
```

### Nuovo Schema
```sql
-- Relazione ManyToMany: Un utente può avere MULTIPLI profili
user_profiles.user_id -> users.id
user_profiles.profile_id -> profiles.id
```

### Nuova Tabella
```sql
CREATE TABLE user_profiles (
    user_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, profile_id)
);
```

## Modifiche al Codice

### 1. Entità `User`
- **Prima**: `@ManyToOne private Profile profile`
- **Dopo**: `@ManyToMany private Set<Profile> profiles`

Nuovi metodi:
- `getProfiles()` / `setProfiles()` - gestione della collezione
- `getProfileNames()` - restituisce i nomi dei profili separati da virgola
- `getProfileCount()` - conta i profili assegnati

### 2. Entità `Profile`
- **Prima**: `@OneToMany(mappedBy = "profile")`
- **Dopo**: `@ManyToMany(mappedBy = "profiles")`

### 3. UI Components

#### UserFormDialog
- Sostituito `ComboBox<Profile>` con `MultiSelectComboBox<Profile>`
- Aggiornato il preview dei permessi per mostrare i permessi **combinati** di tutti i profili selezionati
- Mostra il conteggio totale dei permessi unici

#### UserManagementView
- La colonna "Profili" ora mostra **badges multipli** per ogni profilo assegnato
- I badges sono ordinati alfabeticamente e supportano il wrapping

## Funzionalità

### Gestione Permessi
Quando un utente ha multipli profili, i **permessi sono combinati** (unione):
- Se Profilo A ha permessi [READ, WRITE]
- E Profilo B ha permessi [WRITE, DELETE]
- L'utente avrà: [READ, WRITE, DELETE]

### UI Migliorata
1. **Selezione multipla** intuitiva con chip/badges
2. **Preview in tempo reale** dei permessi combinati
3. **Creazione rapida** di nuovi profili dal form utente
4. **Indicatori visivi** per profili selezionati

## Migrazione Dati

### Automatica (Hibernate)
Con `spring.jpa.hibernate.ddl-auto: update`, Hibernate:
1. Crea la tabella `user_profiles`
2. Mantiene i dati esistenti (ma la colonna `profile_id` deve essere migrata manualmente)

### Manuale
Se usi Flyway o esegui migrazioni manuali, esegui:
```bash
# Applica lo script SQL
mysql -u username -p database_name < src/main/resources/db/migration/V2__user_multiple_profiles.sql
```

## Testing

### Verificare la Migrazione
```sql
-- Controlla che i dati siano stati migrati
SELECT u.username, p.name 
FROM users u
JOIN user_profiles up ON u.id = up.user_id
JOIN profiles p ON up.profile_id = p.profile_id;

-- Verifica utenti senza profili
SELECT username FROM users u
LEFT JOIN user_profiles up ON u.id = up.user_id
WHERE up.profile_id IS NULL;
```

### Test Funzionali
1. ✅ Creare un nuovo utente con multipli profili
2. ✅ Modificare i profili di un utente esistente
3. ✅ Verificare che i permessi combinati siano corretti
4. ✅ Eliminare un profilo e verificare che gli utenti associati rimangano intatti
5. ✅ Visualizzare la grid con i badges multipli

## Rollback (in caso di problemi)

Se necessario tornare alla versione precedente:
```sql
-- 1. Aggiungi di nuovo la colonna profile_id
ALTER TABLE users ADD COLUMN profile_id BIGINT;

-- 2. Migra il primo profilo di ogni utente (se esistono multipli)
UPDATE users u
SET profile_id = (
    SELECT profile_id 
    FROM user_profiles 
    WHERE user_id = u.id 
    LIMIT 1
);

-- 3. Rimuovi la tabella junction
DROP TABLE user_profiles;

-- 4. Ricrea il constraint
ALTER TABLE users 
ADD CONSTRAINT fk_users_profile 
FOREIGN KEY (profile_id) REFERENCES profiles(id);
```

## Note Importanti

⚠️ **Attenzione**: 
- Gli utenti DEVONO avere almeno un profilo (validazione nel form)
- I permessi sono l'unione (union) di tutti i profili, non l'intersezione
- Quando si elimina un profilo, gli utenti mantengono gli altri profili assegnati

## Compatibilità
- ✅ Spring Boot 3.x
- ✅ Hibernate 6.x
- ✅ Vaadin 24.x
- ✅ Java 17+
- ✅ Database: H2, MySQL, PostgreSQL, Oracle
