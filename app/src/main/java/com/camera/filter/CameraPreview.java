package com.camera.filter;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL;

public class CameraPreview {


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

    private int filterTypeHandle = 0;

    private int currentFilterType = 0;

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
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "uniform int filterType;\n" +
                    "\n" +
                    "vec3 applyLomoFilter1(vec3 color) {\n" +
                    "    // Classic LOMO - tăng contrast, giảm saturation, vignette\n" +
                    "    color = pow(color, vec3(1.2));\n" +
                    "    color = mix(vec3(dot(color, vec3(0.299, 0.587, 0.114))), color, 0.7);\n" +
                    "    float vignette = distance(vTextureCoord, vec2(0.5)) * 1.4;\n" +
                    "    color *= 1.0 - vignette * 0.5;\n" +
                    "    return color;\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyLomoFilter2(vec3 color) {\n" +
                    "    // Blue LOMO - tông màu xanh lạnh\n" +
                    "    color.r *= 0.8;\n" +
                    "    color.g *= 0.9;\n" +
                    "    color.b *= 1.2;\n" +
                    "    color = pow(color, vec3(1.1));\n" +
                    "    return color;\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyLomoFilter3(vec3 color) {\n" +
                    "    // Warm LOMO - tông màu ấm\n" +
                    "    color.r *= 1.3;\n" +
                    "    color.g *= 1.1;\n" +
                    "    color.b *= 0.8;\n" +
                    "    color = pow(color, vec3(0.9));\n" +
                    "    return color;\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyLomoFilter4(vec3 color) {\n" +
                    "    // Green LOMO - tông màu xanh lá\n" +
                    "    color.r *= 0.9;\n" +
                    "    color.g *= 1.2;\n" +
                    "    color.b *= 0.9;\n" +
                    "    color = pow(color, vec3(1.05));\n" +
                    "    return color;\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyLomoFilter5(vec3 color) {\n" +
                    "    // Purple LOMO - tông màu tím\n" +
                    "    color.r *= 1.1;\n" +
                    "    color.g *= 0.8;\n" +
                    "    color.b *= 1.3;\n" +
                    "    color = pow(color, vec3(1.15));\n" +
                    "    return color;\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyRetroFilter1(vec3 color) {\n" +
                    "    // Classic 70s - màu vàng ấm\n" +
                    "    color.r = color.r * 1.4 + 0.1;\n" +
                    "    color.g = color.g * 1.2 + 0.05;\n" +
                    "    color.b = color.b * 0.8;\n" +
                    "    return pow(color, vec3(0.8));\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyRetroFilter2(vec3 color) {\n" +
                    "    // Sepia Retro\n" +
                    "    float gray = dot(color, vec3(0.299, 0.587, 0.114));\n" +
                    "    return vec3(gray * 1.2, gray * 1.0, gray * 0.8);\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyRetroFilter3(vec3 color) {\n" +
                    "    // Faded Film - màu nhạt như phim cũ\n" +
                    "    color = pow(color, vec3(1.3));\n" +
                    "    color = color * 0.8 + 0.2;\n" +
                    "    return color;\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyRetroFilter4(vec3 color) {\n" +
                    "    // Vintage Pink - tông hồng cổ điển\n" +
                    "    color.r = color.r * 1.2 + 0.1;\n" +
                    "    color.g = color.g * 0.9;\n" +
                    "    color.b = color.b * 1.1 + 0.05;\n" +
                    "    return color;\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyRetroFilter5(vec3 color) {\n" +
                    "    // Orange Crush - tông cam retro\n" +
                    "    color.r = min(color.r * 1.5, 1.0);\n" +
                    "    color.g = color.g * 1.1;\n" +
                    "    color.b = color.b * 0.7;\n" +
                    "    return pow(color, vec3(0.9));\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyCubeFilter1(vec3 color) {\n" +
                    "    // Color Cube 1 - tăng cường màu đỏ-xanh\n" +
                    "    color.r = pow(color.r, 0.8);\n" +
                    "    color.g = pow(color.g, 1.2);\n" +
                    "    color.b = pow(color.b, 0.9);\n" +
                    "    return color;\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyCubeFilter2(vec3 color) {\n" +
                    "    // Color Cube 2 - màu sắc tương phản cao\n" +
                    "    color = pow(color, vec3(0.7));\n" +
                    "    color = color * 1.3;\n" +
                    "    return clamp(color, 0.0, 1.0);\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyCubeFilter3(vec3 color) {\n" +
                    "    // Color Cube 3 - shift màu cyan-magenta\n" +
                    "    float temp = color.r;\n" +
                    "    color.r = color.g * 0.8 + color.r * 0.2;\n" +
                    "    color.g = color.b * 0.8 + color.g * 0.2;\n" +
                    "    color.b = temp * 0.8 + color.b * 0.2;\n" +
                    "    return color;\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyCubeFilter4(vec3 color) {\n" +
                    "    // Color Cube 4 - màu sắc mát lạnh\n" +
                    "    color.r *= 0.7;\n" +
                    "    color.g *= 1.1;\n" +
                    "    color.b *= 1.4;\n" +
                    "    return pow(color, vec3(1.1));\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyCubeFilter5(vec3 color) {\n" +
                    "    // Color Cube 5 - màu sắc neon\n" +
                    "    color = pow(color, vec3(0.6));\n" +
                    "    if(color.r > 0.5) color.r = min(color.r * 1.5, 1.0);\n" +
                    "    if(color.g > 0.5) color.g = min(color.g * 1.5, 1.0);\n" +
                    "    if(color.b > 0.5) color.b = min(color.b * 1.5, 1.0);\n" +
                    "    return color;\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyBWFilter1(vec3 color) {\n" +
                    "    // Classic B&W\n" +
                    "    float gray = dot(color, vec3(0.299, 0.587, 0.114));\n" +
                    "    return vec3(gray);\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyBWFilter2(vec3 color) {\n" +
                    "    // High Contrast B&W\n" +
                    "    float gray = dot(color, vec3(0.299, 0.587, 0.114));\n" +
                    "    gray = pow(gray, 1.5);\n" +
                    "    return vec3(gray);\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyBWFilter3(vec3 color) {\n" +
                    "    // Soft B&W\n" +
                    "    float gray = dot(color, vec3(0.2126, 0.7152, 0.0722));\n" +
                    "    gray = pow(gray, 0.8);\n" +
                    "    return vec3(gray);\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyBWFilter4(vec3 color) {\n" +
                    "    // Red Channel B&W\n" +
                    "    float gray = color.r * 0.8 + color.g * 0.1 + color.b * 0.1;\n" +
                    "    return vec3(gray);\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyBWFilter5(vec3 color) {\n" +
                    "    // Blue Channel B&W\n" +
                    "    float gray = color.r * 0.1 + color.g * 0.2 + color.b * 0.7;\n" +
                    "    return vec3(gray);\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyVignetteFilter1(vec3 color) {\n" +
                    "    // Classic Vignette\n" +
                    "    float dist = distance(vTextureCoord, vec2(0.5));\n" +
                    "    float vignette = 1.0 - smoothstep(0.3, 0.8, dist);\n" +
                    "    return color * vignette;\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyVignetteFilter2(vec3 color) {\n" +
                    "    // Strong Vignette\n" +
                    "    float dist = distance(vTextureCoord, vec2(0.5));\n" +
                    "    float vignette = 1.0 - smoothstep(0.2, 0.9, dist);\n" +
                    "    return color * vignette;\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyVignetteFilter3(vec3 color) {\n" +
                    "    // Oval Vignette\n" +
                    "    vec2 pos = vTextureCoord - vec2(0.5);\n" +
                    "    pos.x *= 1.5; // tạo hình oval\n" +
                    "    float dist = length(pos);\n" +
                    "    float vignette = 1.0 - smoothstep(0.4, 0.8, dist);\n" +
                    "    return color * vignette;\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyVignetteFilter4(vec3 color) {\n" +
                    "    // Color Vignette - tông ấm\n" +
                    "    float dist = distance(vTextureCoord, vec2(0.5));\n" +
                    "    float vignette = smoothstep(0.5, 1.0, dist);\n" +
                    "    color.r += vignette * 0.2;\n" +
                    "    color.g += vignette * 0.1;\n" +
                    "    return color;\n" +
                    "}\n" +
                    "\n" +
                    "vec3 applyVignetteFilter5(vec3 color) {\n" +
                    "    // Tunnel Vignette\n" +
                    "    vec2 pos = vTextureCoord - vec2(0.5);\n" +
                    "    float dist = dot(pos, pos);\n" +
                    "    float vignette = 1.0 - smoothstep(0.1, 0.6, dist);\n" +
                    "    return color * vignette;\n" +
                    "}\n" +
                    "\n" +
                    "void main() {\n" +
                    "    vec4 color = texture2D(sTexture, vTextureCoord);\n" +
                    "    vec3 finalColor = color.rgb;\n" +
                    "    \n" +
                    "    // LOMO Filters (1-5)\n" +
                    "    if (filterType == 1) {\n" +
                    "        finalColor = applyLomoFilter1(finalColor);\n" +
                    "    } else if (filterType == 2) {\n" +
                    "        finalColor = applyLomoFilter2(finalColor);\n" +
                    "    } else if (filterType == 3) {\n" +
                    "        finalColor = applyLomoFilter3(finalColor);\n" +
                    "    } else if (filterType == 4) {\n" +
                    "        finalColor = applyLomoFilter4(finalColor);\n" +
                    "    } else if (filterType == 5) {\n" +
                    "        finalColor = applyLomoFilter5(finalColor);\n" +
                    "    }\n" +
                    "    // RETRO Filters (6-10)\n" +
                    "    else if (filterType == 6) {\n" +
                    "        finalColor = applyRetroFilter1(finalColor);\n" +
                    "    } else if (filterType == 7) {\n" +
                    "        finalColor = applyRetroFilter2(finalColor);\n" +
                    "    } else if (filterType == 8) {\n" +
                    "        finalColor = applyRetroFilter3(finalColor);\n" +
                    "    } else if (filterType == 9) {\n" +
                    "        finalColor = applyRetroFilter4(finalColor);\n" +
                    "    } else if (filterType == 10) {\n" +
                    "        finalColor = applyRetroFilter5(finalColor);\n" +
                    "    }\n" +
                    "    // CUBE Filters (11-15)\n" +
                    "    else if (filterType == 11) {\n" +
                    "        finalColor = applyCubeFilter1(finalColor);\n" +
                    "    } else if (filterType == 12) {\n" +
                    "        finalColor = applyCubeFilter2(finalColor);\n" +
                    "    } else if (filterType == 13) {\n" +
                    "        finalColor = applyCubeFilter3(finalColor);\n" +
                    "    } else if (filterType == 14) {\n" +
                    "        finalColor = applyCubeFilter4(finalColor);\n" +
                    "    } else if (filterType == 15) {\n" +
                    "        finalColor = applyCubeFilter5(finalColor);\n" +
                    "    }\n" +
                    "    // BLACK & WHITE Filters (16-20)\n" +
                    "    else if (filterType == 16) {\n" +
                    "        finalColor = applyBWFilter1(finalColor);\n" +
                    "    } else if (filterType == 17) {\n" +
                    "        finalColor = applyBWFilter2(finalColor);\n" +
                    "    } else if (filterType == 18) {\n" +
                    "        finalColor = applyBWFilter3(finalColor);\n" +
                    "    } else if (filterType == 19) {\n" +
                    "        finalColor = applyBWFilter4(finalColor);\n" +
                    "    } else if (filterType == 20) {\n" +
                    "        finalColor = applyBWFilter5(finalColor);\n" +
                    "    }\n" +
                    "    // VIGNETTE Filters (21-25)\n" +
                    "    else if (filterType == 21) {\n" +
                    "        finalColor = applyVignetteFilter1(finalColor);\n" +
                    "    } else if (filterType == 22) {\n" +
                    "        finalColor = applyVignetteFilter2(finalColor);\n" +
                    "    } else if (filterType == 23) {\n" +
                    "        finalColor = applyVignetteFilter3(finalColor);\n" +
                    "    } else if (filterType == 24) {\n" +
                    "        finalColor = applyVignetteFilter4(finalColor);\n" +
                    "    } else if (filterType == 25) {\n" +
                    "        finalColor = applyVignetteFilter5(finalColor);\n" +
                    "    }\n" +
                    "    \n" +
                    "    gl_FragColor = vec4(finalColor, color.a);\n" +
                    "}";

    public CameraPreview() {
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
        filterTypeHandle = GLES20.glGetUniformLocation(program, "filterType");
    }

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

        GLES20.glUseProgram(program);

        // Enable vertex attributes
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer);

        GLES20.glEnableVertexAttribArray(textureHandle);
        GLES20.glVertexAttribPointer(textureHandle, 2, GLES20.GL_FLOAT, false, 8, textureBuffer);


        // Set uniforms
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        // Bind texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(samplerHandle, 0);

        // Draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glUniform1i(filterTypeHandle, currentFilterType);

        // Disable vertex attributes
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(textureHandle);
    }

    public void setFilterType(FilterType filterType) {
        this.currentFilterType = filterType.getFilterType();
    }

    public void updateCameraDirection(boolean isFrontCamera) {
        if(isFrontCamera){
            textureCoords = new float[] {
                    0.0f, 1.0f,  // Bottom-right -> Top-left
                    0.0f, 0.0f,  // Top-right -> Bottom-left
                    1.0f, 1.0f,  // Bottom-left -> Top-right
                    1.0f, 0.0f   // Top-left -> Bottom-right
            };
        }else{
            textureCoords = new float[] {
                    1.0f, 1.0f,  // Bottom-right -> Top-left
                    1.0f, 0.0f,  // Top-right -> Bottom-left
                    0.0f, 1.0f,  // Bottom-left -> Top-right
                    0.0f, 0.0f   // Top-left -> Bottom-right
            };
        }

        textureBuffer.clear();
        textureBuffer.put(textureCoords);
        textureBuffer.position(0);
    }
}
