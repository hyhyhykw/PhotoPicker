package com.hy.photopicker;

import android.content.Intent;
import android.os.Bundle;

import com.hy.picker.PhotoPicker;
import com.hy.picker.model.Photo;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity implements FeedbackSelectPictureAdapter.OnItemClickListener {
    RecyclerView mRcyPhoto;
    private final FeedbackSelectPictureAdapter mFeedbackSelectPictureAdapter = new FeedbackSelectPictureAdapter();
//    private PhotoPicker mPhotoPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRcyPhoto = findViewById(R.id.rcy_photo);
        mFeedbackSelectPictureAdapter.setOnItemClickListener(this);

        mRcyPhoto.setLayoutManager(new GridLayoutManager(this, 3));
        mRcyPhoto.setAdapter(mFeedbackSelectPictureAdapter);
//        mPhotoPicker = new PhotoPicker();


    }

    @Override
    public void onClick(int position, @FeedbackSelectPictureAdapter.Type int type) {
        switch (type) {
            case FeedbackSelectPictureAdapter.TYPE_PHOTO:
                PhotoPicker.preview(position, mFeedbackSelectPictureAdapter.mData);
//                startActivity(
//                        new Intent(this, TestPreviewActivity.class)
//                                .putExtra("test", mFeedbackSelectPictureAdapter.getItem(position))
//                );



                break;
            case FeedbackSelectPictureAdapter.TYPE_ADD:
                new PhotoPicker()
                        .max(9)
//                        .video()
                        .select(mFeedbackSelectPictureAdapter.mData)
                        .start(this);
//
//                new PhotoPicker()
//                        .edit(true)
//                        .openCamera(this);
                break;
            case FeedbackSelectPictureAdapter.TYPE_DELETE:
                mFeedbackSelectPictureAdapter.deleteItem(position);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (PhotoPicker.isSingle(requestCode)) {
            Photo photo = PhotoPicker.obtainTakeResult(requestCode, resultCode, data);
            if (null != photo) {
                mFeedbackSelectPictureAdapter.addItem(photo);
            }
        } else {
            ArrayList<Photo> photos = PhotoPicker.obtainMultiResult(requestCode, resultCode, data);
            mFeedbackSelectPictureAdapter.reset(photos);
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

//    @Override
//    public void onPicked(ArrayList<Photo> picItems) {
//        for (Photo picItem : picItems) {
//            Logger.e(picItem);
//        }
////        mFeedbackSelectPictureAdapter.reset(picItems);
//    }

}
