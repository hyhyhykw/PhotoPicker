package com.hy.picker

import android.transition.Transition

/**
 * Created time : 2019-05-18 13:27.
 *
 * @author HY
 */
open class MyTransitionListener : Transition.TransitionListener {
    override fun onTransitionStart(transition: Transition)=Unit

    override fun onTransitionEnd(transition: Transition)=Unit

    override fun onTransitionCancel(transition: Transition)=Unit

    override fun onTransitionPause(transition: Transition)=Unit

    override fun onTransitionResume(transition: Transition) =Unit
}
