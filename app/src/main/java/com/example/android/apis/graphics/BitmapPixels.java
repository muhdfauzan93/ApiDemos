/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Supposed to show Bitmap.Config.ARGB_8888, Bitmap.Config.RGB_565, and Bitmap.Config.ARGB_4444
 * configurations of the same color ramp, but ever since KitKat Bitmap.createBitmap will return
 * a Bitmap.Config.ARGB_8888 bitmap instead of Bitmap.Config.ARGB_4444 bitmap, so the call to
 * mBitmap3.copyPixelsFromBuffer(makeBuffer(data4444, N)); on line 147 will crash Lollipop.
 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) kludge to make it work on newer versions.
 */
public class BitmapPixels extends GraphicsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    private static class SampleView extends View {
        private Bitmap mBitmap1;
        private Bitmap mBitmap2;
        private Bitmap mBitmap3;

        /**
         * access the red component from a pre-multiplied color
         *
         * @param c 32 bit color
         * @return 8 bit red component of {@code c}
         */
        private static int getR32(int c) {
            return (c >> 0) & 0xFF;
        }

        /**
         * access the green component from a pre-multiplied color
         *
         * @param c 32 bit color
         * @return 8 bit green component of {@code c}
         */
        private static int getG32(int c) {
            return (c >> 8) & 0xFF;
        }

        /**
         * access the blue component from a pre-multiplied color
         *
         * @param c 32 bit color
         * @return 8 bit blue component of {@code c}
         */
        private static int getB32(int c) {
            return (c >> 16) & 0xFF;
        }

        /**
         * access the alpha component from a pre-multiplied color
         *
         * @param c 32 bit color
         * @return 8 bit alpha component of {@code c}
         */
        private static int getA32(int c) {
            return (c >> 24) & 0xFF;
        }

        /**
         * This takes components and packs them into an int in the correct device order.
         *
         * @param r red component
         * @param g green component
         * @param b blue component
         * @param a alpha component
         * @return 32 bit ARGB_8888 color
         */
        private static int pack8888(int r, int g, int b, int a) {
            return (r << 0) | (g << 8) | (b << 16) | (a << 24);
        }

        /**
         * This packs the three color components into a 16 bit RGB_565 color.
         *
         * @param r red component
         * @param g green component
         * @param b blue component
         * @return 16 bit RGB_565 packed color
         */
        private static short pack565(int r, int g, int b) {
            return (short) ((r << 11) | (g << 5) | (b << 0));
        }

        /**
         * This packs the three color components and alpha component into a 16 bit RGB_4444 color.
         *
         * @param r red component
         * @param g green component
         * @param b blue component
         * @return 16 bit RGB_4444 packed color
         */
        private static short pack4444(int r, int g, int b, int a) {
            return (short) ((a << 0) | (b << 4) | (g << 8) | (r << 12));
        }

        /**
         * Scales the color {@code c} by the alpha {@code a}. Rather unnecessary in our case since
         * {@code a} is always 255, and {@code c} is either 0 of 255, but what the hay.
         *
         * @param c color value (0-255)
         * @param a alpha value (0-255)
         * @return color value scaled by alpha (pre-multiplied if your would)
         */
        private static int mul255(int c, int a) {
            int prod = c * a + 128;
            return (prod + (prod >> 8)) >> 8;
        }

        /**
         * Turn a color int into a pre-multiplied device color
         */
        private static int premultiplyColor(int c) {
            int r = Color.red(c);
            int g = Color.green(c);
            int b = Color.blue(c);
            int a = Color.alpha(c);
            // now apply the alpha to r, g, b
            r = mul255(r, a);
            g = mul255(g, a);
            b = mul255(b, a);
            // now pack it in the correct order
            return pack8888(r, g, b, a);
        }

        private static void makeRamp(int from, int to, int n,
                                     int[] ramp8888, short[] ramp565,
                                     short[] ramp4444) {
            int r = getR32(from) << 23;
            int g = getG32(from) << 23;
            int b = getB32(from) << 23;
            int a = getA32(from) << 23;
            // now compute our step amounts per component (biased by 23 bits)
            int dr = ((getR32(to) << 23) - r) / (n - 1);
            int dg = ((getG32(to) << 23) - g) / (n - 1);
            int db = ((getB32(to) << 23) - b) / (n - 1);
            int da = ((getA32(to) << 23) - a) / (n - 1);

            for (int i = 0; i < n; i++) {
                ramp8888[i] = pack8888(r >> 23, g >> 23, b >> 23, a >> 23);
                ramp565[i] = pack565(r >> (23 + 3), g >> (23 + 2), b >> (23 + 3));
                ramp4444[i] = pack4444(r >> (23 + 4), g >> (23 + 4), b >> (23 + 4), a >> (23 + 4));
                r += dr;
                g += dg;
                b += db;
                a += da;
            }
        }

        private static IntBuffer makeBuffer(int[] src, int n) {
            IntBuffer dst = IntBuffer.allocate(n * n);
            for (int i = 0; i < n; i++) {
                dst.put(src);
            }
            dst.rewind();
            return dst;
        }

        private static ShortBuffer makeBuffer(short[] src, int n) {
            ShortBuffer dst = ShortBuffer.allocate(n * n);
            for (int i = 0; i < n; i++) {
                dst.put(src);
            }
            dst.rewind();
            return dst;
        }

        public SampleView(Context context) {
            super(context);
            setFocusable(true);

            final int N = 100;
            int[] data8888 = new int[N];
            short[] data565 = new short[N];
            short[] data4444 = new short[N];

            makeRamp(premultiplyColor(Color.RED), premultiplyColor(Color.GREEN),
                    N, data8888, data565, data4444);

            mBitmap1 = Bitmap.createBitmap(N, N, Bitmap.Config.ARGB_8888);
            mBitmap2 = Bitmap.createBitmap(N, N, Bitmap.Config.RGB_565);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mBitmap3 = Bitmap.createBitmap(N, N, Bitmap.Config.RGB_565);
            } else {
                mBitmap3 = Bitmap.createBitmap(N, N, Bitmap.Config.ARGB_4444);
            }

            mBitmap1.copyPixelsFromBuffer(makeBuffer(data8888, N));
            mBitmap2.copyPixelsFromBuffer(makeBuffer(data565, N));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mBitmap3.copyPixelsFromBuffer(makeBuffer(data565, N));
            } else {
                mBitmap3.copyPixelsFromBuffer(makeBuffer(data4444, N));
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFCCCCCC);

            int y = 10;
            canvas.drawBitmap(mBitmap1, 10, y, null);
            y += mBitmap1.getHeight() + 10;
            canvas.drawBitmap(mBitmap2, 10, y, null);
            y += mBitmap2.getHeight() + 10;
            canvas.drawBitmap(mBitmap3, 10, y, null);
        }
    }
}
