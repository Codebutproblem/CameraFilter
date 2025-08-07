package com.camera.filter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class CaptureActivity extends AppCompatActivity {

    ImageView ivCapture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        String imagePath = getIntent().getStringExtra(MainActivity.class.getName());
        ivCapture = findViewById(R.id.iv_capture);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        ivCapture.setImageBitmap(bitmap);

    }
}