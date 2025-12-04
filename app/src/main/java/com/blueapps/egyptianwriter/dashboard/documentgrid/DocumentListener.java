package com.blueapps.egyptianwriter.dashboard.documentgrid;

public interface DocumentListener {

    void OnDeleteDocument(String name);
    void OnExportDocument(String name);
    void OnOpenDocument(String name);

}
