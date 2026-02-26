package io.bootify.my_app.service;

import io.bootify.my_app.domain.Content;
import io.bootify.my_app.repos.ContentRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

@Service
public class PreviewService {

    private static final Logger log = LoggerFactory.getLogger(PreviewService.class);

    private static final Set<String> IMAGE_EXTS = Set.of("png", "jpg", "jpeg", "gif", "bmp", "webp", "svg");
    private static final int PREVIEW_WIDTH = 800;

    private final ContentRepository contentRepository;

    public PreviewService(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    @Transactional(readOnly = true)
    public byte[] generatePreview(Long contentId) {
        return contentRepository.findById(contentId)
                .map(this::doGenerate)
                .orElse(null);
    }

    // -------------------------------------------------------------------------
    // CORE
    // -------------------------------------------------------------------------

    private byte[] doGenerate(Content content) {
        byte[] data = resolveData(content);

        // Nessun file disponibile → restituisce un placeholder grafico informativo
        if (data == null || data.length == 0) {
            try { return renderPlaceholder(content); } catch (Exception e) { return null; }
        }

        String ext = extension(content.getFileName());
        String type = content.getFileType() != null ? content.getFileType().toUpperCase() : "";
        String mime = content.getMimeType() != null ? content.getMimeType().toLowerCase() : "";

        try {
            return switch (ext) {
                case "pdf" -> renderPdf(data);
                case "pptx" -> renderPptx(data);
                case "ppt"  -> renderPpt(data);
                case "docx" -> renderDocx(data);
                case "doc"  -> renderDoc(data);
                case "xlsx" -> renderXlsx(data);
                case "xls"  -> renderXls(data);
                default -> {
                    // fallback by type/mime
                    if ("PDF".equals(type) || mime.contains("pdf"))             yield renderPdf(data);
                    if ("IMAGE".equals(type) || mime.startsWith("image/"))      yield IMAGE_EXTS.contains(ext) ? data : null;
                    if (mime.contains("presentationml") || mime.contains("powerpoint")) yield renderPptx(data);
                    if (mime.contains("wordprocessingml") || mime.contains("msword"))   yield renderDocx(data);
                    if (mime.contains("spreadsheetml") || mime.contains("excel"))       yield renderXlsx(data);
                    yield null;
                }
            };
        } catch (Exception e) {
            log.warn("Preview failed for '{}': {}", content.getFileName(), e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // DATA SOURCE: DB blob o URL remoto
    // -------------------------------------------------------------------------

    private byte[] resolveData(Content content) {
        // 1) BLOB in DB
        if (content.getFileData() != null && content.getFileData().length > 0) {
            return content.getFileData();
        }
        String path = content.getOriginalPath();
        if (path == null) return null;

        // 2) Classpath (src/main/resources) — prefisso "classpath:"
        if (path.startsWith("classpath:")) {
            String resource = path.substring("classpath:".length());
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
                if (in != null) return in.readAllBytes();
            } catch (Exception e) {
                log.debug("Classpath resource not found '{}': {}", resource, e.getMessage());
            }
            return null;
        }

        // 3) URL remoto
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return download(path);
        }

        // 4) Percorso locale su filesystem
        try {
            Path base = Paths.get(path);
            if (Files.isDirectory(base)) {
                Path candidate = base.resolve(content.getFileName());
                if (Files.isReadable(candidate)) return Files.readAllBytes(candidate);
                try (var stream = Files.list(base)) {
                    var found = stream
                            .filter(p -> p.getFileName().toString()
                                    .equalsIgnoreCase(content.getFileName()))
                            .findFirst();
                    if (found.isPresent()) return Files.readAllBytes(found.get());
                }
            } else if (Files.isReadable(base)) {
                return Files.readAllBytes(base);
            }
        } catch (Exception e) {
            log.debug("Local file read failed for '{}': {}", path, e.getMessage());
        }
        return null;
    }

    private byte[] download(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(30_000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            try (InputStream in = conn.getInputStream()) {
                return in.readAllBytes();
            }
        } catch (Exception e) {
            log.warn("Download failed from {}: {}", url, e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // PLACEHOLDER — quando non c'è un file reale
    // -------------------------------------------------------------------------

    private byte[] renderPlaceholder(Content content) throws Exception {
        int width  = PREVIEW_WIDTH;
        int height = 260;

        // Colore header per tipo file
        Color headerColor = typeColor(content.getFileType());

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Sfondo
        g.setColor(new Color(248, 249, 250));
        g.fillRect(0, 0, width, height);

        // Header colorato
        g.setColor(headerColor);
        g.fillRect(0, 0, width, 60);

        // Etichetta tipo
        String typeLabel = content.getFileType() != null ? content.getFileType().toUpperCase() : "FILE";
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        FontMetrics fmT = g.getFontMetrics();
        g.drawString(typeLabel, (width - fmT.stringWidth(typeLabel)) / 2, 40);

        // Nome file
        g.setColor(new Color(33, 37, 41));
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        FontMetrics fmN = g.getFontMetrics();
        String name = content.getFileName() != null ? content.getFileName() : "—";
        // Wrap se troppo lungo
        if (fmN.stringWidth(name) > width - 40) {
            int cut = name.length();
            while (cut > 0 && fmN.stringWidth(name.substring(0, cut) + "…") > width - 40) cut--;
            name = name.substring(0, cut) + "…";
        }
        g.drawString(name, (width - fmN.stringWidth(name)) / 2, 95);

        // Riga separatore
        g.setColor(new Color(220, 220, 220));
        g.drawLine(40, 110, width - 40, 110);

        // Dettagli
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(80, 80, 80));
        int y = 135;
        int lineH = 22;
        String[] lines = {
                "Dimensione: " + (content.getFormattedFileSize() != null ? content.getFormattedFileSize() : "—"),
                "Categoria:  " + (content.getCategory() != null ? content.getCategory() : "—"),
                "Caricato da: " + (content.getUploadUser() != null ? content.getUploadUser() : "—"),
                "Percorso:   " + (content.getOriginalPath() != null ? content.getOriginalPath() : "—")
        };
        for (String line : lines) {
            g.drawString(line, 40, y);
            y += lineH;
        }

        // Footer "Anteprima non disponibile"
        g.setColor(new Color(150, 150, 150));
        g.setFont(new Font("SansSerif", Font.ITALIC, 11));
        String footer = "Anteprima non disponibile — file non presente sul server";
        FontMetrics fmF = g.getFontMetrics();
        g.drawString(footer, (width - fmF.stringWidth(footer)) / 2, height - 14);

        g.dispose();
        return toPng(img);
    }

    private Color typeColor(String fileType) {
        if (fileType == null) return new Color(100, 100, 100);
        return switch (fileType.toUpperCase()) {
            case "PDF"      -> new Color(211, 47, 47);
            case "IMAGE"    -> new Color(56, 142, 60);
            case "VIDEO"    -> new Color(21, 101, 192);
            case "AUDIO"    -> new Color(123, 31, 162);
            case "ARCHIVE"  -> new Color(230, 81, 0);
            case "DOCUMENT" -> new Color(0, 121, 107);
            default         -> new Color(66, 66, 66);
        };
    }

    // -------------------------------------------------------------------------
    // PDF
    // -------------------------------------------------------------------------

    private byte[] renderPdf(byte[] data) throws Exception {
        try (PDDocument doc = Loader.loadPDF(data)) {
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage img = renderer.renderImageWithDPI(0, 150);
            return toPng(img);
        }
    }

    // -------------------------------------------------------------------------
    // POWERPOINT — PPTX
    // -------------------------------------------------------------------------

    private byte[] renderPptx(byte[] data) throws Exception {
        try (XMLSlideShow pptx = new XMLSlideShow(new ByteArrayInputStream(data))) {
            List<XSLFSlide> slides = pptx.getSlides();
            if (slides.isEmpty()) return null;
            Dimension size = pptx.getPageSize();
            double scale = PREVIEW_WIDTH / size.getWidth();
            int w = (int) (size.getWidth() * scale);
            int h = (int) (size.getHeight() * scale);
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, w, h);
            g.scale(scale, scale);
            slides.get(0).draw(g);
            g.dispose();
            return toPng(img);
        }
    }

    // -------------------------------------------------------------------------
    // POWERPOINT — PPT legacy
    // -------------------------------------------------------------------------

    private byte[] renderPpt(byte[] data) throws Exception {
        try (HSLFSlideShow ppt = new HSLFSlideShow(new ByteArrayInputStream(data))) {
            var slides = ppt.getSlides();
            if (slides.isEmpty()) return null;
            Dimension size = ppt.getPageSize();
            double scale = PREVIEW_WIDTH / size.getWidth();
            int w = (int) (size.getWidth() * scale);
            int h = (int) (size.getHeight() * scale);
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, w, h);
            g.scale(scale, scale);
            slides.get(0).draw(g);
            g.dispose();
            return toPng(img);
        }
    }

    // -------------------------------------------------------------------------
    // WORD — DOCX
    // -------------------------------------------------------------------------

    private byte[] renderDocx(byte[] data) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(data))) {
            StringBuilder sb = new StringBuilder();
            doc.getParagraphs().stream()
                    .limit(40)
                    .forEach(p -> {
                        String t = p.getText();
                        if (t != null && !t.isBlank()) sb.append(t).append("\n");
                    });
            return renderText(sb.toString().trim(), "Documento Word — " + "Prima pagina");
        }
    }

    // -------------------------------------------------------------------------
    // WORD — DOC legacy
    // -------------------------------------------------------------------------

    private byte[] renderDoc(byte[] data) throws Exception {
        try (HWPFDocument doc = new HWPFDocument(new ByteArrayInputStream(data))) {
            String text = doc.getRange().text();
            String preview = text.length() > 2000 ? text.substring(0, 2000) : text;
            return renderText(preview.trim(), "Documento Word");
        }
    }

    // -------------------------------------------------------------------------
    // EXCEL — XLSX
    // -------------------------------------------------------------------------

    private byte[] renderXlsx(byte[] data) throws Exception {
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(data))) {
            return renderWorkbook(wb);
        }
    }

    // -------------------------------------------------------------------------
    // EXCEL — XLS legacy
    // -------------------------------------------------------------------------

    private byte[] renderXls(byte[] data) throws Exception {
        try (Workbook wb = new HSSFWorkbook(new ByteArrayInputStream(data))) {
            return renderWorkbook(wb);
        }
    }

    private byte[] renderWorkbook(Workbook wb) throws Exception {
        Sheet sheet = wb.getSheetAt(0);
        StringBuilder sb = new StringBuilder();
        sb.append("Foglio: ").append(sheet.getSheetName()).append("\n\n");
        int rowCount = 0;
        for (Row row : sheet) {
            if (rowCount++ > 25) break;
            StringBuilder rowSb = new StringBuilder();
            for (Cell cell : row) {
                rowSb.append(cellText(cell)).append("  \t");
            }
            String rowStr = rowSb.toString().stripTrailing();
            if (!rowStr.isBlank()) sb.append(rowStr).append("\n");
        }
        return renderText(sb.toString().trim(), "Foglio Excel — " + sheet.getSheetName());
    }

    private String cellText(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue();
            case NUMERIC -> {
                double v = cell.getNumericCellValue();
                yield v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default      -> "";
        };
    }

    // -------------------------------------------------------------------------
    // TEXT → IMAGE (Java2D)
    // -------------------------------------------------------------------------

    private byte[] renderText(String text, String title) throws Exception {
        int width = PREVIEW_WIDTH;
        Font titleFont = new Font("SansSerif", Font.BOLD, 16);
        Font bodyFont  = new Font("Monospaced", Font.PLAIN, 13);
        int lineHeight = 18;
        int padding    = 20;
        int titleH     = 40;

        // Conta righe wrappate
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D gtmp = tmp.createGraphics();
        gtmp.setFont(bodyFont);
        FontMetrics fm = gtmp.getFontMetrics();
        int usableWidth = width - padding * 2;
        String[] rawLines = text.split("\n");
        java.util.List<String> lines = new java.util.ArrayList<>();
        for (String raw : rawLines) {
            if (raw.isBlank()) { lines.add(""); continue; }
            // wrap long lines
            while (fm.stringWidth(raw) > usableWidth && raw.length() > 10) {
                int cut = raw.length();
                while (cut > 0 && fm.stringWidth(raw.substring(0, cut)) > usableWidth) cut--;
                lines.add(raw.substring(0, cut));
                raw = raw.substring(cut);
            }
            lines.add(raw);
        }
        gtmp.dispose();

        int height = Math.min(titleH + lines.size() * lineHeight + padding * 2, 1200);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // sfondo bianco
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // header grigio
        g.setColor(new Color(240, 242, 245));
        g.fillRect(0, 0, width, titleH);
        g.setColor(new Color(60, 60, 60));
        g.setFont(titleFont);
        g.drawString(title, padding, titleH - 12);

        // separatore
        g.setColor(new Color(200, 200, 200));
        g.drawLine(0, titleH, width, titleH);

        // testo
        g.setColor(new Color(30, 30, 30));
        g.setFont(bodyFont);
        int y = titleH + padding + lineHeight;
        for (String line : lines) {
            if (y + lineHeight > height) break;
            g.drawString(line, padding, y);
            y += lineHeight;
        }

        g.dispose();
        return toPng(img);
    }

    // -------------------------------------------------------------------------
    // UTILITY
    // -------------------------------------------------------------------------

    private byte[] toPng(BufferedImage img) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", out);
        return out.toByteArray();
    }

    private String extension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1).toLowerCase() : "";
    }
}
