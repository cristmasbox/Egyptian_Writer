package com.blueapps.egyptianwriter.issuecenter;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.blueapps.egyptianwriter.R;
import com.blueapps.egyptianwriter.databinding.IssuePopupBinding;

import java.util.ArrayList;

public class Issue {

    IssuePopupBinding binding;
    private PopupWindow popupWindow;
    private Context context;
    private String issueTitle;
    private String issueMessage;
    private String issueCode;

    private ArrayList<IssueListener> listeners = new ArrayList<>();

    public Issue(Context context, String issueTitle, String issueMessage, String issueCode){

        this.context = context;
        this.issueTitle = issueTitle;
        this.issueMessage = issueMessage;
        this.issueCode = issueCode;

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        binding = IssuePopupBinding.inflate(layoutInflater);
        popupWindow = new PopupWindow();

        popupWindow.setWidth(ConstraintLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ConstraintLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.setContentView(binding.getRoot());
        popupWindow.setAnimationStyle(R.style.popup_window_animation);

        binding.issueTitle.setText(issueTitle);
        binding.issueMessage.setText(issueMessage + "\n\n" + context.getString(R.string.issue_prefix));
        binding.issueCode.setText(issueCode);

        binding.okButton.setOnClickListener(view -> {
            for (IssueListener listener: listeners){
                listener.onFinish();
            }
            popupWindow.dismiss();
        });

        binding.buttonCopy.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(context.getString(R.string.label_error_code), issueCode);
            clipboard.setPrimaryClip(clip);
        });

    }

    public void show(){
        popupWindow.showAtLocation(binding.getRoot(), Gravity.NO_GRAVITY, 0,0); // Displays popup above the anchor view.
    }

    public void addOnDeleteListener(IssueListener listener){
        listeners.add(listener);
    }

}
