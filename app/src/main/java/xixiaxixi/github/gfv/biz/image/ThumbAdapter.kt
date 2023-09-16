package xixiaxixi.github.gfv.biz.image

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import xixiaxixi.github.gfv.base.getFitCenterSize

class ThumbAdapter(
    private var mMaxWidth: Int = 0,
    private var mMaxHeight: Int = 0
) : Adapter<ThumbAdapter.VH>() {

    private var mThumbs: List<Bitmap>? = null

    private var mThumbWidth = 0
    private var mThumbHeight = 0

    private lateinit var mRv: ImageRecyclerView

    fun bindToImageRecyclerView(rv: ImageRecyclerView) {
        mRv = rv
        rv.adapter = this
    }

    /**
     * @throws [IllegalArgumentException]
     */
    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: List<Bitmap>) {
        require(list.isNotEmpty()) { "Got empty list" }
        mThumbs = list
        updateThumbSize()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mThumbs?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(ImageView(parent.context).apply {
        layoutParams = ViewGroup.LayoutParams(mThumbWidth, mThumbHeight)
    })

    override fun onBindViewHolder(holder: VH, position: Int) {
        val thumb = mThumbs?.getOrNull(position) ?: return
        (holder.itemView as? ImageView)?.let {
            it.setImageBitmap(thumb)
            it.setOnClickListener {
                mRv.scrollRvToFrame(position)
            }
        }
    }

    private fun updateThumbSize() {
        mThumbs?.getOrNull(0)?.let {
            val size = getFitCenterSize(it.width, it.height, mMaxWidth, mMaxHeight)
            mThumbWidth = size.first
            mThumbHeight = size.second
        }

        require(mThumbWidth > 0 && mThumbHeight > 0) { "Got thumb width or thumb height equals 0" }
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView)

}