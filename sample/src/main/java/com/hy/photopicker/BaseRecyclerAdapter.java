package com.hy.photopicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * Created time : 2018/4/3 11:05.
 *
 * @author HY
 */
public abstract class BaseRecyclerAdapter<T, V extends BaseRecyclerAdapter.BaseViewHolder> extends RecyclerView.Adapter<V> {

    protected final List<T> mData = new ArrayList<>();

    protected Context mContext;

    protected static final Interpolator mInterpolator = new LinearInterpolator();
    private int mLastPosition = -1;
    protected long mDuration = 300L;

    public List<T> getData() {
        return mData;
    }

    protected final void toActivity(@NonNull Class<? extends Activity> clazz) {
        toActivity(clazz, null);
    }

    protected final void toActivity(@NonNull Class<? extends Activity> clazz, @Nullable Bundle bundle) {
        toActivity(clazz, bundle, null);
    }

    protected final void toActivity(@NonNull Class<? extends Activity> clazz, @Nullable Bundle bundle, @Nullable Uri data) {
        Intent intent = new Intent(mContext, clazz);
        if (null != bundle) {
            intent.putExtra("bundle", bundle);
        }

        if (null != data) {
            intent.setData(data);
        }

        mContext.startActivity(intent);
    }



    public void reset(@NonNull List<T> data) {
        mLastPosition = -1;
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void reset(@NonNull Collection<T> data) {
        mLastPosition = -1;
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void reset(@NonNull T[] data) {
        reset(Arrays.asList(data));
    }

    public void addData(@NonNull List<T> data) {
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void addData(@NonNull Collection<T> data) {
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void addData(@NonNull T[] data) {
        addData(Arrays.asList(data));
    }

    public void addItem(T t) {
        mData.add(t);
        notifyItemInserted(mData.size() - 1);
    }

    public void changeItem(int position, T t) {
        mData.set(position, t);
        notifyItemChanged(position);
    }

    public void addItem(T t, int position) {
        mData.add(position, t);
        notifyItemInserted(position);
    }

    public void deleteItem(int position) {
        mData.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mData.size() - position);
    }

    public T getFirst() {
        return mData.get(0);
    }

    public T getLast() {
        return mData.get(mData.size() - 1);
    }

    public T getItem(int position) {
        return mData.get(position);
    }

    @NonNull
    @Override
    public V onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (null == mContext) mContext = parent.getContext();

        View view = LayoutInflater.from(mContext).inflate(getLayoutByType(viewType), parent, false);
        V holder = createViewHolder(view, viewType);
        holder.setViewType(viewType);
        return holder;
    }

    @NonNull
    protected abstract V createViewHolder(View view, int viewType);

    protected int getLayoutByType(int viewType) {
        return layout();
    }

    @LayoutRes
    protected abstract int layout();

    @Override
    public void onBindViewHolder(@NonNull V holder, int position) {
        holder.bind();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
//
//    protected BaseAnimation mSelectAnimation = new AlphaInAnimation();
//
//    /**
//     * 添加动画
//     *
//     * @param holder ViewHolder
//     */
//    public void addAnimation(V holder) {
//        if (isOpenAnimation()) {
//            if (holder.getLayoutPosition() > mLastPosition) {
//                for (Animator animator : mSelectAnimation.getAnimators(holder.itemView)) {
//                    startAnim(animator);
//                }
//                mLastPosition = holder.getLayoutPosition();
//            }
//        }
//    }

//    protected boolean isOpenAnimation() {
//        return false;
//    }

//    /**
//     * 开启动画
//     *
//     * @param animator 动画
//     */
//    private void startAnim(Animator animator) {
//        animator.setDuration(mDuration).start();
//        animator.setInterpolator(mInterpolator);
//    }

    public boolean isEmpty() {
        return mData.isEmpty();
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    public abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        private int viewType;
        protected final View itemView;

        public int getViewType() {
            return viewType;
        }

        public void setViewType(int viewType) {
            this.viewType = viewType;
        }

        public BaseViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
//            mUnbinders.add(ButterKnife.bind(this, itemView));
        }

        public abstract void bind();
    }

//    public final void unbind() {
//        for (Unbinder unbinder : mUnbinders) {
//            unbinder.unbind();
//        }
//    }
}
