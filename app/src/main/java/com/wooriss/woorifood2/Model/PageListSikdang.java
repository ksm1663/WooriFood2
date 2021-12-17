package com.wooriss.woorifood2.Model;

import java.io.Serializable;
import java.util.List;

public class PageListSikdang {
    PlaceMeta meta;
    List<Sikdang> documents;

    public PlaceMeta getMeta() {
        return meta;
    }

    public void setMeta(PlaceMeta meta) {
        this.meta = meta;
    }

    public List<Sikdang> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Sikdang> documents) {
        this.documents = documents;
    }
}