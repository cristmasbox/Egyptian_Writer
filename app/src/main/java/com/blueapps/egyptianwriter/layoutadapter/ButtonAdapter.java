package com.blueapps.egyptianwriter.layoutadapter;

import static androidx.core.util.TypedValueCompat.dpToPx;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class ButtonAdapter {

    public ButtonAdapter(Context context, Button textButton, ImageButton imageButton, View otherView, View container, int padding){
        container.post(() -> {
            int measuredWidth = (int) (otherView.getWidth() + textButton.getWidth() + dpToPx(padding, context.getResources().getDisplayMetrics()));
            int fullWidth = container.getWidth();
            if(measuredWidth > fullWidth){
                textButton.setVisibility(View.GONE);
                imageButton.setVisibility(View.VISIBLE);
            } else {
                textButton.setVisibility(View.VISIBLE);
                imageButton.setVisibility(View.INVISIBLE);
            }
        });
    }

}
