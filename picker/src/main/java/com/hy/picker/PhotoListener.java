package com.hy.picker;

import com.picker2.model.Photo;

import java.util.ArrayList;

/**
 * Created time : 2018/8/20 8:22.
 *
 * @author HY
 */
public interface PhotoListener {

    void onPicked(ArrayList<Photo> picItems);

}
