package com.blueapps.egyptianwriter.dashboard.createdocument;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.PopupWindow;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.blueapps.egyptianwriter.R;
import com.blueapps.egyptianwriter.databinding.AddMenuBinding;
import com.blueapps.egyptianwriter.databinding.DocumentMoreMenuBinding;

import java.util.ArrayList;

public class AddMenu {

    AddMenuBinding binding;
    private PopupWindow popupWindow;
    private Context context;

    private boolean dismissed = true;

    private int x = 0;
    private int y = 0;

    private ArrayList<AddMenuListener> listeners = new ArrayList<>();

    public AddMenu(Context context){

        this.context = context;

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        binding = AddMenuBinding.inflate(layoutInflater);
        popupWindow = new PopupWindow(context);
        popupWindow.setContentView(binding.getRoot());

        popupWindow.setWidth(ConstraintLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ConstraintLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setContentView(binding.getRoot());
        popupWindow.setAnimationStyle(R.style.popup_window_animation);

        popupWindow.setBackgroundDrawable(new ColorDrawable());
        
        binding.importDoc.setOnClickListener(view -> {
            dismissed = false;
            popupWindow.dismiss();
            for (AddMenuListener listener: listeners){
                listener.OnImport();
            }
        });
        binding.createDoc.setOnClickListener(view -> {
            for (AddMenuListener listener: listeners){
                listener.OnCreate();
            }
            dismissed = false;
            popupWindow.dismiss();
        });
        popupWindow.setOnDismissListener(() -> {
            if (dismissed) {
                for (AddMenuListener listener : listeners) {
                    listener.OnCancel();
                }
            }
        });

    }

    public void show(){
        popupWindow.showAtLocation(binding.getRoot(), Gravity.NO_GRAVITY, x + DPtoPX(24), y + DPtoPX(24)); // Displays popup above the anchor view.
    }

    public void addAddMenuListener(AddMenuListener listener){
        listeners.add(listener);
    }

    public void setPosition(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int DPtoPX(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

}
