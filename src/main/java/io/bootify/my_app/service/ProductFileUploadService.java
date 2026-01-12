package io.bootify.my_app.service;

import io.bootify.my_app.domain.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
@Transactional
public class ProductFileUploadService {

    private final ProductService productService;
    private final RestTemplate restTemplate;

    @Value("${product.upload.remote.url:http://localhost:8081/api/upload}")
    private String remoteUploadUrl;

    public ProductFileUploadService(ProductService productService, RestTemplate restTemplate) {
        this.productService = productService;
        this.restTemplate = restTemplate;
    }

    /**
     * Uploads a file for a specific product and sends it to remote service
     * 
     * @param productId The ID of the product
     * @param fileData The file bytes
     * @param fileName Original file name
     * @param contentType MIME type
     * @param uploadedBy The username of the person uploading
     * @return The updated Product
     */
    public Product uploadFileForProduct(Long productId, byte[] fileData, String fileName, 
                                       String contentType, String uploadedBy) {
        Product product = productService.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }

        // Store file data locally in product
        product.setFileData(fileData);
        product.setFileName(fileName);
        product.setFileType(contentType);
        product.setFileSize((long) fileData.length);
        product.setUploadedBy(uploadedBy);

        // Save to database first
        Product savedProduct = productService.save(product);

        // Send to remote service
        try {
            sendToRemoteService(productId, fileData, fileName, contentType);
        } catch (Exception e) {
            // Log error but don't fail the local upload
            System.err.println("Failed to send file to remote service: " + e.getMessage());
        }

        return savedProduct;
    }

    /**
     * Sends the file to remote service via multipart form data
     * 
     * @param productId The product ID
     * @param fileData The file bytes
     * @param fileName The file name
     * @param contentType The content type
     */
    private void sendToRemoteService(Long productId, byte[] fileData, String fileName, 
                                    String contentType) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Create multipart body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(fileData) {
            @Override
            public String getFilename() {
                return fileName;
            }
        });
        body.add("productId", productId.toString());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Send POST request to remote service
        ResponseEntity<String> response = restTemplate.exchange(
            remoteUploadUrl,
            HttpMethod.POST,
            requestEntity,
            String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Remote service returned error: " + response.getStatusCode());
        }
    }

    /**
     * Removes file from product
     * 
     * @param productId The product ID
     * @return The updated product
     */
    public Product removeFileFromProduct(Long productId) {
        Product product = productService.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }

        product.setFileData(null);
        product.setFileName(null);
        product.setFileType(null);
        product.setFileSize(null);

        return productService.save(product);
    }
}
