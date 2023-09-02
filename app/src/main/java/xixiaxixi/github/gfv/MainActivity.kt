package xixiaxixi.github.gfv

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import xixiaxixi.github.gfv.image.ImageActivity


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"

        val ACTIVITY_REQUEST_PICK_GIF = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType("image/gif"))
    }

    private lateinit var mLoadImage: View

    private lateinit var mPickImageLauncher: ActivityResultLauncher<PickVisualMediaRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initRegistry()

        initView()
    }

    private fun initRegistry() {
        mPickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            it?.let { onImagePicked(it) }
        }
    }

    private fun initView() {
        mLoadImage = findViewById(R.id.tv_load_image)

        mLoadImage.setOnClickListener { pickGifFromGallery() }
    }

    private fun pickGifFromGallery() {
        mPickImageLauncher.launch(ACTIVITY_REQUEST_PICK_GIF)
    }

    private fun onImagePicked(uri: Uri) {
        Log.i(TAG, "onImagePicked: $uri")
        startImageActivity(uri)
    }

    private fun startImageActivity(uri: Uri) {
        startActivity(Intent(this, ImageActivity::class.java).apply {
            data = uri
        })
    }

}