package com.camera.filter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> {

    private List<FilterType> filterTypes;

    private ItemClickListener itemClickListener;

    public FilterAdapter(List<FilterType> filterTypes, ItemClickListener itemClickListener) {
        this.filterTypes = filterTypes;
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(FilterType filterType);
    }


    @NonNull
    @Override
    public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.filter_item, parent, false);
        return new FilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
        holder.bind(filterTypes.get(position));
    }

    @Override
    public int getItemCount() {
        return filterTypes.size();
    }

    class FilterViewHolder extends RecyclerView.ViewHolder{

        private TextView tvFilterName;
        private View itemView;
        public FilterViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            tvFilterName = itemView.findViewById(R.id.tv_filter_name);
        }

        public void bind(FilterType filterType){
            tvFilterName.setText(filterType.getDisplayName());
            itemView.setOnClickListener(v -> itemClickListener.onItemClick(filterType));
        }
    }
}