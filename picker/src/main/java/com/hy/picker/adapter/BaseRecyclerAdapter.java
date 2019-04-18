package com.hy.picker.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


/**
 * Created time : 2018/4/3 11:05.
 *
 * @author HY
 */
@SuppressWarnings({"WeakerAccess", "unused"})
abstract class BaseRecyclerAdapter<T, V extends BaseRecyclerAdapter.BaseViewHolder> extends RecyclerView.Adapter<V> {

    protected final List<T> mData = new ArrayList<>();

    protected Context mContext;


    public List<T> getData() {
        return mData;
    }

    protected final void toActivity(@NonNull Class<? extends Activity> clazz) {
        toActivity(clazz, null);
    }

    protected final void toActivity(@NonNull Class<? extends Activity> clazz, @Nullable Bundle bundle) {
        toActivity(clazz, bundle, null);
    }

    protected class MyScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState==RecyclerView.SCROLL_STATE_IDLE){
                Fresco.getImagePipeline().resume();
            }else{
                Fresco.getImagePipeline().pause();
            }
        }

    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(new MyScrollListener());
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
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
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
        }

        public abstract void bind();
    }

}
