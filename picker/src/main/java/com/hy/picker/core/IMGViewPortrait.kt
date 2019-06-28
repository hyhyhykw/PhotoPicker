package com.hy.picker.core

/**
 * Created by felix on 2017/11/16 下午5:49.
 */

interface IMGViewPortrait {

    val width: Int

    val height: Int

    val scaleX: Float
    val scaleY: Float


    var rotation: Float

    val pivotX: Float

    val pivotY: Float

    var x: Float
    var y: Float

    var scale: Float

    fun addScale(scale: Float)
}
