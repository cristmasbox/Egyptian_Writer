package com.blueapps.egyptianwriter.issuecenter;

import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blueapps.egyptianwriter.R;
import com.blueapps.egyptianwriter.databinding.StandardPopupBinding;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StandardPopup implements TextWatcher {

    StandardPopupBinding binding;
    private int mode = MODE_ENTER_FILENAME;

    ArrayList<String> fileNames = new ArrayList<>();
    private PopupWindow popupWindow;
    private Context context;
    private String filename = "";

    private ArrayList<PopupListener> listeners = new ArrayList<>();

    private String errorEmptyString;
    private String errorInvalidString;
    private String errorRepeatedString;

    // Constants
    public static final int MODE_ENTER_FILENAME = 0;
    public static final int MODE_WARNING = 1;

    public StandardPopup(Context context, int mode,
                         String title, String message,
                         String dismissText, String confirmText){

        this.mode = mode;
        this.context = context;

        errorEmptyString = context.getString(R.string.error_empty_string);
        errorInvalidString = context.getString(R.string.error_invalid_string);
        errorRepeatedString = context.getString(R.string.error_repeated_string);

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        binding = StandardPopupBinding.inflate(layoutInflater);
        popupWindow = new PopupWindow();

        popupWindow.setWidth(ConstraintLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ConstraintLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.setContentView(binding.getRoot());
        popupWindow.setAnimationStyle(R.style.popup_window_animation);

        // Customize popup
        if (mode == MODE_ENTER_FILENAME){
            // Set style
            binding.popupTitle.setTextAppearance(R.style.textViewNormal);
            binding.confirmButton.setTextAppearance(R.style.filledButton);
            binding.confirmButton.setBackground(AppCompatResources.getDrawable(context, R.drawable.custom_button_filled));

            // Set visibility
            binding.popupTitle.setVisibility(View.VISIBLE);
            binding.fileName.setVisibility(View.VISIBLE);
            binding.errorText.setVisibility(View.GONE);
            binding.confirmButton.setVisibility(View.VISIBLE);
            binding.dismissButton.setVisibility(View.VISIBLE);
            if (message != null){
                binding.popupMessage.setVisibility(View.VISIBLE);
            } else {
                binding.popupMessage.setVisibility(View.GONE);
            }

            // Set text
            binding.popupTitle.setText(title);
            binding.popupMessage.setText(message);
            binding.dismissButton.setText(dismissText);
            binding.confirmButton.setText(confirmText);

            binding.fileName.addTextChangedListener(this);

            binding.confirmButton.setOnClickListener(view -> {
                for (PopupListener listener: listeners){
                    listener.OnSelected(String.valueOf(binding.fileName.getText()));
                }
                popupWindow.dismiss();
            });
            binding.dismissButton.setOnClickListener(view -> {
                for (PopupListener listener: listeners){
                    listener.OnCancel();
                }
                popupWindow.dismiss();
            });
        } else {
            // Set style
            binding.popupTitle.setTextAppearance(R.style.errorText);
            binding.confirmButton.setTextAppearance(R.style.filledButtonWarning);
            binding.confirmButton.setBackground(AppCompatResources.getDrawable(context, R.drawable.custom_button_filled_warning));

            // Set visibility
            binding.popupTitle.setVisibility(View.VISIBLE);
            binding.fileName.setVisibility(View.GONE);
            binding.errorText.setVisibility(View.GONE);
            binding.confirmButton.setVisibility(View.VISIBLE);
            binding.dismissButton.setVisibility(View.VISIBLE);
            if (message != null){
                binding.popupMessage.setVisibility(View.VISIBLE);
            } else {
                binding.popupMessage.setVisibility(View.GONE);
            }

            // Set text
            binding.popupTitle.setText(title);
            binding.popupMessage.setText(message);
            binding.dismissButton.setText(dismissText);
            binding.confirmButton.setText(confirmText);

            binding.confirmButton.setOnClickListener(view -> {
                for (PopupListener listener: listeners){
                    listener.OnConfirmed(filename);
                }
                popupWindow.dismiss();
            });
            binding.dismissButton.setOnClickListener(view -> {
                for (PopupListener listener: listeners){
                    listener.OnCancel();
                }
                popupWindow.dismiss();
            });
        }


    }

    public void addFileNames(ArrayList<String> fileNames){
        this.fileNames.addAll(fileNames);
    }

    public void addFileName(String fileName){
        this.fileNames.add(fileName);
    }

    public void setFilename(String filename){
        this.filename = filename;
        binding.fileName.setText(filename);
    }

    public void show(){
        popupWindow.showAtLocation(binding.getRoot(), Gravity.NO_GRAVITY, 0,0); // Displays popup above the anchor view.
    }

    public String suggestFileName(){

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
            binding.confirmButton.setClickable(false);
            binding.confirmButton.setEnabled(false);
        } else if (StringUtils.containsAny(charSequence, '"', '*', '/', ':', '<', '>', '?', '\\', '|', (char) 0x7F)){
            binding.errorText.setText(errorInvalidString);
            binding.errorText.setVisibility(View.VISIBLE);
            binding.confirmButton.setClickable(false);
            binding.confirmButton.setEnabled(false);
        } else if (fileNames.contains(charSequence.toString())){
            binding.errorText.setText(errorRepeatedString);
            binding.errorText.setVisibility(View.VISIBLE);
            binding.confirmButton.setClickable(false);
            binding.confirmButton.setEnabled(false);
        } else {
            binding.errorText.setVisibility(View.GONE);
            binding.confirmButton.setClickable(true);
            binding.confirmButton.setEnabled(true);
        }
    }

    public void addPopupListener(PopupListener listener){
        listeners.add(listener);
    }

}
