package io.bootify.my_app.component;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import io.bootify.my_app.model.TreeResponse;

import java.util.List;
import java.util.function.Consumer;

/**
 * Componente riutilizzabile che mostra un TreeGrid di TreeResponse con selezione singola via checkbox.
 */
public class StructuredTree extends VerticalLayout {

    private final TreeGrid<TreeResponse> tree = new TreeGrid<>();
    private TreeResponse selectedItem;
    private Consumer<TreeResponse> selectionListener;

    /**
     * Crea il componente con i dati forniti e pre-seleziona l'elemento con il codice indicato.
     *
     * @param treeData       radici dell'albero
     * @param preSelectCode  codice dell'elemento da pre-selezionare (può essere null)
     */
    public StructuredTree(List<TreeResponse> treeData, String preSelectCode) {
        setPadding(false);
        setSpacing(false);
        setWidthFull();

        buildTree(treeData, preSelectCode);
        add(tree);
    }

    /** Costruttore con dati di esempio e pre-selezione su "TR01". */
    public StructuredTree() {
        this(generateDefaultTreeData(), "TR01");
    }

    // -----------------------------------------------------------------------
    // API pubblica
    // -----------------------------------------------------------------------

    /** Restituisce l'elemento correntemente selezionato, o null se nessuno è selezionato. */
    public TreeResponse getSelectedItem() {
        return selectedItem;
    }

    /** Deseleziona l'elemento corrente e aggiorna la vista. */
    public void clearSelection() {
        selectedItem = null;
        tree.getDataCommunicator().reset();
        if (selectionListener != null) selectionListener.accept(null);
    }

    /** Registra un listener chiamato ogni volta che la selezione cambia (null = deselezionato). */
    public void setSelectionListener(Consumer<TreeResponse> listener) {
        this.selectionListener = listener;
    }

    // -----------------------------------------------------------------------
    // Costruzione interna
    // -----------------------------------------------------------------------

    private void buildTree(List<TreeResponse> treeData, String preSelectCode) {
        // Colonna checkbox per la selezione singola
        tree.addComponentColumn(item -> {
            Checkbox checkbox = new Checkbox();
            checkbox.setValue(item.equals(selectedItem));
            checkbox.addValueChangeListener(event -> {
                if (event.getValue()) {
                    selectedItem = item;
                    tree.getDataCommunicator().reset();
                    if (selectionListener != null) selectionListener.accept(selectedItem);
                    Notification.show(
                            "Selezionato: " + item.getDescrizione(),
                            2000,
                            Notification.Position.BOTTOM_CENTER
                    );
                } else {
                    if (item.equals(selectedItem)) {
                        selectedItem = null;
                        tree.getDataCommunicator().reset();
                        if (selectionListener != null) selectionListener.accept(null);
                    }
                }
            });
            return checkbox;
        }).setHeader("").setWidth("80px").setFlexGrow(0);

        tree.addHierarchyColumn(TreeResponse::getDescrizione)
                .setHeader("Elemento")
                .setFlexGrow(3)
                .setWidth("400px");

        tree.setItems(treeData, TreeResponse::getChildren);
        tree.setSelectionMode(Grid.SelectionMode.NONE);
        tree.setWidth("100%");
        tree.setHeight("350px");
        tree.getStyle()
            .set("font-size", "var(--lumo-font-size-m)")
            .set("border-radius", "12px")
            .set("overflow", "hidden");

        // Pre-selezione con espansione automatica dei nodi parent
        if (preSelectCode != null) {
            selectedItem = findAndExpandTreeItem(treeData, preSelectCode);
        }
    }

    /**
     * Cerca ricorsivamente un elemento per codice ed espande tutti i nodi parent.
     */
    private TreeResponse findAndExpandTreeItem(List<TreeResponse> items, String targetCode) {
        if (items == null || targetCode == null) {
            return null;
        }
        for (TreeResponse item : items) {
            if (targetCode.equals(item.getCode())) {
                return item;
            }
            TreeResponse found = findAndExpandTreeItem(item.getChildren(), targetCode);
            if (found != null) {
                tree.expand(item);
                return found;
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Dati di esempio
    // -----------------------------------------------------------------------

    private static List<TreeResponse> generateDefaultTreeData() {
        TreeResponse comp1 = new TreeResponse(10, "Complesso", "Sicurezza Nazionale");
        TreeResponse area1 = new TreeResponse(11, "Area", "Intelligence");
        TreeResponse tr1 = new TreeResponse(111, "Trattazione", "Analisi Strategica");
        area1.getChildren().add(tr1);
        comp1.getChildren().add(area1);

        TreeResponse comp2 = new TreeResponse(20, "Complesso", "Innovazione Tecnologica");
        TreeResponse area2 = new TreeResponse(21, "Area", "Cybersecurity");
        TreeResponse tr2 = new TreeResponse(211, "Trattazione", "Minacce Informatiche");
        area2.getChildren().add(tr2);
        comp2.getChildren().add(area2);

        return List.of(comp1, comp2);
    }
}
