package com.blueapps.egyptianwriter.layoutadapter;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GridAdapter {

    public GridAdapter(RecyclerView grid, int maxLength, GridLayoutManager gridManager){

        grid.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int width = grid.getWidth();
            int columnCount = (width / maxLength) + 1;
            gridManager.setSpanCount(columnCount);
        });
    }

}
