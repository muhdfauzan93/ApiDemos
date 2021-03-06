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
package com.example.android.apis.graphics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.View

/**
 * Shows how to load and use a custom Typeface.
 */
class Typefaces : GraphicsActivity() {
    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * `onCreate`, then we set our content view to a new instance of [SampleView].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(SampleView(this))
    }

    /**
     * Simple custom view which just draws two lines of text, one using the default font and one
     * using the custom font "fonts/samplefont.ttf"
     */
    private class SampleView(context: Context) : View(context) {
        /**
         * [Paint] we use to draw our text
         */
        private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        /**
         * Our custom font, loaded from "fonts/samplefont.ttf"
         */
        private val mFace: Typeface = Typeface.createFromAsset(
                context.assets,
                "fonts/samplefont.ttf"
        )

        /**
         * We implement this to do our drawing. First we set the entire [Canvas] parameter [canvas]
         * to [Color.WHITE]. We set the type face of [Paint] field [mPaint] to null (so it will use
         * the default), and use it to draw the text "Draw with Default:" at (10,100) on [canvas],
         * then draw the text "SAMPLE TEXT" at (10,200), "Draw with Custom Font" at (10,400), and
         * "(Custom Font draws 'A' with solid triangle.)" at (10,500). Next we set the typeface of
         * [mPaint] to our custom [Typeface] field [mFace], and use it to draw the text "SAMPLE TEXT"
         * at (10,600) on [canvas].
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(Color.WHITE)
            mPaint.typeface = null
            canvas.drawText("Draw with Default:", 10f, 100f, mPaint)
            canvas.drawText("  SAMPLE TEXT", 10f, 200f, mPaint)
            canvas.drawText("Draw with Custom Font", 10f, 400f, mPaint)
            canvas.drawText("(Custom Font draws 'A' with solid triangle.)", 10f, 500f, mPaint)
            mPaint.typeface = mFace
            canvas.drawText("  SAMPLE TEXT", 10f, 600f, mPaint)
        }

        /**
         * The init block of our constructor. We set the text size of `Paint` field `mPaint` to 64.
         */
        init {
            mPaint.textSize = 64f
        }
    }
}