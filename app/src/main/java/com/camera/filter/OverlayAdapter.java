package com.camera.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class OverlayAdapter extends RecyclerView.Adapter<OverlayAdapter.ViewHolder> {

    private List<Overlay> overlays;

    private OverlayClickListener overlayClickListener;

    private Context context;

    public interface OverlayClickListener {
        void onClick(Overlay overlay);
    }

    public OverlayAdapter(List<Overlay> overlays, OverlayClickListener overlayClickListener) {
        this.overlays = overlays;
        this.overlayClickListener = overlayClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.overlay_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(overlays.get(position));
    }

    @Override
    public int getItemCount() {
        return overlays.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView ivOverlay;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivOverlay = itemView.findViewById(R.id.iv_overlay);
        }

        public void bind(Overlay overlay) {
            if(overlay.getId() != 0){
                Bitmap bitmap = loadImage(overlay.getImagePath());
                ivOverlay.setImageBitmap(bitmap);
            }
            itemView.setOnClickListener(v -> {
                if (overlayClickListener != null) {
                    overlayClickListener.onClick(overlay);
                }
            });
        }

        private Bitmap loadImage(String imagePath){
            try {
                InputStream inputStream = context.getAssets().open(imagePath);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                return bitmap;
            } catch (IOException e) {
                return null;
            }
        }
    }
}
