package io.bootify.my_app.rest;

import io.bootify.my_app.service.PreviewService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/preview")
public class PreviewController {

    private final PreviewService previewService;

    public PreviewController(PreviewService previewService) {
        this.previewService = previewService;
    }

    /**
     * Restituisce l'anteprima (PNG) della prima pagina del documento.
     * Supporta: PDF, PPTX, PPT, DOCX, DOC, XLSX, XLS, immagini.
     * Il server scarica il file da originalPath (URL) o usa i dati salvati nel DB.
     */
    @GetMapping(value = "/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getPreview(@PathVariable Long id) {
        byte[] png = previewService.generatePreview(id);
        if (png == null || png.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(png);
    }
}
