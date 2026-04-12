package io.bootify.my_app.repos;

import io.bootify.my_app.domain.TreeNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TreeNodeRepository extends JpaRepository<TreeNode, Long> {

    List<TreeNode> findByParentCodeIsNullOrderBySortOrder();

    List<TreeNode> findByParentCodeOrderBySortOrder(Integer parentCode);

    Optional<TreeNode> findByCode(Integer code);
}
