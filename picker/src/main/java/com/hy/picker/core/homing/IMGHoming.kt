package com.hy.picker.core.homing

/**
 * Created by felix on 2017/11/28 下午4:14.
 */

class IMGHoming(var x: Float,
                var y: Float,
                var scale: Float,
                var rotate: Float) {

    operator fun set(x: Float, y: Float, scale: Float, rotate: Float) {
        this.x = x
        this.y = y
        this.scale = scale
        this.rotate = rotate
    }

    fun concat(homing: IMGHoming) {
        this.scale *= homing.scale
        this.x += homing.x
        this.y += homing.y
    }

    fun rConcat(homing: IMGHoming) {
        this.scale *= homing.scale
        this.x -= homing.x
        this.y -= homing.y
    }

    override fun toString(): String {
        return "IMGHoming{" +
                "x=" + x +
                ", y=" + y +
                ", scale=" + scale +
                ", rotate=" + rotate +
                '}'.toString()
    }

    companion object {

        fun isRotate(sHoming: IMGHoming, eHoming: IMGHoming): Boolean {
            return java.lang.Float.compare(sHoming.rotate, eHoming.rotate) != 0
        }
    }
}
