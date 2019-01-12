package com.picker2.utils;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.hy.picker.PhotoPicker;
import com.hy.picker.R;
import com.hy.picker.utils.Logger;
import com.picker2.PickerConstants;
import com.picker2.model.Photo;
import com.picker2.model.PhotoDirectory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by donglua on 15/5/31.
 */
public class MediaStoreHelper implements PickerConstants {

    public static void getPhotoDirs(FragmentActivity activity, Bundle args, PhotosResultCallback resultCallback) {
        LoaderManager.getInstance(activity)
                .initLoader(1, args, new PhotoDirLoaderCallbacks(activity, resultCallback));
    }

    public static void getPhoto(FragmentActivity activity, Bundle args, PhotoSingleCallback callback) {
        LoaderManager.getInstance(activity)
                .initLoader(0, args, new PhotoLoaderCallback(activity, callback));
    }

    private static class PhotoLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {
        private WeakReference<Context> context;
        private PhotoSingleCallback mCallback;
        private boolean video;
        private boolean add;

        public PhotoLoaderCallback(Context context, PhotoSingleCallback callback) {
            this.context = new WeakReference<>(context);
            mCallback = callback;
        }

        @NonNull
        @Override
        public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle bundle) {
            String path = bundle.getString("path");
            video = bundle.getBoolean(EXTRA_PICK_VIDEO, false);
            add = bundle.getBoolean(PICKER_EXTRA_ADD, true);
            return new PhotoLoader(context.get(), path, video);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
            if (data == null) {
                if (null != mCallback) {
                    mCallback.onResultCallback(null, -1);
                }
                return;
            }
            List<PhotoDirectory> directories = MediaListHolder.allDirectories;
            Context context = this.context.get();
            if (context == null) return;

            String BUCKET_ID;
            String TITLE;
            String DATA;
            String MIME_TYPE;
            String DATE_ADDED;
            String SIZE;
            String WIDTH;
            String DATE_TAKEN;
            String HEIGHT;
            String BUCKET_DISPLAY_NAME;
            if (video) {
                BUCKET_ID = MediaStore.Video.Media.BUCKET_ID;
                TITLE = MediaStore.Video.Media.TITLE;
                DATA = MediaStore.Video.Media.DATA;
                MIME_TYPE = MediaStore.Video.Media.MIME_TYPE;
                DATE_ADDED = MediaStore.Video.Media.DATE_ADDED;
                SIZE = MediaStore.Video.Media.SIZE;
                WIDTH = MediaStore.Video.Media.WIDTH;
                HEIGHT = MediaStore.Video.Media.HEIGHT;
                BUCKET_DISPLAY_NAME = MediaStore.Video.Media.BUCKET_DISPLAY_NAME;
                DATE_TAKEN = MediaStore.Video.Media.DATE_TAKEN;
            } else {
                BUCKET_ID = MediaStore.Images.Media.BUCKET_ID;
                TITLE = MediaStore.Images.Media.TITLE;
                DATA = MediaStore.Images.Media.DATA;
                MIME_TYPE = MediaStore.Images.Media.MIME_TYPE;
                DATE_ADDED = MediaStore.Images.Media.DATE_ADDED;
                SIZE = MediaStore.Images.Media.SIZE;
                WIDTH = MediaStore.Images.Media.WIDTH;
                HEIGHT = MediaStore.Images.Media.HEIGHT;
                DATE_TAKEN = MediaStore.Images.Media.DATE_TAKEN;
                BUCKET_DISPLAY_NAME = MediaStore.Images.Media.BUCKET_DISPLAY_NAME;
            }


            if (data.moveToFirst()) {

                long datetaken = data.getLong(data.getColumnIndexOrThrow(DATE_TAKEN));

                String bucketId = data.getString(data.getColumnIndexOrThrow(BUCKET_ID));
                String name = data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME));
                String path = data.getString(data.getColumnIndexOrThrow(DATA));
                long size = data.getInt(data.getColumnIndexOrThrow(SIZE));

                String title = data.getString(data.getColumnIndexOrThrow(TITLE));
                String mimeType = data.getString(data.getColumnIndexOrThrow(MIME_TYPE));
                int width = data.getInt(data.getColumnIndexOrThrow(WIDTH));
                int height = data.getInt(data.getColumnIndexOrThrow(HEIGHT));
                long duration;
                if (video) {
                    duration = data.getLong(data.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                } else {
                    duration = 0;
                }

                PhotoDirectory photoDirectory = new PhotoDirectory();
                photoDirectory.setId(bucketId);
                photoDirectory.setName(name);

                Logger.e("width===========" + width);
                Logger.e("height===========" + height);

                Photo photo = new Photo(path, title, size, duration, width, height, mimeType, datetaken);

                int updateIndex;
                if (!directories.contains(photoDirectory)) {
                    photoDirectory.setCoverPath(path);
                    photoDirectory.addPhoto(photo);
                    photoDirectory.setDateAdded(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED)));

                    if (directories.size() == 1) {
                        directories.add(photoDirectory);
                    } else {
                        directories.add(1, photoDirectory);
                    }
                    updateIndex = -1;
                } else {

                    int index = directories.indexOf(photoDirectory);
                    PhotoDirectory directory = directories.get(index);
                    directory.setCoverPath(path);
                    if (directory.getPhotos().isEmpty()) {
                        directory.addPhoto(photo);
                    } else {
                        directory.addPhoto(0, photo);
                    }
                    updateIndex = index;
                }

                if (add) {
                    if (MediaListHolder.selectPhotos.isEmpty()) {
                        MediaListHolder.selectPhotos.add(photo);
                    } else {
                        MediaListHolder.selectPhotos.add(0, photo);
                    }
                }

                PhotoDirectory allDirectory = MediaListHolder.allDirectories.get(0);
                allDirectory.setCoverPath(path);
                if (allDirectory.getPhotos().isEmpty()) {
                    allDirectory.addPhoto(photo);
                } else {
                    allDirectory.addPhoto(0, photo);
                }


                if (mCallback != null) {
                    mCallback.onResultCallback(photo, updateIndex);
                }
            }

        }

        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        }
    }


    private static class PhotoDirLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        private WeakReference<Context> context;
        private PhotosResultCallback resultCallback;

        private boolean video;
        private ArrayList<Photo> photos;

        public PhotoDirLoaderCallbacks(Context context, PhotosResultCallback resultCallback) {
            this.context = new WeakReference<>(context);
            this.resultCallback = resultCallback;
        }


        @NonNull
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            boolean gif = args.getBoolean(PhotoPicker.EXTRA_SHOW_GIF, false);
            boolean gifOnly = args.getBoolean(PhotoPicker.EXTRA_ONLY_GIF, false);
            video = args.getBoolean(PhotoPicker.EXTRA_PICK_VIDEO, false);
            photos = args.getParcelableArrayList(PhotoPicker.EXTRA_ITEMS);

            return new PhotoDirectoryLoader(context.get(), gif, gifOnly, video);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> loader, final Cursor data) {

            if (data == null) return;

            final List<PhotoDirectory> directories = MediaListHolder.allDirectories;
            final PhotoDirectory photoDirectoryAll = new PhotoDirectory();

            final Context context = this.context.get();
            if (context == null) return;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    final String BUCKET_ID;
                    final String TITLE;
                    final String DATA;
                    final String MIME_TYPE;
                    final String DATE_ADDED;
                    final String SIZE;
                    final String WIDTH;
                    final String HEIGHT;
                    final String BUCKET_DISPLAY_NAME;
                    final String DATE_TAKEN;
                    if (video) {
                        BUCKET_ID = MediaStore.Video.Media.BUCKET_ID;
                        TITLE = MediaStore.Video.Media.TITLE;
                        DATA = MediaStore.Video.Media.DATA;
                        MIME_TYPE = MediaStore.Video.Media.MIME_TYPE;
                        DATE_ADDED = MediaStore.Video.Media.DATE_ADDED;
                        SIZE = MediaStore.Video.Media.SIZE;
                        WIDTH = MediaStore.Video.Media.WIDTH;
                        HEIGHT = MediaStore.Video.Media.HEIGHT;
                        DATE_TAKEN = MediaStore.Video.Media.DATE_TAKEN;
                        BUCKET_DISPLAY_NAME = MediaStore.Video.Media.BUCKET_DISPLAY_NAME;
                        photoDirectoryAll.setName(context.getString(R.string.picker_all_video));
                    } else {
                        BUCKET_ID = MediaStore.Images.Media.BUCKET_ID;
                        TITLE = MediaStore.Images.Media.TITLE;
                        DATA = MediaStore.Images.Media.DATA;
                        MIME_TYPE = MediaStore.Images.Media.MIME_TYPE;
                        DATE_ADDED = MediaStore.Images.Media.DATE_ADDED;
                        SIZE = MediaStore.Images.Media.SIZE;
                        WIDTH = MediaStore.Images.Media.WIDTH;
                        HEIGHT = MediaStore.Images.Media.HEIGHT;
                        DATE_TAKEN = MediaStore.Images.Media.DATE_TAKEN;
                        BUCKET_DISPLAY_NAME = MediaStore.Images.Media.BUCKET_DISPLAY_NAME;
                        photoDirectoryAll.setName(context.getString(R.string.picker_all_image));
                    }

                    photoDirectoryAll.setId("ALL");

                    while (data.moveToNext()) {
                        long datetaken = data.getLong(data.getColumnIndexOrThrow(DATE_TAKEN));
                        String bucketId = data.getString(data.getColumnIndexOrThrow(BUCKET_ID));
                        String name = data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME));
                        String path = data.getString(data.getColumnIndexOrThrow(DATA));
                        long size = data.getInt(data.getColumnIndexOrThrow(SIZE));

                        String title = data.getString(data.getColumnIndexOrThrow(TITLE));
                        String mimeType = data.getString(data.getColumnIndexOrThrow(MIME_TYPE));
                        int width = data.getInt(data.getColumnIndexOrThrow(WIDTH));
                        int height = data.getInt(data.getColumnIndexOrThrow(HEIGHT));
                        long duration;

                        if (video) {
                            duration = data.getLong(data.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                            MediaMetadataRetriever retr = new MediaMetadataRetriever();//获取视频第一帧
                            retr.setDataSource(path);
                            String orientation;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                orientation = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                            } else {
                                orientation = "0";
                            }
                            if ("90".equals(orientation)) {
                                int temp = width;
                                width = height;
                                height = temp;
                            }
                            Logger.e("width===========" + width);
                            Logger.e("height===========" + height);
                        } else {
                            duration = 0;
                        }

                        if (size < 1) continue;

                        PhotoDirectory photoDirectory = new PhotoDirectory();
                        photoDirectory.setId(bucketId);
                        photoDirectory.setName(name);

                        Photo photo = new Photo(path, title, size, duration, width, height, mimeType, datetaken);

                        if (!directories.contains(photoDirectory)) {
                            photoDirectory.setCoverPath(path);
                            photoDirectory.addPhoto(photo);
                            photoDirectory.setDateAdded(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED)));
                            directories.add(photoDirectory);
                        } else {
                            directories.get(directories.indexOf(photoDirectory))
                                    .addPhoto(photo);
                        }

                        photoDirectoryAll.addPhoto(photo);
                        MediaListHolder.currentPhotos.add(photo);
                    }
                    if (photoDirectoryAll.getPhotos().size() > 0) {
                        photoDirectoryAll.setCoverPath(photoDirectoryAll.getPhotos().get(0).getUri());
                    }
                    directories.add(0, photoDirectoryAll);
                    if (resultCallback != null) {
                        resultCallback.onResultCallback();
                    }
                }
            }).start();

        }

        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        }
    }


    public static void destroyLoader(FragmentActivity activity, int id) {
        LoaderManager.getInstance(activity).destroyLoader(id);
    }

    public interface PhotosResultCallback {
        void onResultCallback();
    }

    public interface PhotoSingleCallback {
        void onResultCallback(@Nullable Photo photo, int updateIndex);
    }

}
