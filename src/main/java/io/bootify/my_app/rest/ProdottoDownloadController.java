package io.bootify.my_app.rest;

import io.bootify.my_app.domain.Product;
import io.bootify.my_app.service.ProductService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prodotti")
public class ProdottoDownloadController {

    private final ProductService productService;

    public ProdottoDownloadController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        Product product = productService.findById(id);
        
        if (product == null || !product.hasFile()) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayResource resource = new ByteArrayResource(product.getFileData());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + product.getFileName() + "\"");
        
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (product.getFileType() != null) {
            try {
                mediaType = MediaType.parseMediaType(product.getFileType());
            } catch (Exception e) {
                // Use default if parsing fails
            }
        }

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(product.getFileSize())
                .contentType(mediaType)
                .body(resource);
    }
}
