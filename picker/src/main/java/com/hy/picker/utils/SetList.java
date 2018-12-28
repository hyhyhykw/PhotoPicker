package com.hy.picker.utils;

import java.util.ArrayList;

/**
 * Created time : 2018/8/3 9:04.
 * 自定义集合，无重复元素
 *
 * @author HY
 */
public class SetList<E> extends ArrayList<E> {

    private static final long serialVersionUID = 1434324234L;

    @Override
    public boolean add(E object) {
        if (null == object) return false;
        if (size() == 0) {
            return super.add(object);
        } else {
            int index = -1;
            for (int i = 0; i < this.size(); i++) {
                if (get(i).equals(object)) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                return super.add(object);
            } else {
                set(index, object);
                return false;
            }

        }
    }

    @Override
    public void add(int index, E element) {
        int indexOf = indexOf(element);
        if (indexOf != -1) {
            set(indexOf, element);
            return;
        }
        super.add(index, element);
    }
}
