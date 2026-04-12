package io.bootify.my_app.service;

import io.bootify.my_app.domain.TreeNode;
import io.bootify.my_app.model.TreeResponse;
import io.bootify.my_app.repos.TreeNodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class TreeStructureOperationService {

    private static final Logger log = LoggerFactory.getLogger(TreeStructureOperationService.class);

    private final TreeNodeRepository treeNodeRepository;

    public TreeStructureOperationService(final TreeNodeRepository treeNodeRepository) {
        this.treeNodeRepository = treeNodeRepository;
    }

    @Transactional(readOnly = true)
    public List<TreeResponse> loadTree() {
        List<TreeNode> allNodes = treeNodeRepository.findAll();

        Map<Integer, TreeResponse> byCode = new HashMap<>();
        for (TreeNode n : allNodes) {
            byCode.put(n.getCode(), new TreeResponse(n.getCode(), n.getType(), n.getDescrizione()));
        }

        Map<Integer, List<TreeNode>> childrenMap = new HashMap<>();
        List<TreeNode> rootNodes = new ArrayList<>();

        for (TreeNode n : allNodes) {
            if (n.getParentCode() == null) {
                rootNodes.add(n);
            } else {
                childrenMap.computeIfAbsent(n.getParentCode(), k -> new ArrayList<>()).add(n);
            }
        }

        rootNodes.sort((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()));
        childrenMap.values().forEach(
                list -> list.sort((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder())));

        List<TreeResponse> roots = new ArrayList<>();
        for (TreeNode rootNode : rootNodes) {
            roots.add(buildSubtree(rootNode, byCode, childrenMap));
        }
        return roots;
    }

    @Transactional
    public void seedDefaultDataIfEmpty() {
        if (treeNodeRepository.count() > 0) {
            return;
        }
        // Complesso 1: Sicurezza Nazionale
        saveNode(10, "Complesso", "Sicurezza Nazionale",    null, 0);
        saveNode(11, "Area",      "Intelligence",           10,   0);
        saveNode(12, "Area",      "Difesa",                 10,   1);
        saveNode(111,"Trattazione","Analisi Strategica",    11,   0);
        saveNode(112,"Trattazione","Controspionaggio",      11,   1);
        saveNode(121,"Trattazione","Operazioni Militari",   12,   0);
        // Complesso 2: Innovazione Tecnologica
        saveNode(20, "Complesso", "Innovazione Tecnologica",null, 1);
        saveNode(21, "Area",      "Cybersecurity",          20,   0);
        saveNode(22, "Area",      "Intelligenza Artificiale",20,  1);
        saveNode(211,"Trattazione","Minacce Informatiche",  21,   0);
        saveNode(212,"Trattazione","Sicurezza delle Reti",  21,   1);
        saveNode(221,"Trattazione","Machine Learning",      22,   0);
        log.info("Seeded default tree structure");
    }

    private void saveNode(final int code, final String type, final String descrizione,
            final Integer parentCode, final int sortOrder) {
        TreeNode n = new TreeNode();
        n.setCode(code);
        n.setType(type);
        n.setDescrizione(descrizione);
        n.setParentCode(parentCode);
        n.setSortOrder(sortOrder);
        treeNodeRepository.save(n);
    }

    @Transactional
    public void createNode(final TreeNodeCreateRequest request) {
        List<TreeNode> roots = treeNodeRepository.findByParentCodeIsNullOrderBySortOrder();
        int maxSort = roots.stream().mapToInt(TreeNode::getSortOrder).max().orElse(-1);

        TreeNode node = new TreeNode();
        node.setCode(request.code());
        node.setType(request.type());
        node.setDescrizione(request.descrizione());
        node.setParentCode(null);
        node.setSortOrder(maxSort + 1);
        treeNodeRepository.save(node);
        log.info("Created tree node - code={}, type={}, descrizione={}", request.code(), request.type(), request.descrizione());
    }

    @Transactional
    public void moveNode(final TreeNodeMoveRequest request) {
        TreeNode node = treeNodeRepository.findByCode(request.nodeCode()).orElseThrow();

        switch (request.position()) {
            case ROOT -> {
                List<TreeNode> roots = treeNodeRepository.findByParentCodeIsNullOrderBySortOrder();
                int max = roots.stream().filter(n -> !n.getCode().equals(node.getCode()))
                        .mapToInt(TreeNode::getSortOrder).max().orElse(-1);
                node.setParentCode(null);
                node.setSortOrder(max + 1);
                treeNodeRepository.save(node);
            }
            case CHILD_OF_TARGET -> {
                List<TreeNode> children = treeNodeRepository.findByParentCodeOrderBySortOrder(request.targetCode());
                int max = children.stream().filter(n -> !n.getCode().equals(node.getCode()))
                        .mapToInt(TreeNode::getSortOrder).max().orElse(-1);
                node.setParentCode(request.targetCode());
                node.setSortOrder(max + 1);
                treeNodeRepository.save(node);
            }
            case BEFORE_TARGET -> {
                TreeNode target = treeNodeRepository.findByCode(request.targetCode()).orElseThrow();
                reorderSiblings(node, target.getParentCode(), target.getCode(), false);
            }
            case AFTER_TARGET -> {
                TreeNode target = treeNodeRepository.findByCode(request.targetCode()).orElseThrow();
                reorderSiblings(node, target.getParentCode(), target.getCode(), true);
            }
        }
        log.info("Moved tree node - code={}, targetCode={}, position={}", request.nodeCode(), request.targetCode(), request.position());
    }

    @Transactional
    public void deleteNode(final TreeNodeDeleteRequest request) {
        treeNodeRepository.findByCode(request.nodeCode()).ifPresent(treeNodeRepository::delete);
        log.info("Deleted tree node - code={}", request.nodeCode());
    }

    @Transactional
    public void renameNode(final TreeNodeRenameRequest request) {
        treeNodeRepository.findByCode(request.nodeCode()).ifPresent(node -> {
            node.setDescrizione(request.newDescrizione());
            treeNodeRepository.save(node);
        });
        log.info("Renamed tree node - code={}, newDescrizione={}", request.nodeCode(), request.newDescrizione());
    }

    @Transactional
    public void clearAll() {
        treeNodeRepository.deleteAll();
        log.info("Cleared all tree nodes");
    }

    private TreeResponse buildSubtree(final TreeNode node, final Map<Integer, TreeResponse> byCode,
            final Map<Integer, List<TreeNode>> childrenMap) {
        TreeResponse response = byCode.get(node.getCode());
        for (TreeNode child : childrenMap.getOrDefault(node.getCode(), Collections.emptyList())) {
            response.getChildren().add(buildSubtree(child, byCode, childrenMap));
        }
        return response;
    }

    private void reorderSiblings(final TreeNode node, final Integer newParentCode,
            final Integer anchorCode, final boolean insertAfter) {
        List<TreeNode> siblings = newParentCode == null
                ? treeNodeRepository.findByParentCodeIsNullOrderBySortOrder()
                : treeNodeRepository.findByParentCodeOrderBySortOrder(newParentCode);

        siblings.removeIf(n -> n.getCode().equals(node.getCode()));

        int anchorPos = -1;
        for (int i = 0; i < siblings.size(); i++) {
            if (siblings.get(i).getCode().equals(anchorCode)) {
                anchorPos = i;
                break;
            }
        }

        int insertPos = insertAfter ? anchorPos + 1 : anchorPos;
        if (insertPos < 0) insertPos = siblings.size();
        siblings.add(insertPos, node);

        node.setParentCode(newParentCode);
        for (int i = 0; i < siblings.size(); i++) {
            siblings.get(i).setSortOrder(i);
        }
        treeNodeRepository.saveAll(siblings);
    }

    public record TreeNodeCreateRequest(Integer code, String type, String descrizione) {}

    public record TreeNodeMoveRequest(Integer nodeCode, Integer targetCode,
            TreeNodeMovePosition position) {}

    public record TreeNodeDeleteRequest(Integer nodeCode) {}

    public record TreeNodeRenameRequest(Integer nodeCode, String newDescrizione) {}

    public enum TreeNodeMovePosition {
        ROOT,
        CHILD_OF_TARGET,
        BEFORE_TARGET,
        AFTER_TARGET
    }

}