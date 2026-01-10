package com.blueapps.egyptianwriter.editor;

import static com.blueapps.egyptianwriter.editor.EditorViewModel.MODE_READ;
import static com.blueapps.egyptianwriter.editor.EditorViewModel.MODE_WRITE;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.blueapps.egyptianwriter.R;
import com.blueapps.egyptianwriter.dashboard.DashboardActivity;
import com.blueapps.egyptianwriter.databinding.ActivityDocumentEditorBinding;
import com.blueapps.egyptianwriter.issuecenter.Issue;
import com.blueapps.thoth.ThothView;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.w3c.dom.Document;

public class DocumentEditorActivity extends AppCompatActivity {

    private ActivityDocumentEditorBinding binding;

    private EditorViewModel viewModel;

    // Views
    private View root;
    private TextView documentTitle;
    private ImageButton buttonBack;
    private ImageButton buttonMode;
    private ThothView thothView;
    private ExpandableLayout expandableLayout;
    private ConstraintLayout background;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create non fullscreen layout
        binding = ActivityDocumentEditorBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        // Optimize for software keyboard on android 15+
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());

            int topInset = Math.max(navInsets.top, systemBars.top);
            int bottomInset = Math.max(imeInsets.bottom, navInsets.bottom);

            root.setPadding(navInsets.left, topInset, navInsets.right, bottomInset);

            return insets;
        });

        // get Extras
        Intent intent = getIntent();
        String name = intent.getStringExtra(DashboardActivity.KEY_NAME);
        String filename = intent.getStringExtra(DashboardActivity.KEY_FILE_NAME);

        // get ViewModel
        viewModel = new ViewModelProvider(this).get(EditorViewModel.class);

        // Set names for Views
        root = binding.getRoot();
        documentTitle = binding.documentTitle;
        buttonBack = binding.buttonBack;
        buttonMode = binding.buttonMode;
        thothView = binding.glyphXView;
        expandableLayout = binding.editorExpandLayout;
        background = binding.editorContainer;
        editText = binding.editText;

        documentTitle.setText(name);
        buttonBack.setOnClickListener(view -> {
            finish();
        });
        buttonMode.setOnClickListener(view -> {
            if (viewModel.getMode()){
                viewModel.setMode(MODE_WRITE);
                buttonMode.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.edit_note));
                expandableLayout.expand(true);
            } else {
                viewModel.setMode(MODE_READ);
                buttonMode.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.opened_book));
                expandableLayout.collapse(true);
            }
        });

        if (viewModel.getMode()) {
            expandableLayout.collapse(false);
        } else {
            expandableLayout.expand(false);
        }

        viewModel.setFileMaster(new FileMaster(this, filename));
        viewModel.getFileMaster().addFileListener(new FileListener() {
            @Override
            public void onGlyphXChanged(Document GlyphX) {
                try {
                    thothView.setGlyphXText(GlyphX);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onMdCChanged(String mdc) {

            }
        });

        try {
            thothView.setGlyphXText(viewModel.getFileMaster().getGlyphX());
        } catch (Exception e){
            e.printStackTrace();
        }
        thothView.setAltText(viewModel.getFileMaster().getMdc());

        editText.setText(viewModel.getFileMaster().getMdc());
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                viewModel.getFileMaster().setMdc(editText.getText().toString());
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ;
            }
        });

        background.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            // Set up height of EditText
            int height = background.getHeight();

            ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) binding.editText.getLayoutParams();
            lp.height = height / 3;
            binding.editText.setLayoutParams(lp);
        });

    }


    @Override
    protected void onDestroy() {

        viewModel.getFileMaster().setMdc(editText.getText().toString());

        super.onDestroy();
    }
}