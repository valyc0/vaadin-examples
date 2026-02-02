-- Migration: Add validity dates to user profiles
-- Trasforma la tabella user_profiles da semplice junction table a entità con date di validità

-- Rinomina la vecchia tabella
ALTER TABLE user_profiles RENAME TO user_profiles_old;

-- Crea la nuova tabella user_profiles con ID e date di validità
CREATE TABLE user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    start_date DATE,
    end_date DATE,
    date_created TIMESTAMP NOT NULL,
    last_updated TIMESTAMP NOT NULL,
    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_profiles_profile FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE
);

-- Migra i dati esistenti dalla vecchia tabella alla nuova
-- Le date sono NULL per i profili esistenti (nessuna limitazione temporale)
INSERT INTO user_profiles (user_id, profile_id, start_date, end_date, date_created, last_updated)
SELECT user_id, profile_id, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM user_profiles_old;

-- Elimina la vecchia tabella
DROP TABLE user_profiles_old;

-- Crea indici per migliori performance
CREATE INDEX idx_user_profiles_user ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_profile ON user_profiles(profile_id);
CREATE INDEX idx_user_profiles_dates ON user_profiles(start_date, end_date);
