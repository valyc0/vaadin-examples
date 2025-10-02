package io.bootify.my_app.config;

import io.bootify.my_app.domain.Product;
import io.bootify.my_app.repos.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(ProductRepository productRepository) {
        return args -> {
            // Check if data already exists
            if (productRepository.count() > 0) {
                return;
            }

            List<Product> products = Arrays.asList(
                createProduct("Laptop Dell XPS 15", "Potente laptop con processore Intel i7, 16GB RAM, SSD 512GB", 
                             new BigDecimal("1299.99"), "Elettronica", 15),
                createProduct("iPhone 14 Pro", "Smartphone Apple con chip A16 Bionic, fotocamera 48MP", 
                             new BigDecimal("1199.00"), "Elettronica", 25),
                createProduct("Samsung Galaxy S23", "Smartphone Android flagship con display AMOLED", 
                             new BigDecimal("899.99"), "Elettronica", 30),
                createProduct("MacBook Air M2", "Laptop Apple ultraleggero con chip M2", 
                             new BigDecimal("1449.00"), "Elettronica", 12),
                createProduct("iPad Air", "Tablet Apple con display 10.9 pollici", 
                             new BigDecimal("649.99"), "Elettronica", 20),
                createProduct("Mouse Logitech MX Master 3", "Mouse wireless ergonomico per produttività", 
                             new BigDecimal("99.99"), "Accessori", 50),
                createProduct("Tastiera Meccanica Keychron K2", "Tastiera meccanica wireless retroilluminata", 
                             new BigDecimal("89.99"), "Accessori", 35),
                createProduct("Monitor LG 27'' 4K", "Monitor 4K UHD con HDR10", 
                             new BigDecimal("449.99"), "Elettronica", 18),
                createProduct("Cuffie Sony WH-1000XM5", "Cuffie wireless con cancellazione del rumore", 
                             new BigDecimal("399.99"), "Audio", 22),
                createProduct("Speaker Bluetooth JBL Flip 6", "Speaker portatile impermeabile", 
                             new BigDecimal("129.99"), "Audio", 40),
                createProduct("Webcam Logitech C920", "Webcam Full HD 1080p per streaming", 
                             new BigDecimal("79.99"), "Accessori", 45),
                createProduct("SSD Samsung 1TB", "SSD NVMe M.2 ad alte prestazioni", 
                             new BigDecimal("119.99"), "Componenti", 60),
                createProduct("RAM Corsair Vengeance 32GB", "Memoria DDR4 3200MHz kit 2x16GB", 
                             new BigDecimal("149.99"), "Componenti", 28),
                createProduct("Scheda Video RTX 4070", "GPU NVIDIA per gaming e rendering", 
                             new BigDecimal("649.00"), "Componenti", 8),
                createProduct("Processore AMD Ryzen 7", "CPU 8 core 16 thread per gaming", 
                             new BigDecimal("329.99"), "Componenti", 15),
                createProduct("Zaino Laptop Samsonite", "Zaino porta PC fino a 15.6 pollici", 
                             new BigDecimal("79.99"), "Accessori", 55),
                createProduct("Hub USB-C 7-in-1", "Hub multiporta con HDMI, USB 3.0, lettore SD", 
                             new BigDecimal("49.99"), "Accessori", 70),
                createProduct("Powerbank Anker 20000mAh", "Batteria esterna ricarica rapida", 
                             new BigDecimal("59.99"), "Accessori", 80),
                createProduct("Microfono Blue Yeti", "Microfono USB professionale per podcast", 
                             new BigDecimal("129.99"), "Audio", 25),
                createProduct("Smartwatch Apple Watch Series 8", "Smartwatch con GPS e monitoraggio salute", 
                             new BigDecimal("449.00"), "Elettronica", 18),
                createProduct("Tablet Samsung Galaxy Tab S8", "Tablet Android 11 pollici con S Pen", 
                             new BigDecimal("699.99"), "Elettronica", 14),
                createProduct("Router WiFi 6 TP-Link", "Router mesh dual-band AX3000", 
                             new BigDecimal("149.99"), "Rete", 32),
                createProduct("Switch Ethernet 8 porte", "Switch Gigabit non gestito", 
                             new BigDecimal("39.99"), "Rete", 45),
                createProduct("Stampante HP LaserJet", "Stampante laser monocromatica WiFi", 
                             new BigDecimal("199.99"), "Periferiche", 12),
                createProduct("Scanner Epson Perfection", "Scanner piano ad alta risoluzione", 
                             new BigDecimal("259.99"), "Periferiche", 8),
                createProduct("Webcam 4K Razer Kiyo Pro", "Webcam professionale con HDR", 
                             new BigDecimal("199.99"), "Accessori", 16),
                createProduct("Sedia Gaming DXRacer", "Sedia ergonomica per gaming con supporto lombare", 
                             new BigDecimal("349.99"), "Arredamento", 10),
                createProduct("Scrivania Regolabile", "Scrivania elettrica sit-stand 120x60cm", 
                             new BigDecimal("449.99"), "Arredamento", 6),
                createProduct("Lampada LED da Scrivania", "Lampada dimmerabile con ricarica wireless", 
                             new BigDecimal("59.99"), "Arredamento", 42),
                createProduct("Supporto Laptop Elevato", "Stand in alluminio regolabile", 
                             new BigDecimal("39.99"), "Accessori", 65),
                createProduct("Cable Management Kit", "Kit organizzatore cavi per scrivania", 
                             new BigDecimal("24.99"), "Accessori", 90),
                createProduct("Mousepad XXL Gaming", "Tappetino mouse 90x40cm antiscivolo", 
                             new BigDecimal("29.99"), "Accessori", 75),
                createProduct("Luci LED RGB Philips Hue", "Striscia LED smart 2 metri", 
                             new BigDecimal("79.99"), "Smart Home", 35),
                createProduct("Telecamera Sicurezza WiFi", "Telecamera IP 1080p con visione notturna", 
                             new BigDecimal("89.99"), "Smart Home", 28),
                createProduct("Smart Plug TP-Link", "Presa intelligente WiFi con monitoraggio energia", 
                             new BigDecimal("19.99"), "Smart Home", 100),
                createProduct("Lettore NAS 2-Bay", "Network Attached Storage 8TB", 
                             new BigDecimal("299.99"), "Storage", 12),
                createProduct("Hard Disk Esterno 4TB", "HDD USB 3.0 portatile", 
                             new BigDecimal("99.99"), "Storage", 40),
                createProduct("Chiavetta USB 128GB", "Pendrive USB 3.1 veloce", 
                             new BigDecimal("24.99"), "Storage", 120),
                createProduct("Adattatore USB-C a HDMI", "Convertitore 4K 60Hz", 
                             new BigDecimal("19.99"), "Accessori", 85),
                createProduct("Cavo HDMI 2.1 3m", "Cavo ultra high speed 8K", 
                             new BigDecimal("29.99"), "Accessori", 95)
            );

            productRepository.saveAll(products);
            System.out.println("✅ Inizializzati " + products.size() + " prodotti nel database");
        };
    }

    private Product createProduct(String name, String description, BigDecimal price, 
                                  String category, Integer quantity) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        product.setQuantity(quantity);
        return product;
    }
}
