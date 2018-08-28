package com.hy.picker.core;

import java.util.List;

/**
 * Created time : 2018/8/27 11:16.
 *
 * @author HY
 */
public class CrystalResult {

    private List<Crystal> data;

    public List<Crystal> getData() {
        return data;
    }

    public void setData(List<Crystal> data) {
        this.data = data;
    }

    public static class Crystal {

        private String res;
        private int length;
        public String getRes() {
            return res;
        }

        public void setRes(String res) {
            this.res = res;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

    }
}
