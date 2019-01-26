package com.hy.photopicker;

import android.os.Bundle;

import com.hy.picker.PhotoListener;
import com.hy.picker.PhotoPicker;
import com.hy.picker.TakePhotoListener;
import com.hy.picker.utils.Logger;
import com.picker2.model.Photo;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity implements FeedbackSelectPictureAdapter.OnItemClickListener, PhotoListener, TakePhotoListener {
    RecyclerView mRcyPhoto;
    private FeedbackSelectPictureAdapter mFeedbackSelectPictureAdapter = new FeedbackSelectPictureAdapter();
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

                break;
            case FeedbackSelectPictureAdapter.TYPE_ADD:
                new PhotoPicker()
                        .showCamera(true)
                        .max(9)
//                        .video()
                        .start(this);
//                new PhotoPicker()
//                        .edit(true)
//                        .openCamera(this, this);
                break;
            case FeedbackSelectPictureAdapter.TYPE_DELETE:
                mFeedbackSelectPictureAdapter.deleteItem(position);
                break;
        }
    }


    @Override
    public void onPicked(ArrayList<Photo> picItems) {
        for (Photo picItem : picItems) {
            Logger.e(picItem);
        }
//        mFeedbackSelectPictureAdapter.reset(picItems);
    }

    @Override
    public void onTake(Photo picItem) {
//        mFeedbackSelectPictureAdapter.addItem(picItem);
    }
}
