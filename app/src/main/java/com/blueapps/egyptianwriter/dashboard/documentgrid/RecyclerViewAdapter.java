package com.blueapps.egyptianwriter.dashboard.documentgrid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.blueapps.egyptianwriter.R;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>{
    private ArrayList<DocumentGridData> dataList;
    private Context context;

    private ArrayList<DocumentListener> listeners = new ArrayList<>();

    public RecyclerViewAdapter(Context context, ArrayList<DocumentGridData> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate Layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.document_card, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        DocumentGridData data = dataList.get(position);
        holder.dataTitle.setText(data.getTitle());
        holder.buttonMore.setOnClickListener(view -> {
            DocumentMenu documentMenu = new DocumentMenu(context);

            int[] location = new int[2];
            holder.buttonMore.getLocationOnScreen(location);
            documentMenu.setPosition(location[0], location[1]);

            documentMenu.addDocumentMenuListener(new DocumentMenuListener() {
                @Override
                public void OnCancel() {

                }

                @Override
                public void OnExport() {
                    for (DocumentListener listener: listeners){
                        listener.OnExportDocument(data.getTitle());
                    }
                }

                @Override
                public void OnDelete() {
                    for (DocumentListener listener: listeners){
                        listener.OnDeleteDocument(data.getTitle());
                    }
                }
            });

            documentMenu.show();
        });

        holder.cardView.setOnClickListener(view -> {
            for (DocumentListener listener: listeners){
                listener.OnOpenDocument(data.getTitle());
            }
        });
    }

    @Override
    public int getItemCount() {
        // this method returns the size of recyclerview
        return dataList.size();
    }

    public void addDocumentListener(DocumentListener listener){
        listeners.add(listener);
    }

    public void removeDocumentListeners(){
        listeners.clear();
    }

    // View Holder Class to handle Recycler View.
    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView dataTitle;
        private ImageButton buttonMore;
        private CardView cardView;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            dataTitle = itemView.findViewById(R.id.data_title);
            buttonMore = itemView.findViewById(R.id.button_more);
            cardView = itemView.findViewById(R.id.cardview);
        }
    }
}
