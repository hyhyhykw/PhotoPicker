package com.hy.picker.core.anim

import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator

import com.hy.picker.core.homing.IMGHoming
import com.hy.picker.core.homing.IMGHomingEvaluator

/**
 * Created by felix on 2017/11/28 下午12:54.
 */

class IMGHomingAnimator : ValueAnimator() {

    var isRotate = false
        private set

    private lateinit var evaluator: IMGHomingEvaluator

    init {
        interpolator = AccelerateDecelerateInterpolator()
    }

    override fun setObjectValues(vararg values: Any?) {
        super.setObjectValues(*values)
        if (!::evaluator.isInitialized) {
            evaluator = IMGHomingEvaluator()
        }
        setEvaluator(evaluator)
    }

    fun setHomingValues(sHoming: IMGHoming, eHoming: IMGHoming) {
        setObjectValues(sHoming, eHoming)
        isRotate = IMGHoming.isRotate(sHoming, eHoming)
    }
}
