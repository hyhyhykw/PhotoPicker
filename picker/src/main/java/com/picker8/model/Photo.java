package com.picker8.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.hy.picker.utils.ObjectsUtils;

/**
 * Created by donglua on 15/6/30.
 */
public class Photo implements Parcelable {

    private String uri;
    private boolean selected;
    private String title;
    private long size;
    private long duration;
    private int width;
    private int height;
    private String mimeType;

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        public Photo createFromParcel(Parcel source) {
            return new Photo(source);
        }

        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    public Photo( String uri, boolean selected, String title, long size, long duration, int width, int height, String mimeType) {

        this.uri = uri;
        this.selected = selected;
        this.title = title;
        this.size = size;
        this.duration = duration;
        this.width = width;
        this.height = height;
        this.mimeType = mimeType;
    }

    public Photo() {
    }

    public Photo(Parcel in) {
        uri = in.readString();
        selected = in.readInt() == 1;
        title = in.readString();
        size = in.readLong();
        duration = in.readLong();
        width = in.readInt();
        height = in.readInt();
        mimeType = in.readString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Photo photo = (Photo) o;
        return ObjectsUtils.equals(getUri(), photo.getUri());
    }

    @Override
    public int hashCode() {
        return ObjectsUtils.hash(getUri());
    }


    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isGif() {
        return TextUtils.equals(getMimeType(), "image/gif");
    }

    @Override
    public String toString() {
        return "Photo{" +
                "uri='" + uri + '\'' +
                ", selected=" + selected +
                ", title='" + title + '\'' +
                ", size=" + size +
                ", duration=" + duration +
                ", width=" + width +
                ", height=" + height +
                ", mimeType='" + mimeType + '\'' +
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uri);
        dest.writeInt(selected ? 1 : 0);
        dest.writeString(title);
        dest.writeLong(size);
        dest.writeLong(duration);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeString(mimeType);
    }
}
