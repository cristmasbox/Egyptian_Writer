package com.blueapps.egyptianwriter.fileimport;

import static com.blueapps.egyptianwriter.export.FileResultActivity.MIME_DEFAULT;
import static com.blueapps.egyptianwriter.export.FileResultActivity.MIME_EWDOC;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.documentfile.provider.DocumentFile;

import com.blueapps.egyptianwriter.R;
import com.blueapps.egyptianwriter.dashboard.documentgrid.DocumentGridData;
import com.blueapps.egyptianwriter.dashboard.documentgrid.DocumentManager;
import com.blueapps.egyptianwriter.issuecenter.PopupListener;
import com.blueapps.egyptianwriter.issuecenter.StandardPopup;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.io.File;
import java.io.FileOutputStream;
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
            String filename = getFileName(context, result);
            Log.d(TAG, "Selected Uri: " + result);
            Log.d(TAG, "Filename: \"" + filename + "\"");
            openPopup(getDestinationName(filename), result);
        } else {
            // Inform listeners
            for (ImportListener listener: listeners){
                listener.onError();
            }
            // TODO: ERROR HANDLING
        }
    }

    public void handleImportIntents(Intent intent){
        // Get intent, action and MIME type
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action)
                && type != null) {
            Log.d(TAG, "Started from ACTION.SEND");

            Uri fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (fileUri != null) {
                String filename = getFileName(context, fileUri);
                Log.d(TAG, "File received! Uri: " + fileUri);
                Log.d(TAG, "Filename: \"" + filename + "\"");
                openPopup(getDestinationName(filename), fileUri);
            } else {
                // TODO: Error Handling
            }
        }
        if (Intent.ACTION_VIEW.equals(action)
                && type != null) {
            Log.d(TAG, "Started from ACTION.VIEW");

            Uri fileUri = intent.getData();
            if (fileUri != null) {
                String filename = getFileName(context, fileUri);
                Log.d(TAG, "File received! Uri: " + fileUri);
                Log.d(TAG, "Filename: \"" + filename + "\"");
                openPopup(getDestinationName(filename), fileUri);
            } else {
                // TODO: Error Handling
            }
        }
    }

    private void openPopup(String filename, Uri fileUri){
        StandardPopup standardPopup = new StandardPopup(context, StandardPopup.MODE_ENTER_FILENAME,
                context.getResources().getString(R.string.import_document_title),
                context.getResources().getString(R.string.import_document_message),
                context.getResources().getString(R.string.button_cancel),
                context.getResources().getString(R.string.button_import));
        standardPopup.addFileNames(documentManager.getNames());
        filename = Strings.CS.removeEnd(filename, ".ewdoc");
        standardPopup.setFilename(filename);
        standardPopup.addPopupListener(new PopupListener() {
            @Override
            public void OnCancel() {
                // Inform listeners
                for (ImportListener listener: listeners){
                    listener.onCancel();
                }
            }

            @Override
            public void OnSelected(String name) {
                copyFile(fileUri, new File(context.getFilesDir() + "/Documents/" + name + ".ewdoc"), context);
                // Inform listeners
                for (ImportListener listener: listeners){
                    listener.onImport(name);
                }
            }

            @Override
            public void OnConfirmed(String name) {

            }
        });
        standardPopup.show();
    }

    public static void copyFile(Uri from, File to, Context context) {
        try(InputStream inputStream = context.getContentResolver().openInputStream(from);
            OutputStream outputStream = new FileOutputStream(to)){
                byte[] buffer = new byte[4096];
                int length;
                while ((length = inputStream.read(buffer)) > 0){
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
        } catch (Exception e){
            e.printStackTrace();
            // TODO: Error-Handling
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

    public String getDestinationName(String filename){
        // Remove .ewdoc from filename
        if (!filename.endsWith(".ewdoc")){
            filename += ".ewdoc";
        }
        // Check if filename already exist
        ArrayList<DocumentGridData> documents = documentManager.getDocuments();
        for (DocumentGridData data: documents) {
            if (Objects.equals(data.getFilename(), filename)) {
                filename = adaptFileName(data.getTitle(), documents);
                break;
            }
        }
        return filename;
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

    // Generated by Github copilot reviewed by me
    public static String getFileName(Context context, Uri uri) {
        if (uri == null) return null;

        // 1) Try query OpenableColumns.DISPLAY_NAME
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx != -1) {
                        String name = cursor.getString(idx);
                        if (name != null && !name.isEmpty()) return name;
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error querying display name for uri: " + uri, e);
            } finally {
                if (cursor != null) cursor.close();
            }

            // 2) Try DocumentFile for SAF/document URIs
            try {
                DocumentFile doc = DocumentFile.fromSingleUri(context, uri);
                if (doc.getName() != null && !doc.getName().isEmpty()) {
                    return doc.getName();
                }
            } catch (Exception e) {
                // ignore; best-effort only
            }
        }

        // 3) Fallback: last path segment (works for file:// and some content Uris)
        String path = uri.getPath();
        if (path != null) {
            int cut = path.lastIndexOf('/');
            if (cut != -1 && cut + 1 < path.length()) {
                String last = path.substring(cut + 1);
                if (!last.isEmpty()) return last;
            } else if (!path.isEmpty()) {
                return path;
            }
        }

        return null;
    }

    public void addOnImportListener(ImportListener listener){
        listeners.add(listener);
    }
}
