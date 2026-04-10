package io.bootify.my_app.views;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
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
import io.bootify.my_app.service.TreeStructureOperationService;
import io.bootify.my_app.service.TreeStructureOperationService.TreeNodeCreateRequest;
import io.bootify.my_app.service.TreeStructureOperationService.TreeNodeDeleteRequest;
import io.bootify.my_app.service.TreeStructureOperationService.TreeNodeMovePosition;
import io.bootify.my_app.service.TreeStructureOperationService.TreeNodeMoveRequest;
import io.bootify.my_app.service.TreeStructureOperationService.TreeNodeRenameRequest;
import io.bootify.my_app.service.TreeStructureValidationService;
import io.bootify.my_app.service.TreeStructureValidationService.TreeOperationValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Route(value = "tree-structure", layout = MainLayout.class)
@PageTitle("Gestione Struttura Albero")
public class TreeStructureManagementView extends VerticalLayout {

    private final TreeStructureValidationService treeValidationService;
    private final TreeStructureOperationService treeOperationService;
    private final TreeGrid<TreeResponse> tree = new TreeGrid<>();
    private final List<TreeResponse> rootItems = new ArrayList<>();
    private TreeResponse draggedItem = null;
    private Button expandAllButton;
    private boolean isExpanded = false;

    public TreeStructureManagementView(final TreeStructureValidationService treeValidationService,
            final TreeStructureOperationService treeOperationService) {
        this.treeValidationService = treeValidationService;
        this.treeOperationService = treeOperationService;
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
            Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(e -> openRenameDialog(item));

            Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            deleteBtn.addClickListener(e -> deleteItem(item));

            HorizontalLayout actions = new HorizontalLayout(editBtn, deleteBtn);
            actions.setSpacing(false);
            return actions;
        }).setHeader("Azioni").setWidth("140px").setFlexGrow(0);

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

            if (targetItem != null && isDescendantOf(draggedItem, targetItem)) {
                Notification.show("Non puoi spostare un nodo dentro un suo discendente.",
                                3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            requestMove(draggedItem, targetItem, dropLocation);
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

    private int findMaxCode(List<TreeResponse> items) {
        int max = 0;
        for (TreeResponse item : items) {
            if (item.getCode() != null && item.getCode() > max) {
                max = item.getCode();
            }
            int childMax = findMaxCode(item.getChildren());
            if (childMax > max) max = childMax;
        }
        return max;
    }

    private void openAddDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Aggiungi Nuovo Elemento");

        FormLayout formLayout = new FormLayout();

        int nextCode = findMaxCode(rootItems) + 1;

        TextField codeField = new TextField("Codice");
        codeField.setValue(String.valueOf(nextCode));
        codeField.setHelperText("Calcolato automaticamente, puoi modificarlo");
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
            Integer newCode;
            try {
                newCode = Integer.parseInt(codeField.getValue().trim());
            } catch (NumberFormatException ex) {
                Notification.show("Il codice deve essere un numero intero.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                codeField.setInvalid(true);
                codeField.setErrorMessage("Codice non valido");
                return;
            }
            if (codeExists(newCode)) {
                Notification.show("Il codice '" + newCode + "' esiste già. Usa un codice univoco.", 4000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                codeField.setInvalid(true);
                codeField.setErrorMessage("Codice già esistente");
                return;
            }

            TreeResponse newItem = new TreeResponse(
                    newCode,
                    typeCombo.getValue(),
                    descriptionField.getValue()
            );

                requestCreate(newItem, dialog);
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
        requestDelete(item);
    }

    private void openRenameDialog(final TreeResponse item) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Cambia nome elemento");

        TextField descriptionField = new TextField("Descrizione");
        descriptionField.setWidthFull();
        descriptionField.setValue(item.getDescrizione() != null ? item.getDescrizione() : "");
        descriptionField.setRequired(true);

        Button saveButton = new Button("Salva", event -> {
            String newDescription = descriptionField.getValue() != null
                    ? descriptionField.getValue().trim() : "";
            if (newDescription.isEmpty()) {
                descriptionField.setInvalid(true);
                descriptionField.setErrorMessage("La descrizione e' obbligatoria");
                return;
            }
            descriptionField.setInvalid(false);
            requestRename(item, newDescription, dialog);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annulla", event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        VerticalLayout layout = new VerticalLayout(descriptionField, buttons);
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);

        dialog.add(layout);
        dialog.open();
    }

    private void requestCreate(final TreeResponse newItem, final Dialog sourceDialog) {
        TreeNodeCreateRequest request = new TreeNodeCreateRequest(
                newItem.getCode(),
                newItem.getType(),
                newItem.getDescrizione()
        );

        executeValidatedOperation(
                "Conferma creazione",
            () -> treeValidationService.verifyCreate(request),
                () -> {
                    treeOperationService.createNode(request);
                    rootItems.add(newItem);
                    refreshTree();
                    sourceDialog.close();
                    Notification.show("Elemento aggiunto. Trascinalo per riorganizzare la struttura.",
                                    3000, Notification.Position.BOTTOM_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
        );
    }

    private void requestMove(final TreeResponse item, final TreeResponse targetItem,
            final GridDropLocation dropLocation) {
        TreeNodeMovePosition movePosition = mapMovePosition(dropLocation, targetItem);
        TreeNodeMoveRequest request = new TreeNodeMoveRequest(
                item.getCode(),
                targetItem != null ? targetItem.getCode() : null,
                movePosition
        );

        executeValidatedOperation(
                "Conferma spostamento",
            () -> treeValidationService.verifyMove(request),
                () -> {
                    treeOperationService.moveNode(request);
                    applyMove(item, targetItem, dropLocation);
                    Notification.show("Spostamento completato", 2000, Notification.Position.BOTTOM_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
        );
    }

    private void requestDelete(final TreeResponse item) {
        TreeNodeDeleteRequest request = new TreeNodeDeleteRequest(item.getCode());

        executeValidatedOperation(
                "Conferma eliminazione",
            () -> treeValidationService.verifyDelete(request),
                () -> {
                    treeOperationService.deleteNode(request);
                    removeItemFromTree(item);
                    refreshTree();
                    Notification.show("Elemento eliminato", 2000, Notification.Position.BOTTOM_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
        );
    }

    private void requestRename(final TreeResponse item, final String newDescription,
            final Dialog sourceDialog) {
        TreeNodeRenameRequest request = new TreeNodeRenameRequest(item.getCode(), newDescription);

        executeValidatedOperation(
                "Conferma cambio nome",
                () -> treeValidationService.verifyRename(request),
                () -> {
                    treeOperationService.renameNode(request);
                    item.setDescrizione(newDescription);
                    refreshTree();
                    sourceDialog.close();
                    Notification.show("Nome elemento aggiornato", 2000, Notification.Position.BOTTOM_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
        );
    }

    private void executeValidatedOperation(final String dialogTitle,
            final Supplier<TreeOperationValidationResult> validationSupplier,
            final Runnable confirmedAction) {
        TreeOperationValidationResult validationResult = validationSupplier.get();
        if (!validationResult.allowed()) {
            showValidationErrorDialog(validationResult.message());
            return;
        }

        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader(dialogTitle);
        confirmDialog.setText(validationResult.message());
        confirmDialog.setCancelable(true);
        confirmDialog.setCancelText("Annulla");
        confirmDialog.setConfirmText("OK, prosegui");
        confirmDialog.addConfirmListener(event -> confirmedAction.run());
        confirmDialog.open();
    }

    private void showValidationErrorDialog(final String message) {
        ConfirmDialog errorDialog = new ConfirmDialog();
        errorDialog.setHeader("Operazione non consentita");
        Span errorText = new Span(message);
        errorText.getStyle().set("color", "var(--lumo-error-text-color)");
        errorDialog.setText(errorText);
        errorDialog.setConfirmText("Chiudi");
        errorDialog.setConfirmButtonTheme("error primary");
        errorDialog.open();
    }

    private TreeNodeMovePosition mapMovePosition(final GridDropLocation dropLocation,
            final TreeResponse targetItem) {
        if (targetItem == null) {
            return TreeNodeMovePosition.ROOT;
        }
        if (dropLocation == GridDropLocation.ON_TOP) {
            return TreeNodeMovePosition.CHILD_OF_TARGET;
        }
        if (dropLocation == GridDropLocation.ABOVE) {
            return TreeNodeMovePosition.BEFORE_TARGET;
        }
        return TreeNodeMovePosition.AFTER_TARGET;
    }

    private void applyMove(final TreeResponse item, final TreeResponse targetItem,
            final GridDropLocation dropLocation) {
        removeItemFromTree(item);

        if (targetItem == null) {
            rootItems.add(item);
        } else if (dropLocation == GridDropLocation.ON_TOP) {
            targetItem.getChildren().add(item);
            tree.expand(targetItem);
        } else if (dropLocation == GridDropLocation.ABOVE) {
            insertBefore(targetItem, item);
        } else if (dropLocation == GridDropLocation.BELOW) {
            insertAfter(targetItem, item);
        }

        refreshTree();
    }

    private boolean isDescendantOf(final TreeResponse parent, final TreeResponse possibleDescendant) {
        for (TreeResponse child : parent.getChildren()) {
            if (child.equals(possibleDescendant) || isDescendantOf(child, possibleDescendant)) {
                return true;
            }
        }
        return false;
    }

    private boolean codeExists(Integer code) {
        return codeExistsInList(rootItems, code);
    }

    private boolean codeExistsInList(List<TreeResponse> items, Integer code) {
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
        TreeResponse comp1 = new TreeResponse(10, "Complesso", "Sicurezza Nazionale");
        TreeResponse area1 = new TreeResponse(11, "Area", "Intelligence");
        TreeResponse tr1 = new TreeResponse(111, "Trattazione", "Analisi Strategica");
        TreeResponse tr2 = new TreeResponse(112, "Trattazione", "Controspionaggio");
        area1.getChildren().add(tr1);
        area1.getChildren().add(tr2);
        
        TreeResponse area2 = new TreeResponse(12, "Area", "Difesa");
        TreeResponse tr3 = new TreeResponse(121, "Trattazione", "Operazioni Militari");
        area2.getChildren().add(tr3);
        
        comp1.getChildren().add(area1);
        comp1.getChildren().add(area2);

        // Complesso 2: Innovazione Tecnologica
        TreeResponse comp2 = new TreeResponse(20, "Complesso", "Innovazione Tecnologica");
        TreeResponse area3 = new TreeResponse(21, "Area", "Cybersecurity");
        TreeResponse tr4 = new TreeResponse(211, "Trattazione", "Minacce Informatiche");
        TreeResponse tr5 = new TreeResponse(212, "Trattazione", "Sicurezza delle Reti");
        area3.getChildren().add(tr4);
        area3.getChildren().add(tr5);
        
        TreeResponse area4 = new TreeResponse(22, "Area", "Intelligenza Artificiale");
        TreeResponse tr6 = new TreeResponse(221, "Trattazione", "Machine Learning");
        area4.getChildren().add(tr6);
        
        comp2.getChildren().add(area3);
        comp2.getChildren().add(area4);

        rootItems.add(comp1);
        rootItems.add(comp2);

        refreshTree();
        tree.expandRecursively(rootItems, 2);
    }
}
