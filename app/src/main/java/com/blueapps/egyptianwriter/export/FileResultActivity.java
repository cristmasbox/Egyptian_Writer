package com.blueapps.egyptianwriter.export;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
import static androidx.core.content.FileProvider.getUriForFile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.blueapps.egyptianwriter.R;
import com.blueapps.egyptianwriter.dashboard.DashboardActivity;
import com.blueapps.egyptianwriter.databinding.ActivityFileResultBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileResultActivity extends AppCompatActivity implements ActivityResultCallback<Uri>{

    private ActivityFileResultBinding binding;

    private ActivityResultLauncher<String> saveResultLauncher;

    private File resultFile;

    // Views
    private ImageButton buttonBack;
    private TextView fileNameVert;
    private Button buttonSave;
    private Button buttonShare;

    // Constants,
    public static final String MIME_DEFAULT = "application/octet-stream";
    public static final String MIME_EWDOC = "application/vnd.com.blueapps.egyptianwriter.ewdoc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFileResultBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // get Extras
        Intent intent = getIntent();
        String filename = intent.getStringExtra(DashboardActivity.KEY_FILE_NAME);

        // Set names for Views
        buttonBack = binding.buttonBack;
        fileNameVert = binding.fileNameVert;
        buttonSave = binding.buttonSave;
        buttonShare = binding.buttonShare;

        fileNameVert.setText(filename);

        resultFile = new File(getFilesDir() + "/Documents/" + filename);

        buttonBack.setOnClickListener(view -> {
            finish();
        });

        buttonSave.setOnClickListener(view -> {
            createFile(filename);
        });

        buttonShare.setOnClickListener(view -> {
            shareFile(resultFile);
        });

        saveResultLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument(MIME_EWDOC), this);

    }

    private void createFile(String filename) {
        saveResultLauncher.launch(filename);
    }

    private void copyFile(File from, Uri to){

        try (InputStream is = new FileInputStream(from); OutputStream os = getContentResolver().openOutputStream(to)) {
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

    private void shareFile(File file){
        Uri uri = getUriForFile(this, "com.blueapps.fileprovider", file);
        String[] mimetypes = {MIME_EWDOC};
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.addFlags(FLAG_GRANT_WRITE_URI_PERMISSION);
        shareIntent.setType(MIME_EWDOC);
        shareIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)));
    }

    @Override
    public void onActivityResult(Uri destination) {
        copyFile(resultFile, destination);
    }
}