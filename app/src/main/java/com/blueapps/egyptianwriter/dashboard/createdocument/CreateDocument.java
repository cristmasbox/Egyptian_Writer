package com.blueapps.egyptianwriter.dashboard.createdocument;

import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.blueapps.egyptianwriter.R;
import com.blueapps.egyptianwriter.databinding.CreateNewDocumentBinding;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateDocument implements TextWatcher {

    CreateNewDocumentBinding binding;
    ArrayList<String> fileNames;
    private PopupWindow popupWindow;
    private Context context;

    private ArrayList<CreateDocumentListener> listeners = new ArrayList<>();

    private String errorEmptyString;
    private String errorInvalidString;
    private String errorRepeatedString;

    public CreateDocument(Context context, ArrayList<String> fileNames){

        this.fileNames = fileNames;
        this.context = context;

        errorEmptyString = context.getString(R.string.error_empty_string);
        errorInvalidString = context.getString(R.string.error_invalid_string);
        errorRepeatedString = context.getString(R.string.error_repeated_string);

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        binding = CreateNewDocumentBinding.inflate(layoutInflater);
        popupWindow = new PopupWindow();

        popupWindow.setWidth(ConstraintLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ConstraintLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.setContentView(binding.getRoot());
        popupWindow.setAnimationStyle(R.style.popup_window_animation);

        binding.fileName.setText(suggestFileName());

        binding.fileName.addTextChangedListener(this);

        binding.createButton.setOnClickListener(view -> {
            for (CreateDocumentListener listener: listeners){
                listener.OnCreate(String.valueOf(binding.fileName.getText()));
            }
            popupWindow.dismiss();
        });
        binding.dismissButton.setOnClickListener(view -> {
            for (CreateDocumentListener listener: listeners){
                listener.OnCancel();
            }
            popupWindow.dismiss();
        });

    }

    public void show(){
        popupWindow.showAtLocation(binding.getRoot(), Gravity.NO_GRAVITY, 0,0); // Displays popup above the anchor view.
    }

    private String suggestFileName(){

        String defaultPrefix = context.getResources().getString(R.string.default_file_prefix);
        ArrayList<Integer> numbers = new ArrayList<>();
        for (String name: fileNames) {

            Pattern pattern = Pattern.compile("^" + defaultPrefix + "[0-9]+$");
            Matcher matcher = pattern.matcher(name);
            boolean matchFound = matcher.find();
            if (matchFound) {
                String sNumber = name.substring(defaultPrefix.length());
                if (NumberUtils.isCreatable(sNumber)) {
                    int number = Integer.parseInt(sNumber);
                    numbers.add(number);
                }
            }
        }
        int suggestedNumber = -1;
        Collections.sort(numbers);
        if(!numbers.contains(1)){
            suggestedNumber = 1;
        } else {
            boolean noGaps = true;
            int lastNumber = 0;
            for (int number: numbers){
                if (!(number == lastNumber + 1)){
                    suggestedNumber = lastNumber + 1;
                    noGaps = false;
                    break;
                }
                lastNumber = number;
            }
            if (noGaps){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    suggestedNumber = numbers.getLast() + 1;
                } else {
                    suggestedNumber = numbers.get(numbers.size() - 1) + 1;
                }
            }
        }

        String suggestedString = defaultPrefix + suggestedNumber;

        return suggestedString;
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        checkForErrors(charSequence);
    }

    private void checkForErrors(CharSequence charSequence){
        if (StringUtils.isBlank(charSequence)){
            binding.errorText.setText(errorEmptyString);
            binding.errorText.setVisibility(View.VISIBLE);
            binding.createButton.setClickable(false);
            binding.createButton.setEnabled(false);
        } else if (StringUtils.containsAny(charSequence, '"', '*', '/', ':', '<', '>', '?', '\\', '|', (char) 0x7F)){
            binding.errorText.setText(errorInvalidString);
            binding.errorText.setVisibility(View.VISIBLE);
            binding.createButton.setClickable(false);
            binding.createButton.setEnabled(false);
        } else if (fileNames.contains(charSequence.toString())){
            binding.errorText.setText(errorRepeatedString);
            binding.errorText.setVisibility(View.VISIBLE);
            binding.createButton.setClickable(false);
            binding.createButton.setEnabled(false);
        } else {
            binding.errorText.setVisibility(View.GONE);
            binding.createButton.setClickable(true);
            binding.createButton.setEnabled(true);
        }
    }

    public void addOnCreateListener(CreateDocumentListener listener){
        listeners.add(listener);
    }

}
