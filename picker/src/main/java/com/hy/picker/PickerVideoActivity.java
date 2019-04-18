package com.hy.picker;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.hy.picker.utils.CommonUtils;
import com.hy.picker.model.Photo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created time : 2019/3/17 11:09 PM.
 *
 * @author HY
 */
public class PickerVideoActivity extends AppCompatActivity implements PickerConstants, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {


    private VideoView mVideoView;
    private MediaController mMediaController;
    private int mPositionWhenPaused = -1;
    private Photo mPhoto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);

        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_activity_video_player);
        ImageView ivBack = findViewById(R.id.picker_back);
        mVideoView = findViewById(R.id.picker_video_view);
        mPhoto = getIntent().getParcelableExtra(PICKER_EXTRA_PHOTO);
        if (null == mPhoto) {
            Toast.makeText(this, getString(R.string.picker_video_play_error), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mMediaController = new MediaController(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setMediaController(mMediaController);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) ivBack.getLayoutParams();
        layoutParams.topMargin= CommonUtils.getStatusBarHeight(this);
        ivBack.setLayoutParams(layoutParams);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mVideoView.getLayoutParams();
        params.topMargin= CommonUtils.getStatusBarHeight(this);
        mVideoView.setLayoutParams(params);

        ivBack.setOnClickListener(v -> finish());

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new ContextWrapper(newBase) {
            @Override
            public Object getSystemService(String name) {
                if (Context.AUDIO_SERVICE.equals(name)) {
                    return getApplicationContext().getSystemService(name);
                }
                return super.getSystemService(name);
            }
        });
    }

    @Override
    protected void onDestroy() {
        mMediaController = null;
        mVideoView = null;
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (null == mPhoto) {
            Toast.makeText(this, getString(R.string.picker_video_play_error), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Play Video
        mVideoView.setVideoPath(mPhoto.getUri());
        mVideoView.start();

    }


    @Override
    protected void onResume() {
        if (mPositionWhenPaused >= 0) {
            mVideoView.seekTo(mPositionWhenPaused);
            mPositionWhenPaused = -1;
        }
        super.onResume();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public void onPause() {
        // Stop video when the activity is pause.
        mPositionWhenPaused = mVideoView.getCurrentPosition();
        mVideoView.stopPlayback();

        super.onPause();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.setOnInfoListener((mp1, what, extra) -> {
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                // video started
                mVideoView.setBackgroundColor(Color.TRANSPARENT);
                return true;
            }
            return false;
        });
    }
}
