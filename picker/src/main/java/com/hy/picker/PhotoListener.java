package com.hy.picker;

import java.util.ArrayList;

/**
 * Created time : 2018/8/20 8:22.
 *
 * @author HY
 */
public interface PhotoListener {

    void onPicked(ArrayList<PictureSelectorActivity.PicItem> picItems);

}
