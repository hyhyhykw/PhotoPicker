package com.hy.picker.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.hy.picker.PhotoContext;
import com.hy.picker.PickerConstants;
import com.hy.picker.PicturePreviewActivity;
import com.hy.picker.PictureSelectorActivity;
import com.hy.picker.R;
import com.hy.picker.model.Photo;
import com.hy.picker.utils.CommonUtils;
import com.hy.picker.utils.MediaListHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created time : 2019/4/18 1:06 PM.
 *
 * @author HY
 */
public class PictureAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements PickerConstants {

    private final List<Photo> mPhotos = new ArrayList<>();

    private final int max;
    private final boolean preview;
    private final boolean camera;
    private final boolean video;
    private final Drawable mDefaultDrawable;

    public PictureAdapter(int max, boolean preview, boolean camera, boolean video, Drawable defaultDrawable,int size) {
        this.max = max;
        this.preview = preview;
        this.camera = camera;
        this.video = video;
        mDefaultDrawable = defaultDrawable;
        this.size=size;
    }

    public void reset(List<Photo> photos) {
        mPhotos.clear();
        mPhotos.addAll(photos);
        notifyDataSetChanged();
    }

    public void add(int index, Photo photo) {
        mPhotos.add(index, photo);
        notifyItemInserted(index);

    }

    public void add(Photo photo) {
        if (mPhotos.isEmpty()) {
            mPhotos.add(photo);
            notifyItemInserted(0);
        } else {
            mPhotos.add(photo);
            notifyItemInserted(mPhotos.size() - 1);
        }
    }

    private int getTotalSelectedNum() {
        return MediaListHolder.selectPhotos.size();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();

//        if (size == 0) {
//            size = (PhotoContext.getScreenWidth() - SizeUtils.dp2px(context, 4) * 3) / 4;
//        }

        LayoutInflater inflater = LayoutInflater.from(context);
        RecyclerView.ViewHolder holder;

        if (viewType == 0) {
            View cameraView = inflater.inflate(R.layout.picker_grid_camera, parent, false);

            holder = new CameraHolder(cameraView);
        } else {
            View convertView = inflater.inflate(R.layout.picker_grid_item, parent, false);
            holder = new ItemHolder(convertView);
        }

        return holder;
    }

    private final int size;

    public interface OnItemListener {
        void onItemClick(Photo photo);

        void onItemChecked();

        void onCameraClick();
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        mOnItemListener = onItemListener;
    }

    private OnItemListener mOnItemListener;

    class ItemHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView image;
        View mask;
        AppCompatCheckBox checkBox;
        View itemView;
        ImageView ivGif;
        RelativeLayout lytVideo;
        TextView tvTime;

        ItemHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            image = itemView.findViewById(R.id.picker_photo_image);
            mask = itemView.findViewById(R.id.picker_item_mask);
            checkBox = itemView.findViewById(R.id.picker_item_checkbox);
            ivGif = itemView.findViewById(R.id.picker_iv_gif);
            lytVideo = itemView.findViewById(R.id.picker_lyt_video);
            tvTime = itemView.findViewById(R.id.picker_video_time);
            if (video) {
                checkBox.setVisibility(View.GONE);
                lytVideo.setVisibility(View.VISIBLE);
            }
        }

        void bind() {
            int adapterPosition = getAdapterPosition();
            int position;
            if (camera) {
                position = adapterPosition - 1;
            } else {
                position = adapterPosition;
            }

            Photo item = mPhotos.get(position);

            if (item.isGif()) {
                ivGif.setVisibility(View.VISIBLE);
            } else {
                ivGif.setVisibility(View.GONE);
            }


            ViewGroup.LayoutParams params = image.getLayoutParams();
            if (params.height != size || params.width != size) {
                params.width = size;
                params.height = size;
                image.setLayoutParams(params);
            }


            GenericDraweeHierarchy hierarchy = image.getHierarchy();
            hierarchy.setPlaceholderImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP);
            hierarchy.setFailureImage(mDefaultDrawable, ScalingUtils.ScaleType.CENTER_CROP);
            image.setController(PictureSelectorActivity.getDraweeController(image, Uri.fromFile(new File(item.getUri())), size, size));
            checkBox.setChecked(MediaListHolder.selectPhotos.contains(item));

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed()) {
                    if (getTotalSelectedNum() == max && isChecked) {
                        Toast.makeText(
                                PhotoContext.getContext().getApplicationContext(),
                                PhotoContext.getContext().getResources().getQuantityString(R.plurals.picker_picsel_selected_max, 1, max),
                                Toast.LENGTH_SHORT).show();
                        buttonView.setChecked(false);
                    } else {
//                            item.setSelected(isChecked);
                        if (isChecked) {
                            item.setSelected(true);
                            MediaListHolder.selectPhotos.add(item);
                            mask.setBackgroundColor(PhotoContext.getContext().
                                    getResources().getColor(R.color.picker_picsel_grid_mask_pressed));
                        } else {
                            item.setSelected(false);
                            MediaListHolder.selectPhotos.remove(item);
                            mask.setBackgroundResource(R.drawable.picker_sp_grid_mask);
                        }
                    }
                    if (null != mOnItemListener) {
                        mOnItemListener.onItemChecked();
                    }
                }
            });


            if (MediaListHolder.selectPhotos.contains(item)) {
                mask.setBackgroundColor(PhotoContext.getContext().getResources().getColor(R.color.picker_picsel_grid_mask_pressed));
            } else {
                mask.setBackgroundResource(R.drawable.picker_sp_grid_mask);
            }


            if (video) {
                tvTime.setText(CommonUtils.format(item.getDuration()));
                mask.setOnClickListener(v -> {
                    item.setSelected(true);
                    MediaListHolder.selectPhotos.add(item);

                    if (null != mOnItemListener) {
                        mOnItemListener.onItemClick(item);
                    }
                });
                return;
            }
            mask.setOnClickListener(v -> {
                if (preview) {
                    Intent intent = new Intent(itemView.getContext(), PicturePreviewActivity.class)
                            .putExtra(EXTRA_INDEX, position)
                            .putExtra(EXTRA_IS_GIF, item.isGif())
                            .putExtra(EXTRA_MAX, max);

                    PhotoContext.getContext().startActivity(intent);
                } else {
                    if (max == 1) {
                        if (null != mOnItemListener) {
                            mOnItemListener.onItemClick(item);
                        }
                    } else {
                        checkBox.toggle();
                        boolean isChecked = checkBox.isChecked();
                        if (getTotalSelectedNum() == max && isChecked) {
                            Toast.makeText(PhotoContext.getContext().getApplicationContext(),
                                    PhotoContext.getContext().getResources().getQuantityString(R.plurals.picker_picsel_selected_max, 1, max),
                                    Toast.LENGTH_SHORT).show();
                            checkBox.setChecked(false);
                        } else {
                            if (isChecked) {
                                item.setSelected(true);
                                MediaListHolder.selectPhotos.add(item);
                            } else {
                                item.setSelected(false);
                                MediaListHolder.selectPhotos.remove(item);
                            }
                        }
                        if (MediaListHolder.selectPhotos.contains(item)) {
                            mask.setBackgroundColor(PhotoContext.getContext().getResources().getColor(R.color.picker_picsel_grid_mask_pressed));
                        } else {
                            mask.setBackgroundResource(R.drawable.picker_sp_grid_mask);
                        }
                        if (null != mOnItemListener) {
                            mOnItemListener.onItemChecked();
                        }
                    }
                }


            });

        }
    }


    class CameraHolder extends RecyclerView.ViewHolder {

        private final ImageButton mMask;
        private final TextView mTvTitle;

        CameraHolder(@NonNull View itemView) {
            super(itemView);
            mMask = itemView.findViewById(R.id.picker_camera_mask);
            mTvTitle = itemView.findViewById(R.id.picker_take_picture);

        }

        void bind() {
            mTvTitle.setText(video ?
                    R.string.picker_picsel_record_video :
                    R.string.picker_picsel_take_picture);
            mMask.setOnClickListener(v -> {

                if (null != mOnItemListener) {
                    mOnItemListener.onCameraClick();
                }
            });

        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemHolder) {
            ((ItemHolder) holder).bind();
        } else if (holder instanceof CameraHolder) {
            ((CameraHolder) holder).bind();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (!camera) return 1;
        else {
            if (position == 0) return 0;
            else return 1;
        }
    }


    @Override
    public int getItemCount() {
        return camera ? mPhotos.size() + 1 : mPhotos.size();
    }
}
