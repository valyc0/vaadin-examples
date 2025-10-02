package io.bootify.my_app.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import io.bootify.my_app.domain.Product;
import io.bootify.my_app.service.ProductService;

import java.util.List;

/**
 * Vista che mostra i prodotti in un grafo interattivo.
 * I nodi rappresentano i prodotti e sono collegati se appartengono alla stessa categoria.
 */
@Route(value = "graph", layout = MainLayout.class)
@PageTitle("Grafo Prodotti")
public class GraphView extends VerticalLayout {

    private final ProductService productService;
    private final GraphComponent graphComponent;
    private final Select<String> categoryFilter;

    public GraphView(ProductService productService) {
        this.productService = productService;
        
        setSpacing(true);
        setPadding(true);
        setSizeFull();
        
        // Titolo e descrizione
        H2 title = new H2("Visualizzazione Grafo Prodotti");
        Paragraph description = new Paragraph(
            "I prodotti sono rappresentati come nodi nel grafo. " +
            "I collegamenti tra nodi indicano prodotti della stessa categoria. " +
            "La dimensione del nodo è proporzionale alla quantità in stock. " +
            "Puoi trascinare i nodi per riposizionarli."
        );
        description.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("margin-bottom", "20px");
        
        // Filtro per categoria
        categoryFilter = new Select<>();
        categoryFilter.setLabel("Filtra per categoria");
        categoryFilter.setItems("Tutte", "Elettronica", "Accessori", "Audio", "Componenti", 
                               "Rete", "Periferiche", "Arredamento", "Smart Home", "Storage");
        categoryFilter.setValue("Tutte");
        categoryFilter.setWidth("250px");
        categoryFilter.addValueChangeListener(e -> updateGraph());
        
        // Componente grafo
        graphComponent = new GraphComponent();
        graphComponent.getStyle()
            .set("width", "100%")
            .set("flex-grow", "1");
        
        add(title, description, categoryFilter, graphComponent);
        setFlexGrow(1, graphComponent);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        updateGraph();
    }

    private void updateGraph() {
        String selectedCategory = categoryFilter.getValue();
        List<Product> products;
        
        if ("Tutte".equals(selectedCategory)) {
            products = productService.findAll();
        } else {
            products = productService.findByCategoryName(selectedCategory);
        }
        
        // Converti i prodotti in formato JSON per il componente JavaScript
        JsonArray jsonArray = Json.createArray();
        int index = 0;
        
        for (Product product : products) {
            JsonObject jsonProduct = Json.createObject();
            jsonProduct.put("id", product.getId());
            jsonProduct.put("name", product.getName());
            jsonProduct.put("category", product.getCategory());
            jsonProduct.put("price", product.getPrice().doubleValue());
            jsonProduct.put("quantity", product.getQuantity());
            jsonProduct.put("description", product.getDescription() != null ? product.getDescription() : "");
            
            jsonArray.set(index++, jsonProduct);
        }
        
        // Passa i dati al componente web
        graphComponent.setGraphData(jsonArray);
    }

    /**
     * Componente Web personalizzato che integra il graph-component TypeScript
     */
    @Tag("graph-component")
    @JsModule("./graph-component.ts")
    public static class GraphComponent extends Div {
        
        public GraphComponent() {
            getElement().getStyle()
                .set("display", "block")
                .set("width", "100%")
                .set("height", "100%");
        }
        
        public void setGraphData(JsonArray data) {
            getElement().setPropertyJson("graphData", data);
        }
    }
}
