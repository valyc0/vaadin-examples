package io.bootify.my_app.service;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;

/**
 * Renders Markdown text to safe HTML for display in the AI chat bubbles.
 */
@Service
public class MarkdownRenderer {

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder()
            .sanitizeUrls(true)
            .build();

    public String render(String markdown) {
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}
