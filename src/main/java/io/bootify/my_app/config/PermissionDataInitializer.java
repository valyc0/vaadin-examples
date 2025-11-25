package io.bootify.my_app.config;

import io.bootify.my_app.domain.Permission;
import io.bootify.my_app.repos.PermissionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PermissionDataInitializer {

    @Bean
    CommandLineRunner initPermissions(PermissionRepository permissionRepository) {
        return args -> {
            if (permissionRepository.count() == 0) {
                // USERS permissions
                createPermission(permissionRepository, "USER_VIEW", "Visualizzare utenti", "USERS");
                createPermission(permissionRepository, "USER_CREATE", "Creare nuovi utenti", "USERS");
                createPermission(permissionRepository, "USER_EDIT", "Modificare utenti esistenti", "USERS");
                createPermission(permissionRepository, "USER_DELETE", "Eliminare utenti", "USERS");
                createPermission(permissionRepository, "USER_MANAGE_PROFILES", "Gestire profili utenti", "USERS");

                // PRODUCTS permissions
                createPermission(permissionRepository, "PRODUCT_VIEW", "Visualizzare prodotti", "PRODUCTS");
                createPermission(permissionRepository, "PRODUCT_CREATE", "Creare nuovi prodotti", "PRODUCTS");
                createPermission(permissionRepository, "PRODUCT_EDIT", "Modificare prodotti esistenti", "PRODUCTS");
                createPermission(permissionRepository, "PRODUCT_DELETE", "Eliminare prodotti", "PRODUCTS");
                createPermission(permissionRepository, "PRODUCT_EXPORT", "Esportare dati prodotti", "PRODUCTS");

                // FILES permissions
                createPermission(permissionRepository, "FILE_VIEW", "Visualizzare file", "FILES");
                createPermission(permissionRepository, "FILE_UPLOAD", "Caricare file", "FILES");
                createPermission(permissionRepository, "FILE_DOWNLOAD", "Scaricare file", "FILES");
                createPermission(permissionRepository, "FILE_DELETE", "Eliminare file", "FILES");
                createPermission(permissionRepository, "FILE_MANAGE_METADATA", "Gestire metadati file", "FILES");

                // REPORTS permissions
                createPermission(permissionRepository, "REPORT_VIEW", "Visualizzare report", "REPORTS");
                createPermission(permissionRepository, "REPORT_CREATE", "Creare report", "REPORTS");
                createPermission(permissionRepository, "REPORT_EXPORT", "Esportare report", "REPORTS");

                // SYSTEM permissions
                createPermission(permissionRepository, "SYSTEM_ADMIN", "Accesso amministratore sistema", "SYSTEM");
                createPermission(permissionRepository, "SYSTEM_SETTINGS", "Modificare impostazioni sistema", "SYSTEM");
                createPermission(permissionRepository, "SYSTEM_LOGS", "Visualizzare log di sistema", "SYSTEM");
                createPermission(permissionRepository, "SYSTEM_BACKUP", "Gestire backup", "SYSTEM");

                System.out.println("✓ Inizializzati " + permissionRepository.count() + " permessi predefiniti");
            }
        };
    }

    private void createPermission(PermissionRepository repo, String name, String description, String category) {
        Permission permission = new Permission();
        permission.setName(name);
        permission.setDescription(description);
        permission.setCategory(category);
        permission.setActive(true);
        repo.save(permission);
    }
}
