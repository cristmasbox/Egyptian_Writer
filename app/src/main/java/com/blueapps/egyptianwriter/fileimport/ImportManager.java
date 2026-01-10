package com.blueapps.egyptianwriter.fileimport;

import static com.blueapps.egyptianwriter.export.FileResultActivity.MIME_DEFAULT;
import static com.blueapps.egyptianwriter.export.FileResultActivity.MIME_EWDOC;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;

import com.blueapps.egyptianwriter.dashboard.documentgrid.DocumentGridData;
import com.blueapps.egyptianwriter.dashboard.documentgrid.DocumentManager;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class ImportManager implements ActivityResultCallback<Uri> {

    private static final String TAG = "ImportManager";

    private Context context;
    private DocumentManager documentManager;
    private ActivityResultLauncher<String[]> activityResultLauncher;

    private ArrayList<ImportListener> listeners = new ArrayList<>();


    public ImportManager(Context context, DocumentManager documentManager){
        this.context = context;
        this.documentManager = documentManager;
    }

    public void showDialog(){
        String[] input = {MIME_EWDOC, MIME_DEFAULT};
        activityResultLauncher.launch(input);
    }

    public void setActivityResultLauncher(ActivityResultLauncher<String[]> activityResultLauncher){
        this.activityResultLauncher = activityResultLauncher;
    }

    @Override
    public void onActivityResult(Uri result) {
        if (result != null) {
            if (result.getPath() != null) {
                File file = new File(result.getPath());
                Log.d(TAG, "Selected file: " + file.getPath());
                Log.d(TAG, "MIME-TYPE: " + getMimeType(result));

                // Copy file to app specific directory
                String filename = file.getName();
                if (! filename.equals(".ewdoc")){
                    int lastPointIndex = StringUtils.lastIndexOf(filename, '.');
                    if (lastPointIndex > 0) {
                        String extension = filename.substring(lastPointIndex + 1);
                        String name = filename.substring(0, lastPointIndex);
                        if (extension.equals("ewdoc")){
                            copyFile(result, getDestinationUri(filename));

                            // Inform listeners
                            for (ImportListener listener: listeners){
                                listener.onImport(name);
                            }
                        } else {
                            // Inform listeners
                            for (ImportListener listener: listeners){
                                listener.onError();
                            }
                            // TODO: Error handling
                        }
                    } else {
                        // Inform listeners
                        for (ImportListener listener: listeners){
                            listener.onError();
                        }
                        // TODO: Error handling
                    }
                } else {
                    // Inform listeners
                    for (ImportListener listener: listeners){
                        listener.onError();
                    }
                    // TODO: Error handling
                }
            } else {
                // Inform listeners
                for (ImportListener listener: listeners){
                    listener.onError();
                }
                // TODO: ERROR HANDLING
            }
        } else {
            // Inform listeners
            for (ImportListener listener: listeners){
                listener.onError();
            }
            // TODO: ERROR HANDLING
        }
    }

    public void copyFile(Uri from, Uri to){

        try (InputStream is = context.getContentResolver().openInputStream(from); OutputStream os = context.getContentResolver().openOutputStream(to)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: ERROR HANDLING
        }
    }

    private String adaptFileName(String oldName, ArrayList<DocumentGridData> documents){
        int counter = 1;
        boolean invalidName;
        do {
            invalidName = false;
            for (DocumentGridData data : documents) {
                if (data.getTitle().equals(oldName + " (" + counter + ")")) {
                    counter++;
                    invalidName = true;
                    break;
                }
            }
        } while (invalidName);
        return oldName + " (" + counter + ").ewdoc";
    }

    public Uri getDestinationUri(String filename){
        // Check if filename already exist
        ArrayList<DocumentGridData> documents = documentManager.getDocuments();
        for (DocumentGridData data: documents) {
            if (Objects.equals(data.getFilename(), filename)) {
                filename = adaptFileName(data.getTitle(), documents);
                break;
            }
        }
        return Uri.fromFile(new File(context.getFilesDir() + "/Documents/" + filename));
    }

    private String getMimeType(Uri uri) {
        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public void addOnImportListener(ImportListener listener){
        listeners.add(listener);
    }
}
