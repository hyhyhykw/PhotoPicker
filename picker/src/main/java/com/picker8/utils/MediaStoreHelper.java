package com.picker8.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.hy.picker.PhotoPicker;
import com.hy.picker.R;
import com.picker8.model.Photo;
import com.picker8.model.PhotoDirectory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by donglua on 15/5/31.
 */
public class MediaStoreHelper {

    public static void getPhotoDirs(FragmentActivity activity, Bundle args, PhotosResultCallback resultCallback) {
        LoaderManager.getInstance(activity)
                .initLoader(0, args, new PhotoDirLoaderCallbacks(activity, resultCallback));
    }

    public static void getPhoto(FragmentActivity activity, Bundle args, PhotoSingleCallback callback) {
        LoaderManager.getInstance(activity)
                .initLoader(0, args, new PhotoLoaderCallback(activity, callback));
    }

    private static class PhotoLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {
        private WeakReference<Context> context;
        private PhotoSingleCallback mCallback;
        private boolean video;

        public PhotoLoaderCallback(Context context, PhotoSingleCallback callback) {
            this.context = new WeakReference<>(context);
            mCallback = callback;
        }

        @NonNull
        @Override
        public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle bundle) {
            String path = bundle.getString("path");
            video = bundle.getBoolean(PhotoPicker.EXTRA_PICK_VIDEO, false);
            return new PhotoLoader(context.get(), path, video);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
            if (data == null) {
                if (null != mCallback) {
                    mCallback.onResultCallback(null);
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

            } else {
                BUCKET_ID = MediaStore.Images.Media.BUCKET_ID;
                TITLE = MediaStore.Images.Media.TITLE;
                DATA = MediaStore.Images.Media.DATA;
                MIME_TYPE = MediaStore.Images.Media.MIME_TYPE;
                DATE_ADDED = MediaStore.Images.Media.DATE_ADDED;
                SIZE = MediaStore.Images.Media.SIZE;
                WIDTH = MediaStore.Images.Media.WIDTH;
                HEIGHT = MediaStore.Images.Media.HEIGHT;
                BUCKET_DISPLAY_NAME = MediaStore.Images.Media.BUCKET_DISPLAY_NAME;
            }


            if (data.moveToFirst()) {

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

                Photo photo = new Photo(path, false, title, size, duration, width, height, mimeType);

                if (!directories.contains(photoDirectory)) {
                    photoDirectory.setCoverPath(path);
                    photoDirectory.addPhoto(photo);
                    photoDirectory.setDateAdded(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED)));
                    directories.add(photoDirectory);
                } else {
                    directories.get(directories.indexOf(photoDirectory))
                            .addPhoto(photo);
                }

                if (mCallback != null) {
                    mCallback.onResultCallback(photo);
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
        public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

            if (data == null) return;

            List<PhotoDirectory> directories = new ArrayList<>();
            PhotoDirectory photoDirectoryAll = new PhotoDirectory();

            Context context = this.context.get();
            if (context == null) return;

            String BUCKET_ID;
            String TITLE;
            String DATA;
            String MIME_TYPE;
            String DATE_ADDED;
            String DATE_TAKEN;
            String SIZE;
            String WIDTH;
            String HEIGHT;
            String BUCKET_DISPLAY_NAME;

            if (video) {
                BUCKET_ID = MediaStore.Video.Media.BUCKET_ID;
                TITLE = MediaStore.Video.Media.TITLE;
                DATA = MediaStore.Video.Media.DATA;
                MIME_TYPE = MediaStore.Video.Media.MIME_TYPE;
                DATE_ADDED = MediaStore.Video.Media.DATE_ADDED;
                DATE_TAKEN = MediaStore.Video.Media.DATE_TAKEN;
                SIZE = MediaStore.Video.Media.SIZE;
                WIDTH = MediaStore.Video.Media.WIDTH;
                HEIGHT = MediaStore.Video.Media.HEIGHT;
                BUCKET_DISPLAY_NAME = MediaStore.Video.Media.BUCKET_DISPLAY_NAME;
                photoDirectoryAll.setName(context.getString(R.string.picker_all_video));
            } else {
                BUCKET_ID = MediaStore.Images.Media.BUCKET_ID;
                TITLE = MediaStore.Images.Media.TITLE;
                DATA = MediaStore.Images.Media.DATA;
                MIME_TYPE = MediaStore.Images.Media.MIME_TYPE;
                DATE_ADDED = MediaStore.Images.Media.DATE_ADDED;
                DATE_TAKEN = MediaStore.Images.Media.DATE_TAKEN;
                SIZE = MediaStore.Images.Media.SIZE;
                WIDTH = MediaStore.Images.Media.WIDTH;
                HEIGHT = MediaStore.Images.Media.HEIGHT;
                BUCKET_DISPLAY_NAME = MediaStore.Images.Media.BUCKET_DISPLAY_NAME;
                photoDirectoryAll.setName(context.getString(R.string.picker_all_image));
            }

            photoDirectoryAll.setId("ALL");

            while (data.moveToNext()) {

                String bucketId = data.getString(data.getColumnIndexOrThrow(BUCKET_ID));
                String name = data.getString(data.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME));
                String path = data.getString(data.getColumnIndexOrThrow(DATA));
                long size = data.getInt(data.getColumnIndexOrThrow(SIZE));

                String title = data.getString(data.getColumnIndexOrThrow(TITLE));
                String mimeType = data.getString(data.getColumnIndexOrThrow(MIME_TYPE));
                String dateTaken = data.getString(data.getColumnIndexOrThrow(DATE_TAKEN));
                int width = data.getInt(data.getColumnIndexOrThrow(WIDTH));
                int height = data.getInt(data.getColumnIndexOrThrow(HEIGHT));
                long duration;
                if (video) {
                    duration = data.getLong(data.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                } else {
                    duration = 0;
                }

                if (size < 1) continue;

                PhotoDirectory photoDirectory = new PhotoDirectory();
                photoDirectory.setId(bucketId);
                photoDirectory.setName(name);

                Photo photo = new Photo(path, false, title, size, duration, width, height, mimeType);

                if (null != photos && !photos.isEmpty()) {
                    boolean remove = photos.remove(photo);
                    photo.setSelected(remove);
                    MediaListHolder.selectPhotos.add(photo);
                }

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
            }
            if (photoDirectoryAll.getPhotos().size() > 0) {
                photoDirectoryAll.setCoverPath(photoDirectoryAll.getPhotos().get(0).getUri());
            }
            directories.add(0, photoDirectoryAll);
            if (resultCallback != null) {
                resultCallback.onResultCallback(directories);
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {

        }
    }


    public interface PhotosResultCallback {
        void onResultCallback(List<PhotoDirectory> directories);
    }

    public interface PhotoSingleCallback {
        void onResultCallback(@Nullable Photo photo);
    }

}
