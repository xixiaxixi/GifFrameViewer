package xixiaxixi.github.gfv.biz.image

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ImageRecyclerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : RecyclerView(context, attrs, defStyle) {

    private var mRvTotalScroll = 0
    private var mIsAutoScrolling = false
    private var mOnCurrentFrameChangedListener: ((Int) -> Unit)? = null

    private lateinit var mTouchListener: OnTouchListener

    init {
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        addItemDecoration(ThumbRvDecorator())
        addOnScrollListener(InnerOnScrollListener())
        initOnTouchListener()
    }

    @SuppressLint("ClickableViewAccessibility") // Maybe Gif Viewer is useless for blind people...
    private fun initOnTouchListener() {
        mTouchListener = OnTouchListener { _, _ ->
            mIsAutoScrolling = false
            false
        }
        setOnTouchListener(mTouchListener)
    }

    override fun smoothScrollBy(dx: Int, dy: Int) {
        mIsAutoScrolling = true
        super.smoothScrollBy(dx, dy)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setOnTouchListener(l: OnTouchListener?) {
        super.setOnTouchListener { v, event ->
            var ret = l?.onTouch(v, event) ?: false
            ret = mTouchListener.onTouch(v, event) || ret
            ret
        }
    }

    fun setOnCurrentFrameChangedListener(listener: (Int) -> Unit) {
        mOnCurrentFrameChangedListener = listener
    }

    private val childWidth: Int
        get() = (layoutManager as LinearLayoutManager).getChildAt(0)?.width ?: 0

    fun scrollRvToFrame(frameIdx: Int, smooth: Boolean = true) {
        if (frameIdx != indexOfMiddle) {
            mOnCurrentFrameChangedListener?.invoke(frameIdx)
        }
        if (smooth) {
            smoothScrollBy(frameIdx * childWidth - mRvTotalScroll, 0)
        } else {
            scrollBy(frameIdx * childWidth - mRvTotalScroll, 0)
        }
    }

    inner class InnerOnScrollListener : OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == SCROLL_STATE_IDLE) {
                // snap to center
                val frameIdxInCenter = indexOfMiddle
                scrollRvToFrame(frameIdxInCenter)

                mIsAutoScrolling = false
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            mRvTotalScroll += dx
            val frameIdxInCenter = indexOfMiddle

            if (!mIsAutoScrolling) {
                mOnCurrentFrameChangedListener?.invoke(frameIdxInCenter)
            }
        }

    }

    private val indexOfMiddle: Int
        get() = if (childWidth > 0) {
            (mRvTotalScroll + childWidth / 2) / childWidth
        } else {
            0
        }

}