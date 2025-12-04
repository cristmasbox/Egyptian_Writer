package com.blueapps.egyptianwriter.editor;

import androidx.lifecycle.ViewModel;

public class EditorViewModel extends ViewModel {

    // Constants
    public static final boolean MODE_READ = true;
    public static final boolean MODE_WRITE = false;

    private boolean mode = MODE_READ;
    private FileMaster fileMaster = null;


    // Getter and Setter
    public boolean getMode() {
        return mode;
    }

    public void setMode(boolean mode) {
        this.mode = mode;
    }

    public FileMaster getFileMaster() {
        return fileMaster;
    }

    public void setFileMaster(FileMaster fileMaster) {
        if (this.fileMaster == null) {
            this.fileMaster = fileMaster;
            this.fileMaster.extractData();
        }
    }
}
