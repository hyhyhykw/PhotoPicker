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
        if (size == 0) {
            return super.add(element)
        } else {
            var index = -1
            for (i in 0 until this.size) {
                if (get(i) == element) {
                    index = i
                    break
                }
            }
            return if (index == -1) {
                super.add(element)
            } else {
                set(index, element)
                false
            }

        }
    }

    override fun add(index: Int, element: E) {
        val indexOf = indexOf(element)
        if (indexOf != -1) {
            set(indexOf, element)
            return
        }
        super.add(index, element)
    }

    companion object {

        private const val serialVersionUID = 1434324234L
    }
}
