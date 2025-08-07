package com.camera.filter;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Pair;

public class CameraGLSurfaceView extends GLSurfaceView {
    public CameraGLSurfaceView(Context context) {
        super(context);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCameraGLRenderer(CameraGLRenderer renderer) {
        setEGLContextClientVersion(2);
        setRenderer(renderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }
}
