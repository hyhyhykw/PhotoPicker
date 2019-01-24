package com.picker2.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hy.picker.R;
import com.picker2.model.Photo;
import com.picker2.model.PhotoDirectory;

import java.util.List;

import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.MIME_TYPE;



//                             _ooOoo_
//                            o8888888o
//                            88" . "88
//                            (| -_- |)
//                            O\  =  /O
//                         ____/`---'\____
//                       .'  \\|     |//  `.
//                      /  \\|||  :  |||//  \
//                     /  _||||| -:- |||||-  \
//                     |   | \\\  -  /// |   |
//                     | \_|  ''\---/''  |   |
//                     \  .-\__  `-`  ___/-. /
//                   ___`. .'  /--.--\  `. . __
//                ."" '<  `.___\_<|>_/___.'  >'"".
//               | | :  `- \`.;`\ _ /`;.`/ - ` : | |
//               \  \ `-.   \_ __\ /__ _/   .-` /  /
//          ======`-.____`-.___\_____/___.-`____.-'======
//                             `=---='
//         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//                   佛祖保佑             永无BUG
//
//             佛曰:
//                   写字楼里写字间，写字间中程序员；
//                   程序人员写程序，又将程序换酒钱；
//                   酒醒只在屏前坐，酒醉还来屏下眠；
//                   酒醉酒醒日复日，屏前屏下年复年；
//                   但愿老死电脑间，不愿鞠躬老板前；
//                   奔驰宝马贵者趣，公交自行程序员；
//                   别人笑我太疯癫，我笑自己命太贱；
//                   但见满街漂亮妹，哪个归得程序员？
public class MediaScannerUtils {

    @NonNull
    private Builder mBuilder;

    private MediaScannerUtils(@NonNull Builder builder) {
        mBuilder = builder;
    }

    public MediaScannerUtils(Context context) {
        mBuilder = new Builder(context);
    }

    public void scanner(OnResultListener listener) {
        new MultiScannerTask(mBuilder, listener).execute();
    }

    public void scanner(OnSingleResultListener listener) {
        new SingleScannerTask(mBuilder, listener).execute();
    }


    private static final String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
    };

    private static final String[] VIDEO_PROJECTION = {
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
    };

    public static class Builder {
        private boolean gif;
        private boolean gifOnly;
        private boolean video;
        private Context mContext;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder gif(boolean gif) {
            this.gif = gif;
            return this;
        }

        public Builder gifOnly(boolean gifOnly) {
            this.gifOnly = gifOnly;
            return this;
        }

        public Builder video(boolean video) {
            this.video = video;
            return this;
        }

        private boolean add = true;
        private String path;

        public Builder add(boolean add) {
            this.add = add;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public MediaScannerUtils build() {
            return new MediaScannerUtils(this);
        }
    }

    public interface OnSingleResultListener {
        void onResult(@Nullable Photo photo, int updateIndex);
    }

    public interface OnResultListener {
        void onResult(boolean success);
    }

    private static final class MultiScannerTask extends AsyncTask<String, Void, List<PhotoDirectory>> {

        private Builder mBuilder;
        private OnResultListener mListener;

        MultiScannerTask(Builder builder, OnResultListener listener) {
            mBuilder = builder;
            mListener = listener;
        }

        @Override
        protected List<PhotoDirectory> doInBackground(String... strings) {
            String[] projection;
            Uri queryUri;
            String order;
            String selections;
            String[] selectionArgs;
            if (mBuilder.video) {
                projection = VIDEO_PROJECTION;
                queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                order = MediaStore.Video.Media.DATE_ADDED + " DESC";

                selections = null;
                selectionArgs = null;
            } else {
                projection = IMAGE_PROJECTION;
                queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                order = MediaStore.Images.Media.DATE_ADDED + " DESC";
                if (mBuilder.gifOnly) {
                    selections = MIME_TYPE + "=?";
                    selectionArgs = new String[]{"image/gif"};
                } else {
                    if (mBuilder.gif) {
                        selections = MIME_TYPE + "=? or " + MIME_TYPE + "=? or " + MIME_TYPE + "=? " + "or " + MIME_TYPE + "=?";
                        selectionArgs = new String[]{"image/jpeg", "image/png", "image/bmp", "image/gif"};
                    } else {
                        selections = MIME_TYPE + "=? or " + MIME_TYPE + "=? or " + MIME_TYPE + "=?";
                        selectionArgs = new String[]{"image/jpeg", "image/png", "image/bmp"};
                    }
                }
            }

            Context context = mBuilder.mContext;
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(queryUri, projection, selections, selectionArgs, order);

            if (null == cursor) {
                return null;
            }

            final List<PhotoDirectory> directories = MediaListHolder.allDirectories;
            final PhotoDirectory photoDirectoryAll = new PhotoDirectory();

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
            if (mBuilder.video) {
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

            while (cursor.moveToNext()) {
                long size = cursor.getInt(cursor.getColumnIndexOrThrow(SIZE));
                if (size < 1) continue;

                long datetaken = cursor.getLong(cursor.getColumnIndexOrThrow(DATE_TAKEN));
                String bucketId = cursor.getString(cursor.getColumnIndexOrThrow(BUCKET_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(DATA));

                String title = cursor.getString(cursor.getColumnIndexOrThrow(TITLE));
                String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MIME_TYPE));
                int width = cursor.getInt(cursor.getColumnIndexOrThrow(WIDTH));
                int height = cursor.getInt(cursor.getColumnIndexOrThrow(HEIGHT));
                long duration;

                if (mBuilder.video) {
                    duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    if (duration < 1000) continue;
                } else {
                    duration = 0;
                }


                PhotoDirectory photoDirectory = new PhotoDirectory();
                photoDirectory.setId(bucketId);
                photoDirectory.setName(name);

                Photo photo = new Photo(path, title, size, duration, width, height, mimeType, datetaken);

                if (!directories.contains(photoDirectory)) {
                    photoDirectory.setCoverPath(path);
                    photoDirectory.addPhoto(photo);
                    photoDirectory.setDateAdded(cursor.getLong(cursor.getColumnIndexOrThrow(DATE_ADDED)));
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

            cursor.close();
            return directories;
        }

        @Override
        protected void onPostExecute(List<PhotoDirectory> photoDirectories) {
            super.onPostExecute(photoDirectories);
            if (null == photoDirectories) {
                mListener.onResult(false);
            } else {
                mListener.onResult(true);
            }
        }
    }

    private static class ResultParams {
        Photo mPhoto;
        int position;

        public ResultParams(Photo photo, int position) {
            mPhoto = photo;
            this.position = position;
        }
    }

    private static final class SingleScannerTask extends AsyncTask<String, Void, ResultParams> {
        private Builder mBuilder;
        private OnSingleResultListener mListener;

        SingleScannerTask(Builder builder, OnSingleResultListener listener) {
            mBuilder = builder;
            mListener = listener;
        }

        @Override
        protected ResultParams doInBackground(String... strings) {
            String[] projection;
            Uri queryUri;
            String order;

            String selections = DATA + "=?";
            String[] selectionArgs = new String[]{mBuilder.path};

            if (mBuilder.video) {
                projection = VIDEO_PROJECTION;
                queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                order = MediaStore.Video.Media.DATE_ADDED + " DESC";
            } else {
                projection = IMAGE_PROJECTION;
                queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                order = MediaStore.Images.Media.DATE_ADDED + " DESC";
            }


            Context context = mBuilder.mContext;
            ContentResolver resolver = context.getContentResolver();
            Cursor cursor = resolver.query(queryUri, projection, selections, selectionArgs, order);

            if (null == cursor) {
                return null;
            }

            List<PhotoDirectory> directories = MediaListHolder.allDirectories;

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
            if (mBuilder.video) {
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


            if (cursor.moveToFirst()) {

                long datetaken = cursor.getLong(cursor.getColumnIndexOrThrow(DATE_TAKEN));

                String bucketId = cursor.getString(cursor.getColumnIndexOrThrow(BUCKET_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(DATA));
                long size = cursor.getInt(cursor.getColumnIndexOrThrow(SIZE));

                String title = cursor.getString(cursor.getColumnIndexOrThrow(TITLE));
                String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MIME_TYPE));
                int width = cursor.getInt(cursor.getColumnIndexOrThrow(WIDTH));
                int height = cursor.getInt(cursor.getColumnIndexOrThrow(HEIGHT));
                long duration;
                if (mBuilder.video) {
                    duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    if (duration < 1000) return null;
                } else {
                    duration = 0;
                }

                PhotoDirectory photoDirectory = new PhotoDirectory();
                photoDirectory.setId(bucketId);
                photoDirectory.setName(name);


                Photo photo = new Photo(path, title, size, duration, width, height, mimeType, datetaken);

                int updateIndex;
                if (!directories.contains(photoDirectory)) {
                    photoDirectory.setCoverPath(path);
                    photoDirectory.addPhoto(photo);
                    photoDirectory.setDateAdded(cursor.getLong(cursor.getColumnIndexOrThrow(DATE_ADDED)));

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

                if (mBuilder.add) {
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


                cursor.close();
                return new ResultParams(photo, updateIndex);
            }

            return null;
        }

        @Override
        protected void onPostExecute(ResultParams resultParams) {
            super.onPostExecute(resultParams);
            if (null == resultParams) {
                mListener.onResult(null, -1);
            } else {
                mListener.onResult(resultParams.mPhoto, resultParams.position);
            }
        }
    }

}
