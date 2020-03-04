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
import android.graphics.*
import android.os.Bundle
import android.view.View

/**
 * Shows the results of using:
 *
 *  * [Matrix.ScaleToFit.FILL]
 *  * [Matrix.ScaleToFit.START]
 *  * [Matrix.ScaleToFit.CENTER]
 *  * [Matrix.ScaleToFit.END]
 *
 * as the ScaleToFit options when calling:
 * [Matrix.setRectToRect] `(Rect srcRect, Rect dstRect, Matrix.ScaleToFit stf)`
 */
class ScaleToFit : GraphicsActivity() {
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
     * Custom view which generates and displays 4 ovals created with different bounding rectangles,
     * then proceeds to demonstrate the results of displaying each using [Matrix.setRectToRect] into
     * a square using the four different types of [Matrix.ScaleToFit] options.
     */
    private class SampleView(context: Context?) : View(context) {
        /**
         * [Paint] used to draw all our ovals.
         */
        private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        /**
         * [Paint] used to draw the square we are fitting our ovals into.
         */
        private val mHairPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        /**
         * [Paint] we use to draw the label on each of the four rows of scaled examples.
         */
        private val mLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        /**
         * [Matrix] we use to scale and translate values to map the ovals to the destination
         * square (generated by [Matrix.setRectToRect]).
         */
        private val mMatrix = Matrix()
        /**
         * [RectF] we use as the bounding rectangle for the ovals drawn by [Canvas.drawOval],
         * and as the source rectangle to map from when calling [Matrix.setRectToRect]
         */
        private val mSrcR = RectF()
        /**
         * Scale to convert dp to pixels.
         */
        private val mScale: Float = resources.displayMetrics.density
        /**
         * Destination rectangle that we draw into.
         */
        private val mDstR: RectF

        /**
         * Sets the width and height of [RectF] field [mSrcR] to that of the oval whose data is
         * fetched from the const [IntArray] field [sSrcData] given the value of our [Int] parameter
         * [index]. The width `val w` is to be found at location `index*3`, and the height `val h`
         * is to be found at `index*3+1`. Having fetched them we simply call the [RectF.set] method
         * of [mSrcR] to set its coordinates to these values.
         *
         * @param index which of the 4 ovals to use for the configuation of [RectF] field [mSrcR].
         */
        private fun setSrcR(index: Int) {
            val w = sSrcData[index * 3 + 0]
            val h = sSrcData[index * 3 + 1]
            mSrcR[0f, 0f, w.toFloat()] = h.toFloat()
        }

        /**
         * After fetching the color of the oval specified by [Int] parameter [index] from [IntArray]
         * field [sSrcData] (the colors are found at location `index*3+2` in the array) and using it
         * to set the color of [Paint] field [mPaint], we draw the oval whose width and height has
         * already been set in [RectF] field [mSrcR] on the [Canvas] parameter [canvas] using [mPaint]
         * as the [Paint].
         *
         * @param canvas [Canvas] to draw on
         * @param index  oval number, we use it to fetch the color for this oval from [IntArray]
         *               field [sSrcData]
         */
        private fun drawSrcR(canvas: Canvas, index: Int) {
            mPaint.color = sSrcData[index * 3 + 2]
            canvas.drawOval(mSrcR, mPaint)
        }

        /**
         * Pre-concatenates the matrix of [Canvas] parameter [canvas] with a matrix intended to
         * scale and translate values that map the source rectangle [RectF] field [mSrcR] to the
         * destination rectangle [RectF] field [mDstR] given the [Matrix.ScaleToFit] option in
         * [stf], and draws the oval chosen by the parameter [index] to the canvas.
         *
         * First we save the current matrix and clip of [Canvas] parameter [canvas] onto a private
         * stack, then we call our method [setSrcR] to configure [RectF] field [mSrcR] for the data
         * for oval [index]. We use the method [Matrix.setRectToRect] to create in [Matrix] field
         * [mMatrix] a matrix intended to scale and translate values that map the source rectangle
         * in [RectF] field [mSrcR] to the destination rectangle in [RectF] field [mDstR] given the
         * [Matrix.ScaleToFit] specified by our parameter [stf], and pre-concatenate it to the
         * current matrix of [canvas]. We then call our method [drawSrcR] to draw the [index] oval
         * to [canvas] using the correct color, and restore the matrix and clip of [canvas] to the
         * state it was in before our call to [Canvas.save]. Finally we draw an outline of the
         * destination rectangle [mDstR] using the [Paint] field [mHairPaint].
         *
         * @param canvas [Canvas] to draw on
         * @param index  Number of the oval to draw
         * @param stf    [Matrix.ScaleToFit] option to use in call to [Matrix.setRectToRect]
         */
        private fun drawFit(canvas: Canvas, index: Int, stf: Matrix.ScaleToFit) {
            canvas.save()
            setSrcR(index)
            mMatrix.setRectToRect(mSrcR, mDstR, stf)
            canvas.concat(mMatrix)
            drawSrcR(canvas, index)
            canvas.restore()
            canvas.drawRect(mDstR, mHairPaint)
        }

        /**
         * We implement this to do our drawing. First we set the entire [Canvas] parameter [canvas]
         * to the color WHITE, then we move it to the location (10,10), and save the current matrix
         * and clip onto a private stack. Then for each of our [N] ovals, we call our [setSrcR]
         * method to set the coordinates of [RectF] field [mSrcR] for that oval, and using our
         * [drawSrcR] method draw the oval to the canvas. We then move the canvas to the right by
         * the width of [mSrcR] with an additional 15 pixels for spacing. When done drawing the
         * ovals we restore the state of the canvas to that it had before our call to save.
         *
         * Now we move the [canvas] down 100 pixels and for each of the four scale to fit options
         * contained in the [Matrix.ScaleToFit] array [sFits] we first save the canvas matrix to
         * its private stack. Then for each of the four ovals we call our method [drawFit] to
         * scale and translate the oval then draw it into [RectF] field [mDstR], and move the canvas
         * to the right by the width of [mDstR] with an 8 pixel spacing to get ready for the next
         * oval. When done with the ovals for this row we draw the text describing the scale to fit
         * option that was used for that row, restore the canvas matrix to the state before our call
         * to save, and move the canvas down by [HEIGHT] plus 20 pixels for the next row.
         *
         * @param canvas the [Canvas] on which the background will be drawn
         */
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(Color.WHITE)
            canvas.translate(10f, 10f)
            canvas.save()
            for (i in 0 until N) {
                setSrcR(i)
                drawSrcR(canvas, i)
                canvas.translate(mSrcR.width() + 15, 0f)
            }
            canvas.restore()
            canvas.translate(0f, 100f)
            for (j in sFits.indices) {
                canvas.save()
                for (i in 0 until N) {
                    drawFit(canvas, i, sFits[j])
                    canvas.translate(mDstR.width() + 8, 0f)
                }
                canvas.drawText(sFitLabels[j], 0f, HEIGHT * 2f / 3, mLabelPaint)
                canvas.restore()
                canvas.translate(0f, HEIGHT + 20.toFloat())
            }
        }

        companion object {
            /**
             * The four different [Matrix.ScaleToFit] options passed to `setRectToRect`.
             */
            private val sFits = arrayOf(
                    /**
                     * Scale in X and Y independently, so that src matches dst exactly.
                     * This may change the aspect ratio of the src.
                     */
                    Matrix.ScaleToFit.FILL,
                    /**
                     * Compute a scale that will maintain the original src aspect ratio,
                     * but will also ensure that src fits entirely inside dst. At least
                     * one axis (X or Y) will fit exactly. START aligns the result to the
                     * left and top edges of dst.
                     */
                    Matrix.ScaleToFit.START,
                    /**
                     * Compute a scale that will maintain the original src aspect ratio,
                     * but will also ensure that src fits entirely inside dst. At least
                     * one axis (X or Y) will fit exactly. The result is centered inside dst.
                     */
                    Matrix.ScaleToFit.CENTER,
                    /**
                     * Compute a scale that will maintain the original src aspect ratio, but
                     * will also ensure that src fits entirely inside dst. At least one axis
                     * (X or Y) will fit exactly. END aligns the result to the right and bottom
                     * edges of dst.
                     */
                    Matrix.ScaleToFit.END
            )
            /**
             * The labels corresponding to the [Matrix.ScaleToFit] option used for a row.
             */
            private val sFitLabels = arrayOf("FILL", "START", "CENTER", "END")
            /**
             * Contains the width, height and color used to draw our 4 ovals.
             */
            private val sSrcData = intArrayOf(
                    80, 40, Color.RED,
                    40, 80, Color.GREEN,
                    30, 30, Color.BLUE,
                    80, 80, Color.BLACK
            )
            /**
             * Number of ovals we draw.
             */
            private const val N = 4
            /**
             * Width of the destination rectangle for our drawing, modified by our constructor to
             * scale for the current display density
             */
            private var WIDTH = 52
            /**
             * Height of the destination rectangle for our drawing, modified by our constructor to
             * scale for the current display density
             */
            private var HEIGHT = 52
        }

        /**
         * The init block of ur constructor. First we use `mScale` to scale our fields `WIDTH` and
         * `HEIGHT` for the display density. We allocate a new instance of `RectF` with width `WIDTH`
         * and height `HEIGHT` for our field `mDstR`, set the style of our `Paint` field `mHairPaint`
         * to STROKE, and the text size of `Paint` field `mLabelPaint` to `16 * mScale`.
         */
        init {
            WIDTH *= mScale.toInt()
            HEIGHT *= mScale.toInt()
            mDstR = RectF(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat())
            mHairPaint.style = Paint.Style.STROKE
            mLabelPaint.textSize = 16 * mScale
        }
    }
}