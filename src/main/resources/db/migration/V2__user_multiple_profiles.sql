-- Migration: User multiple profiles
-- Change from single profile (ManyToOne) to multiple profiles (ManyToMany)

-- Create the new junction table for user-profile many-to-many relationship
CREATE TABLE IF NOT EXISTS user_profiles (
    user_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, profile_id),
    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_profiles_profile FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE
);

-- Migrate existing data: copy profile_id from users table to user_profiles junction table
INSERT INTO user_profiles (user_id, profile_id)
SELECT id, profile_id 
FROM users 
WHERE profile_id IS NOT NULL;

-- Drop the old foreign key constraint and column from users table
-- Note: Syntax may vary depending on your database (H2, MySQL, PostgreSQL, etc.)
-- For H2:
ALTER TABLE users DROP COLUMN IF EXISTS profile_id;

-- For MySQL/MariaDB (uncomment if needed):
-- ALTER TABLE users DROP FOREIGN KEY fk_users_profile;
-- ALTER TABLE users DROP COLUMN profile_id;

-- For PostgreSQL (uncomment if needed):
-- ALTER TABLE users DROP CONSTRAINT IF EXISTS fk_users_profile;
-- ALTER TABLE users DROP COLUMN IF EXISTS profile_id;

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_user_profiles_user ON user_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_profiles_profile ON user_profiles(profile_id);
