package com.blueapps.egyptianwriter.dashboard.documentgrid;

public class DocumentGridData {

    private String title;
    private String filename;

    public DocumentGridData(String title, String filename) {
        this.title = title;
        this.filename = filename;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
