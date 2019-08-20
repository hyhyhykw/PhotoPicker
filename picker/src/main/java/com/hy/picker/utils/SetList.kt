package com.hy.picker.utils

import java.util.*

/**
 * Created time : 2018/8/3 9:04.
 * 自定义集合，无重复元素
 *
 * @author HY
 */
class SetList<E> : ArrayList<E>() {

    override fun add(element: E): Boolean {
        if (null == element) return false
        return if (size == 0) {
            super.add(element)
        } else {
            val index = indexOf(element)

            if (index == -1) {
                super.add(element)
            } else {
                set(index, element)
                false
            }
        }
    }

    override fun add(index: Int, element: E) {
        if (null == element) return
        if (size == 0) {
            super.add(element)
        } else {
            val position = indexOf(element)
            if (position == -1) {
                super.add(index, element)
            } else {
                set(position, element)
            }
        }
    }

    companion object {

        private const val serialVersionUID = 1434324234L
    }
}
