/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.graphics;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.os.SystemClock;

import com.example.android.apis.R;

/**
 * Draws a {@code Triangle} using OpenGL ES 1.x-compatible renderer.
 */
@SuppressWarnings("WeakerAccess")
public class TriangleRenderer implements GLSurfaceView.Renderer {

    public TriangleRenderer(Context context) {
        mContext = context;
        mTriangle = new Triangle();
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        /*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        gl.glDisable(GL10.GL_DITHER);

        /*
         * Some one-time OpenGL initialization can be made here
         * probably based on features of this particular context
         */
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

        gl.glClearColor(.5f, .5f, .5f, 1);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glEnable(GL10.GL_TEXTURE_2D);

        /*
         * Create our texture. This has to be done each time the
         * surface is created.
         */

        int[] textures = new int[1];
        gl.glGenTextures(1, textures, 0);

        mTextureID = textures[0];
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);

        InputStream is = mContext.getResources().openRawResource(R.raw.robot);
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // Ignore.
            }
        }

        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
    }

    public void onDrawFrame(GL10 gl) {
        /*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        gl.glDisable(GL10.GL_DITHER);

        gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,
                GL10.GL_MODULATE);

        /*
         * Usually, the first thing one might want to do is to clear
         * the screen. The most efficient way of doing this is to use
         * glClear().
         */

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        /*
         * Now we're ready to draw some 3D objects
         */

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        GLU.gluLookAt(gl, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glActiveTexture(GL10.GL_TEXTURE0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
                GL10.GL_REPEAT);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
                GL10.GL_REPEAT);

        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);

        gl.glRotatef(angle, 0, 0, 1.0f);

        mTriangle.draw(gl);
    }

    public void onSurfaceChanged(GL10 gl, int w, int h) {
        gl.glViewport(0, 0, w, h);

        /*
        * Set our projection matrix. This doesn't have to be done
        * each time we draw, but usually a new projection needs to
        * be set when the viewport is resized.
        */

        float ratio = (float) w / h;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7);

    }

    private Context mContext;
    private Triangle mTriangle;
    private int mTextureID;
}

/**
 * Draws a triangle, used both by {@code TriangleRenderer} an {@code FrameBufferObjectActivity}.
 */
@SuppressWarnings("WeakerAccess")
class Triangle {
    /**
     * Number of vertices in our triangle
     */
    private final static int VERTS = 3;

    /**
     * Vertex Buffer loaded with (x,y,z) coordinates of our triangle
     */
    private FloatBuffer mFVertexBuffer;
    /**
     * Texture buffer loaded with (x,y,z) coordinates of our triangle offset by 0.5 so that
     * the texture is centered.
     */
    private FloatBuffer mTexBuffer;
    /**
     * Indices of the triangle (0,1,2) in counter clockwise order so that the normal points up
     */
    private ShortBuffer mIndexBuffer;

    /**
     * Constructs our {@code Triangle} instance by allocating and initializing our fields. First we
     * allocate {@code ByteBuffer vbb} on the native heap, set its byte order to native byte order
     * and initialize {@code FloatBuffer mFVertexBuffer} with a view of {@code vbb} as a float buffer.
     * We allocate {@code ByteBuffer tbb} on the native heap, set its byte order to native byte order
     * and initialize {@code FloatBuffer mTexBuffer} with a view of {@code tbb} as a float buffer.
     * We allocate {@code ByteBuffer ibb} on the native heap, set its byte order to native byte order
     * and initialize {@code ShortBuffer mIndexBuffer} with a view of {@code tbb} as a short buffer.
     * <p>
     * We define the contents of {@code float[] coords} to be the coordinates of a unit-sided
     * equilateral triangle centered on the origin. Then we load the 9 entries in {@code coords[]}
     * multiplied by 2.0 into {@code FloatBuffer mFVertexBuffer}, and the (x,y) values only
     * multiplied by 2.0 and offset by 0.5 into {@code FloatBuffer mTexBuffer}. We load
     * {@code ShortBuffer mIndexBuffer} with the three indices 0, 1, and 2.
     * <p>
     * Finally we position {@code mFVertexBuffer}, {@code mTexBuffer}, and {@code mIndexBuffer} to
     * their beginning entry ready for use.
     */
    public Triangle() {

        // Buffers to be passed to gl*Pointer() functions
        // must be direct, i.e., they must be placed on the
        // native heap where the garbage collector cannot
        // move them.
        //
        // Buffers with multi-byte data types (e.g., short, int, float)
        // must have their byte order set to native order

        ByteBuffer vbb = ByteBuffer.allocateDirect(VERTS * 3 * 4);
        vbb.order(ByteOrder.nativeOrder());
        mFVertexBuffer = vbb.asFloatBuffer();

        ByteBuffer tbb = ByteBuffer.allocateDirect(VERTS * 2 * 4);
        tbb.order(ByteOrder.nativeOrder());
        mTexBuffer = tbb.asFloatBuffer();

        ByteBuffer ibb = ByteBuffer.allocateDirect(VERTS * 2);
        ibb.order(ByteOrder.nativeOrder());
        mIndexBuffer = ibb.asShortBuffer();

        // A unit-sided equilateral triangle centered on the origin.
        float[] coords = {
                // X, Y, Z
                -0.5f, -0.25f, 0,
                0.5f, -0.25f, 0,
                0.0f, 0.559016994f, 0
        };

        for (int i = 0; i < VERTS; i++) {
            for (int j = 0; j < 3; j++) {
                mFVertexBuffer.put(coords[i * 3 + j] * 2.0f);
            }
        }

        for (int i = 0; i < VERTS; i++) {
            for (int j = 0; j < 2; j++) {
                mTexBuffer.put(coords[i * 3 + j] * 2.0f + 0.5f);
            }
        }

        for (int i = 0; i < VERTS; i++) {
            mIndexBuffer.put((short) i);
        }

        mFVertexBuffer.position(0);
        mTexBuffer.position(0);
        mIndexBuffer.position(0);
    }

    /**
     * Draws our triangle. First we specify the orientation of front-facing polygons to be GL_CCW
     * (selects counterclockwise polygons as front-facing). We next define {@code mFVertexBuffer} to
     * be our array of vertex data, with 3 coordinates per vertex, using GL_FLOAT as its data type,
     * and a stride of 0. We enable the server-side GL capability GL_TEXTURE_2D (two-dimensional
     * texturing is performed), then define {@code mTexBuffer} to be our array of texture coordinates,
     * with 2 coordinates per point, using GL_FLOAT as its data type, and a stride of 0.
     * <p>
     * Finally we call {@code glDrawElements} to render primitives from array data specifying
     * {@code ShortBuffer mIndexBuffer} as our indices array, GL_UNSIGNED_SHORT as its data type,
     * and GL_TRIANGLE_STRIP as the type of primitive to render (Every group of 3 adjacent vertices
     * forms a triangle - we have only one triangle).
     *
     * @param gl the GL interface.
     */
    public void draw(GL10 gl) {
        gl.glFrontFace(GL10.GL_CCW);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mFVertexBuffer);
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, VERTS, GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
    }
}
