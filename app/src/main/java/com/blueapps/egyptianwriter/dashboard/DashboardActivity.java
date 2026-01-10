package com.blueapps.egyptianwriter.dashboard;

import static com.blueapps.egyptianwriter.export.FileResultActivity.MIME_DEFAULT;
import static com.blueapps.egyptianwriter.export.FileResultActivity.MIME_EWDOC;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blueapps.egyptianwriter.dashboard.createdocument.AddMenu;
import com.blueapps.egyptianwriter.dashboard.createdocument.AddMenuListener;
import com.blueapps.egyptianwriter.editor.DocumentEditorActivity;
import com.blueapps.egyptianwriter.dashboard.createdocument.CreateDocument;
import com.blueapps.egyptianwriter.dashboard.createdocument.CreateDocumentListener;
import com.blueapps.egyptianwriter.databinding.DashboardBinding;
import com.blueapps.egyptianwriter.dashboard.deletedocument.DeleteDocument;
import com.blueapps.egyptianwriter.dashboard.deletedocument.DeleteDocumentListener;
import com.blueapps.egyptianwriter.dashboard.documentgrid.DocumentGridData;
import com.blueapps.egyptianwriter.dashboard.documentgrid.DocumentListener;
import com.blueapps.egyptianwriter.dashboard.documentgrid.DocumentManager;
import com.blueapps.egyptianwriter.dashboard.documentgrid.DocumentGridAdapter;
import com.blueapps.egyptianwriter.export.FileResultActivity;
import com.blueapps.egyptianwriter.fileimport.ImportListener;
import com.blueapps.egyptianwriter.fileimport.ImportManager;
import com.blueapps.egyptianwriter.layoutadapter.GridAdapter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity implements DocumentListener, CreateDocumentListener, ImportListener {

    DashboardBinding binding;
    private static final String TAG = "DashboardActivity";

    private DocumentManager documentManager;
    private ImportManager importManager;

    // Views
    private RecyclerView documentGrid;
    private Button addDocument;
    private ConstraintLayout noDocumentContainer;

    // Constants
    public static final String KEY_NAME = "name";
    public static final String KEY_FILE_NAME = "filename";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create non fullscreen layout
        binding = DashboardBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set names for Views
        documentGrid = binding.documentGrid;
        addDocument = binding.addDocument;
        noDocumentContainer = binding.noDocumentContainer;

        // Set up document grid
        documentManager = new DocumentManager(this);

        // Set up import
        importManager = new ImportManager(this, documentManager);
        importManager.setActivityResultLauncher(registerForActivityResult(new ActivityResultContracts.OpenDocument(), importManager));

        // Handle intents
        handleIntents();

        DocumentGridAdapter adapter = new DocumentGridAdapter(this, getDocuments(documentManager));
        adapter.removeDocumentListeners();
        adapter.addDocumentListener(this);
        GridLayoutManager gridManager = new GridLayoutManager(this, 2);
        new GridAdapter(documentGrid, DPtoPX(200), gridManager);

        documentGrid.setLayoutManager(gridManager);
        documentGrid.setAdapter(adapter);


        addDocument.setOnClickListener(view -> {
            AddMenu addMenu = new AddMenu(this);
            int[] location = new int[2];
            addDocument.getLocationOnScreen(location);
            addMenu.setPosition(location[0], location[1] + 40);
            addMenu.addAddMenuListener(new AddMenuListener() {

                @Override
                public void OnCancel() {

                }

                @Override
                public void OnImport() {
                    importManager.addOnImportListener(DashboardActivity.this);
                    importManager.showDialog();
                }

                @Override
                public void OnCreate() {
                    DashboardActivity.this.runOnUiThread(() -> {
                        CreateDocument createDocument = new CreateDocument(DashboardActivity.this, documentManager.getNames());
                        createDocument.show();
                        createDocument.addOnCreateListener(DashboardActivity.this);
                    });
                }
            });

            addMenu.show();
        });

    }

    private ArrayList<DocumentGridData> getDocuments(DocumentManager documentManager){
        ArrayList<DocumentGridData> documentGridData = documentManager.getDocuments();

        if (documentGridData.isEmpty()){
            noDocumentContainer.setVisibility(View.VISIBLE);
            documentGrid.setVisibility(View.GONE);
        } else {
            noDocumentContainer.setVisibility(View.GONE);
            documentGrid.setVisibility(View.VISIBLE);
        }

        return documentGridData;
    }

    public int DPtoPX(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void openFile() {

    }

    private void copyFile(Uri output, File input){

        try (InputStream is = new FileInputStream(input); OutputStream os = getContentResolver().openOutputStream(output)) {
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

    private void handleIntents(){
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if ((Intent.ACTION_SEND.equals(action) || Intent.ACTION_VIEW.equals(action)) && type != null) {
            if (MIME_DEFAULT.equals(type) || MIME_EWDOC.equals(type)) {
                Log.d(TAG, "Started from ACTION.SEND");
                Uri fileUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (fileUri != null) {
                    Log.d(TAG, "File received! Uri: " + fileUri.getPath());
                    File file = new File(fileUri.getPath());
                    if (Strings.CS.endsWith(file.getName(), ".ewdoc")){
                        importManager.copyFile(fileUri, importManager.getDestinationUri(file.getName()));
                        Log.d(TAG, "File copied!");
                    } else {
                        Log.d(TAG, "Wrong file extension!");
                        // TODO: Error handling
                    }
                }
            }
        }
    }

    // Listeners

    // Document Listener
    @Override
    public void OnDeleteDocument(String name) {

        DeleteDocument deleteDocument = new DeleteDocument(this, name);
        deleteDocument.show();
        deleteDocument.addOnDeleteListener(new DeleteDocumentListener() {
            @Override
            public void OnCancel() {

            }

            @Override
            public void OnDelete() {
                documentManager.deleteDocument(name);
                DocumentGridAdapter adapter = new DocumentGridAdapter(DashboardActivity.this, getDocuments(documentManager));
                adapter.removeDocumentListeners();
                adapter.addDocumentListener(DashboardActivity.this);
                documentGrid.setAdapter(adapter);
            }
        });
    }

    @Override
    public void OnExportDocument(String name) {
        Intent myIntent = new Intent(DashboardActivity.this, FileResultActivity.class);
        // Add extras
        myIntent.putExtra(KEY_FILE_NAME, name + ".ewdoc");
        DashboardActivity.this.startActivity(myIntent);
    }

    @Override
    public void OnOpenDocument(String name) {
        Intent myIntent = new Intent(DashboardActivity.this, DocumentEditorActivity.class);
        // Add extras
        myIntent.putExtra(KEY_NAME, name);
        myIntent.putExtra(KEY_FILE_NAME, name + ".ewdoc");
        DashboardActivity.this.startActivity(myIntent);
    }


    // Import Listener
    @Override
    public void onImport(String name) {
        documentManager.addDocument(name);
        DocumentGridAdapter adapter = new DocumentGridAdapter(DashboardActivity.this, getDocuments(documentManager));
        adapter.removeDocumentListeners();
        adapter.addDocumentListener(DashboardActivity.this);
        documentGrid.setAdapter(adapter);
    }

    @Override
    public void onError() {

    }


    // CreateDocument listener
    @Override
    public void OnCancel() {

    }

    @Override
    public void OnCreate(String name) {
        documentManager.addDocument(name);
        DocumentGridAdapter adapter = new DocumentGridAdapter(DashboardActivity.this, getDocuments(documentManager));
        adapter.removeDocumentListeners();
        adapter.addDocumentListener(DashboardActivity.this);
        documentGrid.setAdapter(adapter);
    }
}