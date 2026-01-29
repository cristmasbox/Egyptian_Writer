package com.blueapps.egyptianwriter.dashboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blueapps.egyptianwriter.R;
import com.blueapps.egyptianwriter.dashboard.createdocument.AddMenu;
import com.blueapps.egyptianwriter.dashboard.createdocument.AddMenuListener;
import com.blueapps.egyptianwriter.issuecenter.StandardPopup;
import com.blueapps.egyptianwriter.editor.DocumentEditorActivity;
import com.blueapps.egyptianwriter.issuecenter.PopupListener;
import com.blueapps.egyptianwriter.databinding.DashboardBinding;
import com.blueapps.egyptianwriter.dashboard.documentgrid.DocumentGridData;
import com.blueapps.egyptianwriter.dashboard.documentgrid.DocumentListener;
import com.blueapps.egyptianwriter.dashboard.documentgrid.DocumentManager;
import com.blueapps.egyptianwriter.dashboard.documentgrid.DocumentGridAdapter;
import com.blueapps.egyptianwriter.export.FileResultActivity;
import com.blueapps.egyptianwriter.fileimport.ImportListener;
import com.blueapps.egyptianwriter.fileimport.ImportManager;
import com.blueapps.egyptianwriter.layoutadapter.ButtonAdapter;
import com.blueapps.egyptianwriter.layoutadapter.GridAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity implements DocumentListener, PopupListener, ImportListener {

    DashboardBinding binding;
    private static final String TAG = "DashboardActivity";
    private Intent intent;
    private boolean hasStarted = false;

    private DocumentManager documentManager;
    private ImportManager importManager;

    // Views
    private ConstraintLayout dataContainer;
    private RecyclerView documentGrid;
    private Button addDocument;
    private ImageButton addDocumentSmall;
    private TextView documentTitle;
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
        intent = getIntent();

        // Set names for Views
        dataContainer = binding.dataContainer;
        documentGrid = binding.documentGrid;
        addDocument = binding.addDocument;
        addDocumentSmall = binding.addDocumentSmall;
        documentTitle = binding.documentTitle;
        noDocumentContainer = binding.noDocumentContainer;

        // Set up document grid
        documentManager = new DocumentManager(this);

        // Set up import
        importManager = new ImportManager(this, documentManager);
        importManager.setActivityResultLauncher(registerForActivityResult(new ActivityResultContracts.OpenDocument(), importManager));

        // Set up grid
        DocumentGridAdapter adapter = new DocumentGridAdapter(this, getDocuments(documentManager));
        adapter.removeDocumentListeners();
        adapter.addDocumentListener(this);
        GridLayoutManager gridManager = new GridLayoutManager(this, 2);

        // adapt layout
        new GridAdapter(documentGrid, DPtoPX(200), gridManager);
        new ButtonAdapter(this, addDocument, addDocumentSmall, documentTitle, dataContainer, 32);

        documentGrid.setLayoutManager(gridManager);
        documentGrid.setAdapter(adapter);

        addDocument.setOnClickListener(view -> {
            addDocument();
        });
        addDocumentSmall.setOnClickListener(view -> {
            addDocument();
        });

    }

    private void addDocument(){
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

                    StandardPopup standardPopup = new StandardPopup(DashboardActivity.this, StandardPopup.MODE_ENTER_FILENAME,
                            getResources().getString(R.string.new_document_title), null,
                            getResources().getString(R.string.button_cancel),
                            getResources().getString(R.string.button_create));

                    standardPopup.addFileNames(documentManager.getNames());
                    standardPopup.setFilename(standardPopup.suggestFileName());
                    standardPopup.show();
                    standardPopup.addPopupListener(DashboardActivity.this);
                });
            }
        });

        addMenu.show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Handle intents
        if (!hasStarted) {
            importManager.handleImportIntents(intent);
            importManager.addOnImportListener(DashboardActivity.this);
            hasStarted = true;
        }
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

    // Listeners

    // Document Listener
    @Override
    public void OnDeleteDocument(String name) {

        StandardPopup standardPopup = new StandardPopup(DashboardActivity.this, StandardPopup.MODE_WARNING,
                getResources().getString(R.string.delete_document),
                String.format(getResources().getString(R.string.delete_message_program), name),
                getResources().getString(R.string.button_cancel),
                getResources().getString(R.string.button_delete));
        standardPopup.addPopupListener(DashboardActivity.this);
        standardPopup.setFilename(name);
        standardPopup.show();

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
        DocumentGridAdapter adapter = new DocumentGridAdapter(DashboardActivity.this, getDocuments(documentManager));
        adapter.removeDocumentListeners();
        adapter.addDocumentListener(DashboardActivity.this);
        documentGrid.setAdapter(adapter);
    }

    @Override
    public void onError() {

    }

    @Override
    public void onCancel() {

    }


    // StandardPopup listener
    @Override
    public void OnCancel() {

    }

    @Override
    public void OnSelected(String name) {
        documentManager.addDocument(name);
        DocumentGridAdapter adapter = new DocumentGridAdapter(DashboardActivity.this, getDocuments(documentManager));
        adapter.removeDocumentListeners();
        adapter.addDocumentListener(DashboardActivity.this);
        documentGrid.setAdapter(adapter);
    }

    @Override
    public void OnConfirmed(String name) {
        documentManager.deleteDocument(name);
        DocumentGridAdapter adapter = new DocumentGridAdapter(DashboardActivity.this, getDocuments(documentManager));
        adapter.removeDocumentListeners();
        adapter.addDocumentListener(DashboardActivity.this);
        documentGrid.setAdapter(adapter);
    }
}