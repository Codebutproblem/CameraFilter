package com.camera.filter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private CameraGLSurfaceView cameraGLSurfaceView;
    private CameraGLRenderer cameraGLRenderer;

    private ImageButton btnCapture, btnRotate;

    private ProgressDialog progressDialog;

    private Camera camera;

    private RecyclerView filterRecyclerView, overlayRecyclerView;
    private FilterAdapter filterAdapter;

    private OverlayAdapter overlayAdapter;

    private List<Overlay> overlays = List.of(
            new Overlay(0, null),
            new Overlay(1, "scratch/scratch1.png"),
            new Overlay(2, "scratch/scratch2.png"),
            new Overlay(3, "scratch/scratch3.png"),
            new Overlay(4, "scratch/scratch4.png"),
            new Overlay(5, "scratch/scratch5.png")
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraGLSurfaceView = findViewById(R.id.camera_surface_view);
        btnCapture = findViewById(R.id.btn_capture);
        filterRecyclerView = findViewById(R.id.rv_filter);
        btnRotate = findViewById(R.id.btn_rotate);
        overlayRecyclerView = findViewById(R.id.rv_overlay);

        cameraGLRenderer = new CameraGLRenderer(this);
        cameraGLSurfaceView.setCameraGLRenderer(cameraGLRenderer);

        filterAdapter = new FilterAdapter(FilterType.getAllFilters(), filterType -> {
            cameraGLRenderer.setFilterType(filterType);
            cameraGLSurfaceView.requestRender();
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        filterRecyclerView.setLayoutManager(layoutManager);
        filterRecyclerView.setAdapter(filterAdapter);


        overlayAdapter = new OverlayAdapter(overlays, overlay -> {
            if(overlay.getId() == 0){
                cameraGLRenderer.clearOverlay();
            }else{
                cameraGLRenderer.setOverlay(overlay.getImagePath());
            }
            cameraGLSurfaceView.requestRender();
        });
        LinearLayoutManager overlayLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        overlayRecyclerView.setLayoutManager(overlayLayoutManager);
        overlayRecyclerView.setAdapter(overlayAdapter);


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading...");

        btnCapture.setOnClickListener(v -> {
            progressDialog.show();
            cameraGLRenderer.requestCapture(bitmap -> {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                intent.putExtra(MainActivity.class.getName(), FileHelper.saveBitmapToFile(MainActivity.this, bitmap));
                progressDialog.dismiss();
                startActivity(intent);
            });
        });

        btnRotate.setOnClickListener(v -> {
            boolean isFrontCamera = camera.rotateCamera();
            camera.setCameraStateListener(new Camera.CameraStateListener() {
                @Override
                public void onCameraOpened() {
                    cameraGLRenderer.updateCameraDirection(isFrontCamera);
                    camera.setCameraStateListener(null);
                }

                @Override
                public void onCameraClosed() {
                    camera.setCameraStateListener(null);
                }

                @Override
                public void onCameraError(String error) {
                    camera.setCameraStateListener(null);
                }
            });
        });
    }

    private void setupCamera() {
        cameraGLRenderer.setSurfaceReadyListener(surfaceTexture -> {
            camera = new Camera(this, surfaceTexture, cameraGLSurfaceView.getWidth(), cameraGLSurfaceView.getHeight());
            camera.openBackCamera();
        });

        cameraGLRenderer.setFrameAvailableListener(surfaceTexture -> {
            cameraGLSurfaceView.requestRender();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCamera();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        }else{
            setupCamera();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        camera.closeCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera = null;
    }
}