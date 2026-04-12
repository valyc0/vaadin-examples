package io.bootify.my_app.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tree_node")
public class TreeNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer code;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 255)
    private String descrizione;

    @Column(name = "parent_code")
    private Integer parentCode;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    public Long getId() { return id; }
    public void setId(final Long id) { this.id = id; }

    public Integer getCode() { return code; }
    public void setCode(final Integer code) { this.code = code; }

    public String getType() { return type; }
    public void setType(final String type) { this.type = type; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(final String descrizione) { this.descrizione = descrizione; }

    public Integer getParentCode() { return parentCode; }
    public void setParentCode(final Integer parentCode) { this.parentCode = parentCode; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(final Integer sortOrder) { this.sortOrder = sortOrder; }
}
