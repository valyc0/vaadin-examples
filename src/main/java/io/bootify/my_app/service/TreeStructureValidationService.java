package io.bootify.my_app.service;

import io.bootify.my_app.service.TreeStructureOperationService.TreeNodeCreateRequest;
import io.bootify.my_app.service.TreeStructureOperationService.TreeNodeDeleteRequest;
import io.bootify.my_app.service.TreeStructureOperationService.TreeNodeMoveRequest;
import io.bootify.my_app.service.TreeStructureOperationService.TreeNodeRenameRequest;
import org.springframework.stereotype.Service;


@Service
public class TreeStructureValidationService {

    private static final Integer MINACCE_INFORMATICHE_CODE = 211;

    public TreeOperationValidationResult verifyCreate(final TreeNodeCreateRequest request) {
        return allow("Creazione nodo " + request.code());
    }

    public TreeOperationValidationResult verifyMove(final TreeNodeMoveRequest request) {
        if (MINACCE_INFORMATICHE_CODE.equals(request.nodeCode())) {
            return new TreeOperationValidationResult(false,
                    "Spostamento non consentito per il nodo Minacce Informatiche.");
        }
        return allow("Spostamento nodo " + request.nodeCode());
    }

    public TreeOperationValidationResult verifyDelete(final TreeNodeDeleteRequest request) {
        if (MINACCE_INFORMATICHE_CODE.equals(request.nodeCode())) {
            return new TreeOperationValidationResult(false,
                    "Cancellazione non consentita per il nodo Minacce Informatiche.");
        }
        return allow("Eliminazione nodo " + request.nodeCode());
    }

    public TreeOperationValidationResult verifyRename(final TreeNodeRenameRequest request) {
        if (MINACCE_INFORMATICHE_CODE.equals(request.nodeCode())) {
            return new TreeOperationValidationResult(false,
                    "Cambio nome non consentito per il nodo Minacce Informatiche.");
        }
        return allow("Cambio nome nodo " + request.nodeCode());
    }

    private TreeOperationValidationResult allow(final String operationDescription) {
        return new TreeOperationValidationResult(true,
                operationDescription + " verificato lato server. Premi OK per proseguire.");
    }

    public record TreeOperationValidationResult(boolean allowed, String message) {
    }

}