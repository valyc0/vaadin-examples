package io.bootify.my_app.dto;

import java.time.LocalDate;

public class DocumentSearchFilterDTO {
    
    // Filtri generali
    private String nomeFile;
    private String tipologia;
    private LocalDate dataDa;
    private LocalDate dataA;
    
    // Filtri metadati principali
    private String autore;
    private String formato;
    private Double dimensioneMinKB;
    private Double dimensioneMaxKB;
    private String titolo;
    private String tags;
    
    // Metadato custom
    private String metadataChiave;
    private String metadataValore;
    
    // Filtro strutturato (tree)
    private String strutturaCode;
    private String strutturaType;
    private String strutturaDescrizione;
    
    public DocumentSearchFilterDTO() {
    }
    
    // Getters and Setters
    
    public String getNomeFile() {
        return nomeFile;
    }
    
    public void setNomeFile(String nomeFile) {
        this.nomeFile = nomeFile;
    }
    
    public String getTipologia() {
        return tipologia;
    }
    
    public void setTipologia(String tipologia) {
        this.tipologia = tipologia;
    }
    
    public LocalDate getDataDa() {
        return dataDa;
    }
    
    public void setDataDa(LocalDate dataDa) {
        this.dataDa = dataDa;
    }
    
    public LocalDate getDataA() {
        return dataA;
    }
    
    public void setDataA(LocalDate dataA) {
        this.dataA = dataA;
    }
    
    public String getAutore() {
        return autore;
    }
    
    public void setAutore(String autore) {
        this.autore = autore;
    }
    
    public String getFormato() {
        return formato;
    }
    
    public void setFormato(String formato) {
        this.formato = formato;
    }
    
    public Double getDimensioneMinKB() {
        return dimensioneMinKB;
    }
    
    public void setDimensioneMinKB(Double dimensioneMinKB) {
        this.dimensioneMinKB = dimensioneMinKB;
    }
    
    public Double getDimensioneMaxKB() {
        return dimensioneMaxKB;
    }
    
    public void setDimensioneMaxKB(Double dimensioneMaxKB) {
        this.dimensioneMaxKB = dimensioneMaxKB;
    }
    
    public String getTitolo() {
        return titolo;
    }
    
    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public String getMetadataChiave() {
        return metadataChiave;
    }
    
    public void setMetadataChiave(String metadataChiave) {
        this.metadataChiave = metadataChiave;
    }
    
    public String getMetadataValore() {
        return metadataValore;
    }
    
    public void setMetadataValore(String metadataValore) {
        this.metadataValore = metadataValore;
    }
    
    public String getStrutturaCode() {
        return strutturaCode;
    }
    
    public void setStrutturaCode(String strutturaCode) {
        this.strutturaCode = strutturaCode;
    }
    
    public String getStrutturaType() {
        return strutturaType;
    }
    
    public void setStrutturaType(String strutturaType) {
        this.strutturaType = strutturaType;
    }
    
    public String getStrutturaDescrizione() {
        return strutturaDescrizione;
    }
    
    public void setStrutturaDescrizione(String strutturaDescrizione) {
        this.strutturaDescrizione = strutturaDescrizione;
    }
    
    @Override
    public String toString() {
        return "DocumentSearchFilterDTO{" +
                "nomeFile='" + nomeFile + '\'' +
                ", tipologia='" + tipologia + '\'' +
                ", dataDa=" + dataDa +
                ", dataA=" + dataA +
                ", autore='" + autore + '\'' +
                ", formato='" + formato + '\'' +
                ", dimensioneMinKB=" + dimensioneMinKB +
                ", dimensioneMaxKB=" + dimensioneMaxKB +
                ", titolo='" + titolo + '\'' +
                ", tags='" + tags + '\'' +
                ", metadataChiave='" + metadataChiave + '\'' +
                ", metadataValore='" + metadataValore + '\'' +
                ", strutturaCode='" + strutturaCode + '\'' +
                ", strutturaType='" + strutturaType + '\'' +
                ", strutturaDescrizione='" + strutturaDescrizione + '\'' +
                '}';
    }
}
