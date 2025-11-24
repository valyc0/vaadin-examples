package io.bootify.my_app.model;

import java.util.ArrayList;
import java.util.List;

public class TreeResponse {
    private String code;
    private String type;
    private String descrizione;
    private List<TreeResponse> children = new ArrayList<>();

    public TreeResponse(String code, String type, String descrizione) {
        this.code = code;
        this.type = type;
        this.descrizione = descrizione;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public List<TreeResponse> getChildren() {
        return children;
    }

    public void setChildren(List<TreeResponse> children) {
        this.children = children;
    }
}
