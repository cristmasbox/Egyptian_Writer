package com.blueapps.egyptianwriter.dashboard.documentgrid;

import android.content.Context;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class DocumentManager {

    private static final String TAG = "DocumentManager";

    private ArrayList<DocumentGridData> documents = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();
    private File path;

    public DocumentManager(Context context){
        path = new File(context.getFilesDir() + "/Documents");
    }

    public ArrayList<DocumentGridData> getDocuments(){

        documents = new ArrayList<>();
        names = new ArrayList<>();
        try {
            if (!path.exists()){
                if (!path.mkdir()){
                    // TODO: Error handling
                }
            }
            File[] filesArray = path.listFiles((dir, name) -> name.toLowerCase().endsWith(".ewdoc"));
            ArrayList<File> files;
            if (filesArray != null) {
                // Sort Files in alphabetical order
                Arrays.sort(filesArray, Comparator.comparing(File::getName));

                files = new ArrayList<>(Arrays.asList(filesArray));

                Log.d(TAG, "This files where found: " + files);

                for (File file : files) {
                    String name = file.getName();
                    String filename = name;
                    if (!name.equals(".ewdoc")) {
                        int lastPointIndex = StringUtils.lastIndexOf(name, '.');
                        if (lastPointIndex > 0) {
                            name = name.substring(0, lastPointIndex);
                        }
                        DocumentGridData document = new DocumentGridData(name, filename);
                        documents.add(document);
                        names.add(name);
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }


        return documents;
    }

    public ArrayList<String> getNames(){
        return names;
    }

    public void addDocument(String filename){
        if(!path.exists()){
            path.mkdir();
        }
        File file = new File(this.path, filename + ".ewdoc");
        try {
            if (!file.createNewFile()) {
                Log.e(TAG, "File already exist");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteDocument(String filename){
        File file = new File(this.path, filename + ".ewdoc");
        try {
            if (file.delete()){
                Log.e(TAG, "File could not deleted");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
