package io.bootify.my_app.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/product-uploads")
public class ProductFileUploadController {

    @Value("${product.upload.directory:uploads/products}")
    private String uploadDirectory;

    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadProductFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productId") String productId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "File vuoto");
                return ResponseEntity.badRequest().body(response);
            }

            // Create upload directory if not exists
            Path uploadPath = Paths.get(uploadDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String originalFilename = file.getOriginalFilename();
            String filename = productId + "_" + timestamp + "_" + originalFilename;
            
            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Build response
            response.put("success", true);
            response.put("message", "File caricato con successo");
            response.put("productId", productId);
            response.put("filename", filename);
            response.put("filepath", filePath.toString());
            response.put("size", file.getSize());
            response.put("contentType", file.getContentType());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Errore durante il salvataggio: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getUploadInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("uploadDirectory", uploadDirectory);
        
        try {
            Path uploadPath = Paths.get(uploadDirectory);
            info.put("absolutePath", uploadPath.toAbsolutePath().toString());
            info.put("exists", String.valueOf(Files.exists(uploadPath)));
            info.put("writable", String.valueOf(Files.isWritable(uploadPath.getParent())));
        } catch (Exception e) {
            info.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(info);
    }
}
