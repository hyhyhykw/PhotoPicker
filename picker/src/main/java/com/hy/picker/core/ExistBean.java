package com.hy.picker.core;

import java.io.File;

/**
 * Created time : 2018/8/27 11:41.
 *
 * @author HY
 */
public class ExistBean {
    private File file;
    private boolean exist;

    public ExistBean() {
    }

    public ExistBean(File file, boolean exist) {
        this.file = file;
        this.exist = exist;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }
}
