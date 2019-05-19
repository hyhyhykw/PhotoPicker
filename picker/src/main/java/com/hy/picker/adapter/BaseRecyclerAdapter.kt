package com.hy.picker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.hy.picker.canLoadImage
import java.util.*


/**
 * Created time : 2018/4/3 11:05.
 *
 * @author HY
 */
abstract class BaseRecyclerAdapter<T, V : BaseRecyclerAdapter.BaseViewHolder> : RecyclerView.Adapter<V>() {

    protected val mData = ArrayList<T>()

    val data: List<T>
        get() = mData

    val isEmpty: Boolean
        get() = mData.isEmpty()

    protected inner class MyScrollListener : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (!recyclerView.context.canLoadImage()) return
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                Fresco.getImagePipeline().resume()
            } else {
                Fresco.getImagePipeline().pause()
            }
        }

    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.addOnScrollListener(MyScrollListener())
    }

    fun reset(data: List<T>) {
        mData.clear()
        mData.addAll(data)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): T {
        return mData[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): V {

        val view = LayoutInflater.from(parent.context).inflate(getLayoutByType(viewType), parent, false)
        val holder = createViewHolder(view, viewType)
        holder.viewType = viewType
        return holder
    }

    protected abstract fun createViewHolder(view: View, viewType: Int): V

    protected open fun getLayoutByType(viewType: Int): Int {
        return layout()
    }

    @LayoutRes
    protected abstract fun layout(): Int

    override fun onBindViewHolder(holder: V, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    fun clear() {
        mData.clear()
        notifyDataSetChanged()
    }

    abstract class BaseViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
        var viewType: Int = 0


        abstract fun bind()
    }

}
