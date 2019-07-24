/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Piasy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.hy.picker.utils

import android.graphics.PointF
import com.hy.picker.view.PickerScaleImageView
import kotlin.math.abs

/**
 * credit: https://github.com/Piasy/BigImageViewer/issues/2
 */

class DisplayOptimizeListener(private val imageView: PickerScaleImageView) : PickerScaleImageView.OnImageEventListener {
    override fun onPreviewLoadError(e: Exception?) = Unit
    override fun onImageLoadError(e: Exception?) = Unit
    override fun onTileLoadError(e: Exception?) = Unit
    override fun onReady() {
        var result = 0.5f
        val imageWidth = imageView.sWidth
        val imageHeight = imageView.sHeight
        val viewWidth = imageView.width
        val viewHeight = imageView.height

        var hasZeroValue = false
        if (imageWidth == 0 || imageHeight == 0 || viewWidth == 0 || viewHeight == 0) {
            hasZeroValue = true
        }

        if (!hasZeroValue) {
            result = if (imageWidth <= imageHeight) {
                viewWidth.toFloat() / imageWidth
            } else {
                viewHeight.toFloat() / imageHeight
            }
        }

        if (!hasZeroValue && imageHeight.toFloat() / imageWidth > LONG_IMAGE_SIZE_RATIO) {
            // scale at top
            val builder = imageView
                    .animateScaleAndCenter(result, PointF(imageWidth / 2f, 0f))!!
            builder.withEasing(PickerScaleImageView.EASE_OUT_QUAD)
                    .start()
        }

        // `对结果进行放大裁定，防止计算结果跟双击放大结果过于相近`
        if (abs(result - 0.1) < 0.2f) {
            result += 0.2f
        }

        imageView.setDoubleTapZoomScale(result)
    }

    override fun onImageLoaded() = Unit


    override fun onPreviewReleased() = Unit


    companion object {
        private const val LONG_IMAGE_SIZE_RATIO = 2
    }

}
