package xixiaxixi.github.gfv.image

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import xixiaxixi.github.gfv.R

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class ImageActivity : AppCompatActivity() {

    private lateinit var mIvImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_image)

        initView()

        loadImage()
    }

    private fun initView() {
        // 设置返回键
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mIvImage = findViewById(R.id.iv_image)

        mIvImage.setOnClickListener { toggleGifPause() }
    }

    private fun loadImage() {
        Glide.with(this).asGif().load(intent.data).into(mIvImage)
    }

    private fun toggleGifPause() {
        (mIvImage.drawable as? GifDrawable)?.let {
            if (it.isRunning) {
                it.stop()
            } else {
                it.start()
            }
        }

    }

}