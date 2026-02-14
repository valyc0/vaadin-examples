package io.bootify.my_app.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.bootify.my_app.model.TreeResponse;

import java.util.ArrayList;
import java.util.List;

@Route(value = "tree-structure", layout = MainLayout.class)
@PageTitle("Gestione Struttura Albero")
public class TreeStructureManagementView extends VerticalLayout {

    private final TreeGrid<TreeResponse> tree = new TreeGrid<>();
    private final List<TreeResponse> rootItems = new ArrayList<>();
    private TreeResponse draggedItem = null;
    private Button expandAllButton;
    private boolean isExpanded = false;

    public TreeStructureManagementView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H3("Costruisci Struttura Albero (Drag & Drop)"));

        // Toolbar con pulsanti azione
        HorizontalLayout toolbar = createToolbar();
        add(toolbar);

        // TreeGrid per visualizzare e riordinare la struttura
        buildTreeGrid();
        add(tree);

        // Inizializza con struttura di default
        initializeDefaultStructure();
    }

    private HorizontalLayout createToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setSpacing(true);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);

        Button addButton = new Button("Aggiungi Elemento", new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openAddDialog());

        Button clearButton = new Button("Pulisci Tutto", new Icon(VaadinIcon.TRASH));
        clearButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        clearButton.addClickListener(e -> {
            rootItems.clear();
            refreshTree();
            Notification.show("Struttura pulita", 2000, Notification.Position.BOTTOM_CENTER);
        });

        Button sendButton = new Button("Mostra JSON", new Icon(VaadinIcon.CODE));
        sendButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        sendButton.addClickListener(e -> showJson());

        expandAllButton = new Button("Espandi Tutto", new Icon(VaadinIcon.EXPAND_FULL));
        expandAllButton.addClickListener(e -> toggleExpandCollapse());

        toolbar.add(addButton, clearButton, expandAllButton, sendButton);

        return toolbar;
    }

    private void buildTreeGrid() {
        tree.addHierarchyColumn(TreeResponse::getDescrizione)
                .setHeader("Elemento (Trascina per riordinare)")
                .setFlexGrow(3);
        
        tree.addColumn(TreeResponse::getCode)
                .setHeader("Codice")
                .setWidth("120px");
        
        tree.addColumn(TreeResponse::getType)
                .setHeader("Tipo")
                .setWidth("120px");

        tree.addComponentColumn(item -> {
            Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            deleteBtn.addClickListener(e -> deleteItem(item));
            return deleteBtn;
        }).setHeader("Azioni").setWidth("100px").setFlexGrow(0);

        tree.setItems(rootItems, TreeResponse::getChildren);
        tree.setWidthFull();
        tree.setHeight("500px");

        // Abilita drag and drop
        setupDragAndDrop();
    }

    private void setupDragAndDrop() {
        tree.setRowsDraggable(true);
        tree.setDropMode(GridDropMode.ON_TOP_OR_BETWEEN);

        tree.addDragStartListener(event -> {
            draggedItem = event.getDraggedItems().get(0);
        });

        tree.addDragEndListener(event -> {
            draggedItem = null;
        });

        tree.addDropListener(event -> {
            TreeResponse targetItem = event.getDropTargetItem().orElse(null);
            GridDropLocation dropLocation = event.getDropLocation();

            if (draggedItem == null || draggedItem.equals(targetItem)) {
                return;
            }

            // Rimuovi l'item dalla sua posizione originale
            removeItemFromTree(draggedItem);

            if (targetItem == null) {
                // Drop su area vuota - aggiungi come root
                rootItems.add(draggedItem);
            } else if (dropLocation == GridDropLocation.ON_TOP) {
                // Drop sopra un item - diventa figlio
                targetItem.getChildren().add(draggedItem);
            } else if (dropLocation == GridDropLocation.ABOVE) {
                // Drop sopra - inserisci prima del target
                insertBefore(targetItem, draggedItem);
            } else if (dropLocation == GridDropLocation.BELOW) {
                // Drop sotto - inserisci dopo il target
                insertAfter(targetItem, draggedItem);
            }

            refreshTree();
            tree.expand(targetItem);
        });
    }

    private void removeItemFromTree(TreeResponse item) {
        // Rimuovi dalle root
        if (!rootItems.remove(item)) {
            // Rimuovi dai figli
            removeFromChildren(rootItems, item);
        }
    }

    private boolean removeFromChildren(List<TreeResponse> items, TreeResponse toRemove) {
        for (TreeResponse parent : items) {
            if (parent.getChildren().remove(toRemove)) {
                return true;
            }
            if (removeFromChildren(parent.getChildren(), toRemove)) {
                return true;
            }
        }
        return false;
    }

    private void insertBefore(TreeResponse target, TreeResponse toInsert) {
        // Cerca nelle root
        int index = rootItems.indexOf(target);
        if (index != -1) {
            rootItems.add(index, toInsert);
            return;
        }
        // Cerca nei figli
        insertBeforeInChildren(rootItems, target, toInsert);
    }

    private boolean insertBeforeInChildren(List<TreeResponse> items, TreeResponse target, TreeResponse toInsert) {
        for (TreeResponse parent : items) {
            int index = parent.getChildren().indexOf(target);
            if (index != -1) {
                parent.getChildren().add(index, toInsert);
                return true;
            }
            if (insertBeforeInChildren(parent.getChildren(), target, toInsert)) {
                return true;
            }
        }
        return false;
    }

    private void insertAfter(TreeResponse target, TreeResponse toInsert) {
        // Cerca nelle root
        int index = rootItems.indexOf(target);
        if (index != -1) {
            rootItems.add(index + 1, toInsert);
            return;
        }
        // Cerca nei figli
        insertAfterInChildren(rootItems, target, toInsert);
    }

    private boolean insertAfterInChildren(List<TreeResponse> items, TreeResponse target, TreeResponse toInsert) {
        for (TreeResponse parent : items) {
            int index = parent.getChildren().indexOf(target);
            if (index != -1) {
                parent.getChildren().add(index + 1, toInsert);
                return true;
            }
            if (insertAfterInChildren(parent.getChildren(), target, toInsert)) {
                return true;
            }
        }
        return false;
    }

    private void openAddDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Aggiungi Nuovo Elemento");

        FormLayout formLayout = new FormLayout();

        TextField codeField = new TextField("Codice");
        codeField.setPlaceholder("es: CT01, AR01, TR01");
        codeField.setRequired(true);
        codeField.addValueChangeListener(e -> codeField.setInvalid(false));

        ComboBox<String> typeCombo = new ComboBox<>("Tipo");
        typeCombo.setItems("Complesso", "Area", "Trattazione");
        typeCombo.setValue("Complesso");
        typeCombo.setRequired(true);

        TextField descriptionField = new TextField("Descrizione");
        descriptionField.setPlaceholder("es: Sicurezza Nazionale");
        descriptionField.setRequired(true);

        formLayout.add(codeField, typeCombo, descriptionField);

        Button saveButton = new Button("Aggiungi", e -> {
            if (codeField.isEmpty() || typeCombo.isEmpty() || descriptionField.isEmpty()) {
                Notification.show("Tutti i campi sono obbligatori", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Controllo codice duplicato
            String newCode = codeField.getValue();
            if (codeExists(newCode)) {
                Notification.show("Il codice '" + newCode + "' esiste già. Usa un codice univoco.", 4000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                codeField.setInvalid(true);
                codeField.setErrorMessage("Codice già esistente");
                return;
            }

            TreeResponse newItem = new TreeResponse(
                    codeField.getValue(),
                    typeCombo.getValue(),
                    descriptionField.getValue()
            );

            rootItems.add(newItem);
            refreshTree();
            dialog.close();
            
            Notification.show("Elemento aggiunto. Trascinalo per riorganizzare la struttura.", 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annulla", e -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        buttons.setSpacing(true);

        VerticalLayout dialogLayout = new VerticalLayout(formLayout, buttons);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void deleteItem(TreeResponse item) {
        removeItemFromTree(item);
        refreshTree();
        Notification.show("Elemento eliminato", 2000, Notification.Position.BOTTOM_CENTER);
    }

    private boolean codeExists(String code) {
        return codeExistsInList(rootItems, code);
    }

    private boolean codeExistsInList(List<TreeResponse> items, String code) {
        for (TreeResponse item : items) {
            if (code.equals(item.getCode())) {
                return true;
            }
            if (codeExistsInList(item.getChildren(), code)) {
                return true;
            }
        }
        return false;
    }

    private void showJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            String json = mapper.writeValueAsString(rootItems);

            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Struttura Gerarchica (JSON)");
            dialog.setWidth("800px");
            dialog.setHeight("600px");

            Pre jsonPre = new Pre(json);
            jsonPre.getStyle()
                    .set("background-color", "#f5f5f5")
                    .set("padding", "16px")
                    .set("border-radius", "4px")
                    .set("overflow", "auto")
                    .set("font-family", "monospace")
                    .set("font-size", "13px");

            Button copyButton = new Button("Copia JSON", new Icon(VaadinIcon.COPY), e -> {
                // Copia nella clipboard tramite JavaScript
                getUI().ifPresent(ui -> ui.getPage().executeJs(
                    "navigator.clipboard.writeText($0).then(() => {});",
                    json
                ));
                Notification.show("JSON copiato negli appunti", 2000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
            copyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            Button closeButton = new Button("Chiudi", e -> dialog.close());

            HorizontalLayout buttons = new HorizontalLayout(copyButton, closeButton);
            buttons.setSpacing(true);

            VerticalLayout layout = new VerticalLayout(jsonPre, buttons);
            layout.setSizeFull();
            layout.expand(jsonPre);

            dialog.add(layout);
            dialog.open();

        } catch (Exception e) {
            Notification.show("Errore nella generazione JSON: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void refreshTree() {
        tree.setItems(rootItems, TreeResponse::getChildren);
    }

    private void toggleExpandCollapse() {
        if (isExpanded) {
            // Comprimi tutto
            tree.collapseRecursively(rootItems, 10);
            expandAllButton.setText("Espandi Tutto");
            expandAllButton.setIcon(new Icon(VaadinIcon.EXPAND_FULL));
            isExpanded = false;
        } else {
            // Espandi tutto
            tree.expandRecursively(rootItems, 10);
            expandAllButton.setText("Comprimi Tutto");
            expandAllButton.setIcon(new Icon(VaadinIcon.COMPRESS));
            isExpanded = true;
        }
    }

    private void initializeDefaultStructure() {
        // Complesso 1: Sicurezza Nazionale
        TreeResponse comp1 = new TreeResponse("CT01", "Complesso", "Sicurezza Nazionale");
        TreeResponse area1 = new TreeResponse("AR01", "Area", "Intelligence");
        TreeResponse tr1 = new TreeResponse("TR01", "Trattazione", "Analisi Strategica");
        TreeResponse tr2 = new TreeResponse("TR02", "Trattazione", "Controspionaggio");
        area1.getChildren().add(tr1);
        area1.getChildren().add(tr2);
        
        TreeResponse area2 = new TreeResponse("AR02", "Area", "Difesa");
        TreeResponse tr3 = new TreeResponse("TR03", "Trattazione", "Operazioni Militari");
        area2.getChildren().add(tr3);
        
        comp1.getChildren().add(area1);
        comp1.getChildren().add(area2);

        // Complesso 2: Innovazione Tecnologica
        TreeResponse comp2 = new TreeResponse("CT02", "Complesso", "Innovazione Tecnologica");
        TreeResponse area3 = new TreeResponse("AR03", "Area", "Cybersecurity");
        TreeResponse tr4 = new TreeResponse("TR04", "Trattazione", "Minacce Informatiche");
        TreeResponse tr5 = new TreeResponse("TR05", "Trattazione", "Sicurezza delle Reti");
        area3.getChildren().add(tr4);
        area3.getChildren().add(tr5);
        
        TreeResponse area4 = new TreeResponse("AR04", "Area", "Intelligenza Artificiale");
        TreeResponse tr6 = new TreeResponse("TR06", "Trattazione", "Machine Learning");
        area4.getChildren().add(tr6);
        
        comp2.getChildren().add(area3);
        comp2.getChildren().add(area4);

        rootItems.add(comp1);
        rootItems.add(comp2);

        refreshTree();
        tree.expandRecursively(rootItems, 2);
    }
}
