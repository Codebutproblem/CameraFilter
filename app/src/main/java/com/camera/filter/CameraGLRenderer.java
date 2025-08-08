package com.camera.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraGLRenderer implements GLSurfaceView.Renderer {

    private Context context;

    public CameraGLRenderer(Context context) {
        this.context = context;
    }

    private final float[] vPMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private SurfaceTexture surfaceTexture;
    private int textureId = 0;
    private boolean updateSurface = false;
    private final Object updateLock = new Object();
    private FrameAvailableListener frameAvailableListener;

    private SurfaceReadyListener surfaceReadyListener;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean captureNextFrame = false;

    private CaptureListener captureListener;

    private int width = 0, height = 0;

    private int overlayTextureId = 0;

    private OverlayPreview overlayPreview;

    public interface FrameAvailableListener {
        void onFrameAvailable(SurfaceTexture surfaceTexture);
    }

    public interface SurfaceReadyListener {
        void onSurfaceReady(SurfaceTexture surfaceTexture);
    }

    public interface CaptureListener {
        void onCapture(Bitmap bitmap);
    }

    private CameraPreview cameraPreview;
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        int[] textures = new int[2];
        GLES20.glGenTextures(2, textures, 0);
        textureId = textures[0];
        overlayTextureId = textures[1];
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, overlayTextureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        surfaceTexture = new SurfaceTexture(textureId);

        cameraPreview = new CameraPreview();

        surfaceTexture.setOnFrameAvailableListener(surfaceTexture -> {
            synchronized (updateLock){
                updateSurface = true;
            }
            handler.post(()-> {
                if (frameAvailableListener != null) {
                    frameAvailableListener.onFrameAvailable(surfaceTexture);
                }
            });
        });

        overlayPreview = new OverlayPreview();

        handler.post(() -> {
            if (surfaceReadyListener != null) {
                surfaceReadyListener.onSurfaceReady(surfaceTexture);
            }
        });
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        if(ratio > 1.0f){
            Matrix.frustumM(projectionMatrix, 0, -1, 1, -1.0f / ratio, 1.0f / ratio, 3, 7);
        } else {
            Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        }
    }

    private boolean applyOverlay = false;

    private String overLayPath;

    @Override
    public void onDrawFrame(GL10 gl) {

        synchronized(updateLock) {
            if (updateSurface) {
                surfaceTexture.updateTexImage();
                updateSurface = false;
            }
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if(isFrontCamera){
            Matrix.setLookAtM(viewMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, -1.0f, 0.0f);
        }else{
            Matrix.setLookAtM(viewMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        }


        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        cameraPreview.draw(vPMatrix);


        if(applyOverlay && overLayPath != null){
            Bitmap overlayBitmap = loadBitmap(overLayPath);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, overlayBitmap, 0);
            applyOverlay = false;
        }
        if(overLayPath != null){
            overlayPreview.draw(vPMatrix);
        }


        if(captureNextFrame){
            captureNextFrame = false;
            captureBitmap();
        }

    }

    private void captureBitmap() {
        int[] buffer = new int[width * height];
        IntBuffer intBuffer = IntBuffer.wrap(buffer);
        intBuffer.position(0);
        GLES20.glReadPixels(
                0, 0, width, height,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, intBuffer
        );

        int[] pixelsBuffer = new int[width * height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int pix = buffer[i * width + j];
                int pb = (pix >> 16) & 0xff;
                int pr = (pix << 16) & 0x00ff0000;
                int pix1 = (pix & 0xff00ff00) | pr | pb;
                pixelsBuffer[(height - i - 1) * width + j] = pix1;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(
                pixelsBuffer, width, height,
                Bitmap.Config.ARGB_8888
        );
        handler.post(()->{
            if(captureListener != null) {
                captureListener.onCapture(bitmap);
            }
        });
    }

    public void requestCapture(CaptureListener captureListener) {
        captureNextFrame = true;
        this.captureListener = captureListener;
    }

    public void setFrameAvailableListener(FrameAvailableListener frameAvailableListener) {
        this.frameAvailableListener = frameAvailableListener;
    }

    public void setSurfaceReadyListener(SurfaceReadyListener surfaceReadyListener) {
        this.surfaceReadyListener = surfaceReadyListener;
        if(surfaceTexture != null){
            surfaceReadyListener.onSurfaceReady(surfaceTexture);
        }
    }

    public void setFilterType(FilterType filterType) {
        cameraPreview.setFilterType(filterType);
    }


    private boolean isFrontCamera = false;

    public void setFrontCamera(boolean frontCamera) {
        isFrontCamera = frontCamera;
    }

    private Bitmap loadBitmap(String assetPath) {
        try {
            InputStream inputStream = context.getAssets().open(assetPath);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return bitmap;
        } catch (IOException e) {
            throw new RuntimeException("Error loading bitmap from assets: " + assetPath, e);
        }
    }

    public void setOverlay(String overLayPath) {
        applyOverlay = true;
        this.overLayPath = overLayPath;
    }

    public void clearOverlay() {
        applyOverlay = false;
        overLayPath = null;
    }

    public void setAlpha(float alpha) {
        overlayPreview.setAlpha(alpha);
    }
}
