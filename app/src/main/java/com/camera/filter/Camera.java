package com.camera.filter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.List;

public class Camera{

    private SurfaceTexture surfaceTexture;

    private Context context;

    private CameraDevice cameraDevice;

    private int width;

    private int height;

    private boolean isFrontCamera = true;

    private CameraCaptureSession cameraCaptureSession;

    private CameraStateListener cameraStateListener;

    public void setCameraStateListener(CameraStateListener cameraStateListener) {
        this.cameraStateListener = cameraStateListener;
    }
    public interface CameraStateListener {
        void onCameraOpened();
        void onCameraClosed();
        void onCameraError(String error);
    }
    public Camera(Context context, SurfaceTexture surfaceTexture, int width, int height) {
        this.context = context;
        this.surfaceTexture = surfaceTexture;
        this.width = width;
        this.height = height;
    }

    public void openFrontCamera() {
        closeCamera();
        isFrontCamera = true;
        openCamera(true);
    }

    public void openBackCamera() {
        closeCamera();
        isFrontCamera = false;
        openCamera(false);
    }

    public boolean rotateCamera(){
        if(isFrontCamera){
            openBackCamera();
        } else {
            openFrontCamera();
        }
        return isFrontCamera;
    }

    public void openCamera(boolean isFrontCamera) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = getCameraId(cameraManager, isFrontCamera ? CameraCharacteristics.LENS_FACING_FRONT : CameraCharacteristics.LENS_FACING_BACK);

            if (cameraId != null && ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull CameraDevice camera) {
                        cameraDevice = camera;
                        startPreview();
                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice camera) {
                        cameraDevice = null;
                        camera.close();
                        if(cameraStateListener != null) cameraStateListener.onCameraClosed();
                    }

                    @Override
                    public void onError(@NonNull CameraDevice camera, int error) {
                        cameraDevice = null;
                        camera.close();
                        if(cameraStateListener != null) cameraStateListener.onCameraError("Camera error: " + error);
                    }
                }, null);
            }

        } catch (CameraAccessException e) {
            if(cameraStateListener != null) cameraStateListener.onCameraError(e.getMessage());
        }
    }

    private void startPreview(){
        surfaceTexture.setDefaultBufferSize(width, height);
        @SuppressLint("Recycle")
        Surface surface = new Surface(surfaceTexture);

        try {
            CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            CaptureRequest captureRequest = captureRequestBuilder.build();

            cameraDevice.createCaptureSession(List.of(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    cameraCaptureSession = session;
                    try {
                        session.setRepeatingRequest(captureRequest, null, null);
                        if(cameraStateListener != null) cameraStateListener.onCameraOpened();
                    } catch (CameraAccessException e) {
                        if(cameraStateListener != null) cameraStateListener.onCameraError(e.getMessage());
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    if(cameraStateListener != null) cameraStateListener.onCameraError("Configuration failed");
                }
            }, null);
        } catch (CameraAccessException e) {
            if(cameraStateListener != null) cameraStateListener.onCameraError(e.getMessage());
        } catch (Exception e) {
            if(cameraStateListener != null) cameraStateListener.onCameraError("Unexpected error: " + e.getMessage());
        }
    }

    public void closeCamera(){
        if(cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if(cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }
    }

    public static String getCameraId(CameraManager cameraManager, int lensFacing) {
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (facing != null && facing == lensFacing) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
}
