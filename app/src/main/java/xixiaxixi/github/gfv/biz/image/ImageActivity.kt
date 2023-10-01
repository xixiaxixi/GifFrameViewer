package xixiaxixi.github.gfv.biz.image

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.MutableCreationExtras
import xixiaxixi.github.gfv.GFVApplication
import xixiaxixi.github.gfv.R
import xixiaxixi.github.gfv.base.getFitCenterSize
import xixiaxixi.github.gfv.biz.viewModel


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class ImageActivity : AppCompatActivity() {
    
    companion object {
        const val TAG = "ImageActivity"

        val thumbHeight: Int = GFVApplication.context.resources.getDimension(R.dimen.thumb_height).toInt()
    }

    private lateinit var mIvImage: ImageView
    private lateinit var mRvFrames: ImageRecyclerView
    private lateinit var mTvCurrentFrame: TextView
    private lateinit var mBtnPrevFrame: TextView
    private lateinit var mBtnNextFrame: TextView

    private var mThumbWidth: Int = 0

    private val mVM: ImageActivityVM by lazy {
        viewModel(
           MutableCreationExtras().apply {
               this[ImageActivityVM.optionKey] = ImageActivityVM.ImageActivityVMOption().apply {
                   maxThumbWidth = thumbHeight
                   maxThumbHeight = thumbHeight
               }
           }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        initView()
        initObserver()
        loadImage()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun initView() {
        // 设置返回键
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mTvCurrentFrame = findViewById(R.id.tv_current_frame)

        initRv()
        initIv()
        initBtn()

    }

    @SuppressLint("ClickableViewAccessibility") // Maybe Gif Viewer is useless for blind people...
    private fun initRv() {
        mRvFrames = findViewById(R.id.rv_frames)
        mRvFrames.setOnCurrentFrameChangedListener {
            mVM.setCurrentFrameIdx(it, FrameIdxChangeReason.UserScrollRv)
        }
        mRvFrames.setOnTouchListener { _, _ ->
            mVM.setIsGifPlaying(false)
            false
        }
    }

    private fun initIv() {
        mIvImage = findViewById(R.id.iv_image)
        mIvImage.setOnClickListener { toggleGifPause() }
    }

    private fun initBtn() {
        mBtnPrevFrame = findViewById(R.id.btn_prev_frame)
        mBtnNextFrame = findViewById(R.id.btn_next_frame)

        mBtnPrevFrame.setOnClickListener {
            mVM.setIsGifPlaying(false)
            mVM.setCurrentFrameIdx(
                mVM.currentFrameIdx.getValue()?.minus(1)?.coerceAtLeast(0) ?: 0,
                FrameIdxChangeReason.UserClickBtn
            )
        }
        mBtnNextFrame.setOnClickListener {
            mVM.setIsGifPlaying(false)
            mVM.setCurrentFrameIdx(
                mVM.currentFrameIdx.getValue()?.plus(1)?.coerceAtMost(mVM.gifThumbs.value?.lastIndex ?: 0) ?: 0,
                FrameIdxChangeReason.UserClickBtn
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initObserver() {
        mVM.gifThumbs.observe(this) { setGifThumbList(it) }

        mVM.currentDisplayImage.observe(this) { setDisplayImage(it.value) }

        mVM.currentFrameIdx.observeTwoWayBinding(this, FrameIdxChangeReason.UserScrollRv) {
            mRvFrames.scrollRvToFrame(it, it != 0)
        }

        mVM.currentFrameIdx.observe(this) {
            mTvCurrentFrame.text = "${it + 1}/${mVM.gifThumbs.value?.size ?: 0}"
        }
    }

    private fun loadImage() {
        intent.data?.let {
            mVM.setGifUri(it)
        }
    }

    private fun toggleGifPause() {
        mVM.setIsGifPlaying(mVM.isGifPlaying.value == false)
    }

    private fun setGifThumbList(thumbs: List<Bitmap>) {
        require(thumbs.isNotEmpty()) { "Got empty list" }
        val size = getFitCenterSize(thumbs[0].width, thumbs[0].height, thumbHeight, thumbHeight)
        mThumbWidth = size.first
        ThumbAdapter(mThumbWidth, size.second).apply {
            setData(thumbs)
            bindToImageRecyclerView(mRvFrames)
        }
    }

    private fun setDisplayImage(bitmap: Bitmap?) {
        mIvImage.setImageBitmap(bitmap)
    }

}