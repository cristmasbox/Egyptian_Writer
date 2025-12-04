package com.blueapps.egyptianwriter.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blueapps.egyptianwriter.editor.DocumentEditorActivity;
import com.blueapps.egyptianwriter.dashboard.createdocument.CreateDocument;
import com.blueapps.egyptianwriter.dashboard.createdocument.CreateDocumentListener;
import com.blueapps.egyptianwriter.databinding.DashboardBinding;
import com.blueapps.egyptianwriter.dashboard.deletedocument.DeleteDocument;
import com.blueapps.egyptianwriter.dashboard.deletedocument.DeleteDocumentListener;
import com.blueapps.egyptianwriter.dashboard.documentgrid.DocumentGridData;
import com.blueapps.egyptianwriter.dashboard.documentgrid.DocumentListener;
import com.blueapps.egyptianwriter.dashboard.documentgrid.DocumentManager;
import com.blueapps.egyptianwriter.dashboard.documentgrid.RecyclerViewAdapter;
import com.blueapps.egyptianwriter.layoutadapter.GridAdapter;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity implements DocumentListener {

    DashboardBinding binding;

    private DocumentManager documentManager;

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
        documentGrid = binding.grid;
        addDocument = binding.addDocument;
        noDocumentContainer = binding.noDocumentContainer;

        // Set up grid
        documentManager = new DocumentManager(this);

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, getDocuments(documentManager));
        adapter.removeDocumentListeners();
        adapter.addDocumentListener(this);
        GridLayoutManager gridManager = new GridLayoutManager(this, 2);
        new GridAdapter(documentGrid, DPtoPX(200), gridManager);

        documentGrid.setLayoutManager(gridManager);
        documentGrid.setAdapter(adapter);

        addDocument.setOnClickListener(view -> {
            this.runOnUiThread(() -> {
                CreateDocument createDocument = new CreateDocument(DashboardActivity.this, documentManager.getNames());
                createDocument.show();
                createDocument.addOnCreateListener(new CreateDocumentListener() {
                    @Override
                    public void OnCancel() {

                    }

                    @Override
                    public void OnCreate(String name) {
                        documentManager.addDocument(name);
                        RecyclerViewAdapter adapter = new RecyclerViewAdapter(DashboardActivity.this, getDocuments(documentManager));
                        adapter.removeDocumentListeners();
                        adapter.addDocumentListener(DashboardActivity.this);
                        documentGrid.setAdapter(adapter);
                    }
                });
            });
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
                RecyclerViewAdapter adapter = new RecyclerViewAdapter(DashboardActivity.this, getDocuments(documentManager));
                adapter.removeDocumentListeners();
                adapter.addDocumentListener(DashboardActivity.this);
                documentGrid.setAdapter(adapter);
            }
        });
    }

    @Override
    public void OnExportDocument(String name) {

    }

    @Override
    public void OnOpenDocument(String name) {
        Intent myIntent = new Intent(DashboardActivity.this, DocumentEditorActivity.class);
        // Add extras
        myIntent.putExtra(KEY_NAME, name);
        myIntent.putExtra(KEY_FILE_NAME, name + ".ewdoc");
        DashboardActivity.this.startActivity(myIntent);
    }

    public int DPtoPX(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}