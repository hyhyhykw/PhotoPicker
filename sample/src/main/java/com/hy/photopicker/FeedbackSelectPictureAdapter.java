package com.hy.photopicker;
//
//                              _ooOoo_
//                             o8888888o
//                             88" . "88
//                             (| -_- |)
//                             O\  =  /O
//                          ____/`---'\____
//                        .'  \\|     |//  `.
//                       /  \\|||  :  |||//  \
//                      /  _||||| -:- |||||-  \
//                      |   | \\\  -  /// |   |
//                      | \_|  ''\---/''  |   |
//                      \  .-\__  `-`  ___/-. /
//                    ___`. .'  /--.--\  `. . __
//                 ."" '<  `.___\_<|>_/___.'  >'"".
//                | | :  `- \`.;`\ _ /`;.`/ - ` : | |
//                \  \ `-.   \_ __\ /__ _/   .-` /  /
//           ======`-.____`-.___\_____/___.-`____.-'======
//                              `=---='
//          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//                    佛祖保佑             永无BUG
//
//              佛曰:
//                    写字楼里写字间，写字间中程序员；
//                    程序人员写程序，又将程序换酒钱；
//                    酒醒只在屏前坐，酒醉还来屏下眠；
//                    酒醉酒醒日复日，屏前屏下年复年；
//                    但愿老死电脑间，不愿鞠躬老板前；
//                    奔驰宝马贵者趣，公交自行程序员；
//                    别人笑我太疯癫，我笑自己命太贱；
//                    但见满街漂亮妹，哪个归得程序员？

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.hy.picker.model.Photo;

import java.io.File;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;


/**
 * Created time : 2018/5/30 15:28.
 *
 * @author HY
 */
public class FeedbackSelectPictureAdapter extends BaseRecyclerAdapter<Photo, BaseRecyclerAdapter.BaseViewHolder> {

    public static final int TYPE_PHOTO = 1;
    public static final int TYPE_ADD = 2;
    public static final int TYPE_DELETE = 3;

    @IntDef({TYPE_PHOTO, TYPE_ADD, TYPE_DELETE})
    public @interface Type {

    }

    @NonNull
    @Override
    protected BaseRecyclerAdapter.BaseViewHolder createViewHolder(View view, int viewType) {
        if (viewType == TYPE_ADD) {
            return new AddHolder(view);
        }
        return new PicHolder(view);
    }

    @Override
    public int getItemCount() {
        return Math.min(mData.size() + 1, 9);
    }

    @Override
    public int getItemViewType(int position) {
        int size = mData.size();
        if (position == size) {
            return TYPE_ADD;
        } else {
            return TYPE_PHOTO;
        }
    }

    @Override
    protected int getLayoutByType(int viewType) {
        if (viewType == TYPE_ADD) {
            return R.layout.item_rcy_add;
        }
        return super.getLayoutByType(viewType);
    }

    @Override
    protected int layout() {
        return R.layout.item_feedback_photo_select;
    }

    class PicHolder extends BaseRecyclerAdapter.BaseViewHolder {

        final SimpleDraweeView mIvImage;
        final ImageView mIvDeleteImage;

        PicHolder(View itemView) {
            super(itemView);
            mIvImage = itemView.findViewById(R.id.iv_image);
            mIvDeleteImage = itemView.findViewById(R.id.iv_delete_image);
        }

        @Override
        public void bind() {
            Photo item = getItem(getAdapterPosition());
//            String path = item.getThumbnailSmallPath();
//            if (TextUtils.isEmpty(path)) {
//                path = item.getThumbnailBigPath();
//            }
//
//            if (TextUtils.isEmpty(path)) {
//                path = item.getOriginalPath();
//            }

//            Glide.with(mContext)
//                    .load(item.getUri())
//                    .thumbnail(0.4f)
//                    .into(mIvImage);

            mIvImage.setImageURI(Uri.fromFile(new File(item.getUri())));

            mIvImage.setOnClickListener(v -> mOnItemClickListener.onClick(getAdapterPosition(), TYPE_PHOTO));
            mIvDeleteImage.setOnClickListener(v -> mOnItemClickListener.onClick(getAdapterPosition(), TYPE_DELETE));
        }
    }

    class AddHolder extends BaseRecyclerAdapter.BaseViewHolder {
        final ImageView mIvAdd;

        AddHolder(View itemView) {
            super(itemView);
            mIvAdd = itemView.findViewById(R.id.iv_add);
        }

        @Override
        public void bind() {
            mIvAdd.setOnClickListener(v -> mOnItemClickListener.onClick(getAdapterPosition(), TYPE_ADD));
        }
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(int position, @Type int type);
    }
}
