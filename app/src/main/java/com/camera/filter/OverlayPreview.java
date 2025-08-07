package com.camera.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class OverlayPreview {

    private Context context;

    public OverlayPreview(Context context) {
        this.context = context;
        vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertices);
        vertexBuffer.position(0);

        textureBuffer = ByteBuffer.allocateDirect(textureCoords.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureCoords);
        textureBuffer.position(0);

        program = createProgram();

        positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        textureHandle = GLES20.glGetAttribLocation(program, "aTextureCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        samplerHandle = GLES20.glGetUniformLocation(program, "sTexture");
    }

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;

    private int program;

    // Vertex coordinates
    private float[] vertices = {
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            -1.0f,  1.0f, 0.0f,
            1.0f,  1.0f, 0.0f
    };

    private float[] textureCoords = {
            1.0f, 1.0f,  // Bottom-right -> Top-left
            1.0f, 0.0f,  // Top-right -> Bottom-left
            0.0f, 1.0f,  // Bottom-left -> Top-right
            0.0f, 0.0f   // Top-left -> Bottom-right
    };

    private int positionHandle = 0;
    private int textureHandle = 0;
    private int mvpMatrixHandle = 0;
    private int samplerHandle = 0;


    private String vertexShaderCode =
            "attribute vec4 aPosition;\n" +
                    "attribute vec2 aTextureCoord;\n" +
                    "uniform mat4 uMVPMatrix;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "\n" +
                    "void main() {\n" +
                    "    gl_Position = uMVPMatrix * aPosition;\n" +
                    "    vTextureCoord = aTextureCoord;\n" +
                    "}\n";

    private String fragmentShaderCode =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform sampler2D uTexture;\n" +
                    "\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(uTexture, vTextureCoord);\n" +
                    "}\n";


    private int createProgram(){
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        int program = GLES20.glCreateProgram();

        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        return program;
    }

    private int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public void draw(int textureId, float[] mvpMatrix){

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUseProgram(program);

        // Set vertex attributes
        GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glEnableVertexAttribArray(0);
        GLES20.glEnableVertexAttribArray(1);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(0);
        GLES20.glDisableVertexAttribArray(1);
    }

    private int loadTexture(String assetPath) {
        int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            try {
                // Load bitmap từ assets
                InputStream inputStream = context.getAssets().open(assetPath);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                // Bind texture
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

                // Set filter parameters
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

                // Load bitmap vào texture
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

                bitmap.recycle();

            } catch (IOException e) {
                throw new RuntimeException("Error loading texture from assets: " + assetPath, e);
            }
        }

        return textureHandle[0];
    }
}
