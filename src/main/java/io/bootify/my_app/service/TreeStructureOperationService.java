package io.bootify.my_app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class TreeStructureOperationService {

    private static final Logger log = LoggerFactory.getLogger(TreeStructureOperationService.class);

    public void createNode(final TreeNodeCreateRequest request) {
        log.info("POST tree node - code={}, type={}, descrizione={}",
                request.code(), request.type(), request.descrizione());
    }

    public void moveNode(final TreeNodeMoveRequest request) {
        log.info("PUT tree node - nodeCode={}, targetCode={}, position={}",
                request.nodeCode(), request.targetCode(), request.position());
    }

    public void deleteNode(final TreeNodeDeleteRequest request) {
        log.info("DELETE tree node - nodeCode={}", request.nodeCode());
    }

    public void renameNode(final TreeNodeRenameRequest request) {
        log.info("PUT tree node rename - nodeCode={}, newDescrizione={}",
                request.nodeCode(), request.newDescrizione());
    }

    public record TreeNodeCreateRequest(Integer code, String type, String descrizione) {
    }

    public record TreeNodeMoveRequest(Integer nodeCode, Integer targetCode,
            TreeNodeMovePosition position) {
    }

    public record TreeNodeDeleteRequest(Integer nodeCode) {
    }

    public record TreeNodeRenameRequest(Integer nodeCode, String newDescrizione) {
    }

    public enum TreeNodeMovePosition {
        ROOT,
        CHILD_OF_TARGET,
        BEFORE_TARGET,
        AFTER_TARGET
    }

}