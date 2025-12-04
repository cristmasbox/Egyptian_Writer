package com.blueapps.egyptianwriter.dashboard.deletedocument;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.PopupWindow;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.blueapps.egyptianwriter.R;
import com.blueapps.egyptianwriter.databinding.DeleteDocumentBinding;

import java.util.ArrayList;

public class DeleteDocument{

    DeleteDocumentBinding binding;
    private PopupWindow popupWindow;
    private Context context;
    private String name = "";

    private ArrayList<DeleteDocumentListener> listeners = new ArrayList<>();

    public DeleteDocument(Context context, String name){

        this.context = context;
        this.name = name;

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        binding = DeleteDocumentBinding.inflate(layoutInflater);
        popupWindow = new PopupWindow();

        popupWindow.setWidth(ConstraintLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ConstraintLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setFocusable(true);
        popupWindow.setContentView(binding.getRoot());
        popupWindow.setAnimationStyle(R.style.popup_window_animation);

        binding.warningMessage.setText(String.format(context.getResources().getString(R.string.delete_message_program), name));

        binding.deleteButton.setOnClickListener(view -> {
            for (DeleteDocumentListener listener: listeners){
                listener.OnDelete();
            }
            popupWindow.dismiss();
        });
        binding.dismissButton.setOnClickListener(view -> {
            for (DeleteDocumentListener listener: listeners){
                listener.OnCancel();
            }
            popupWindow.dismiss();
        });

    }

    public void show(){
        popupWindow.showAtLocation(binding.getRoot(), Gravity.NO_GRAVITY, 0,0); // Displays popup above the anchor view.
    }

    public void addOnDeleteListener(DeleteDocumentListener listener){
        listeners.add(listener);
    }

}
