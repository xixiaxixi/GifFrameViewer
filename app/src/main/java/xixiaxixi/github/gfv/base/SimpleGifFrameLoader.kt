package xixiaxixi.github.gfv.base

import android.graphics.Bitmap
import android.util.Log
import com.bumptech.glide.gifdecoder.GifDecoder
import com.bumptech.glide.gifdecoder.GifHeaderParser
import com.bumptech.glide.gifdecoder.StandardGifDecoder
import xixiaxixi.github.gfv.biz.image.ImageActivity
import java.nio.ByteBuffer

class SimpleGifFrameLoader(
    byteArray: ByteArray,
    private val mWidth: Int = 0,
    private val mHeight: Int = 0,
) : GifFrameLoader {

    companion object {
        const val TAG = "SimpleGifFrameLoader"
    }

    private val frameInfos = mutableListOf<FrameInfo>()
    private val frames = mutableListOf<Bitmap>()

    init {
        val byteBuffer = ByteBuffer.wrap(byteArray)

        val gifHeader = GifHeaderParser().also {
            it.setData(byteBuffer)
        }.parseHeader()

        val gifDecoder = StandardGifDecoder(SimpleBitmapProvider(), gifHeader, byteBuffer)

        for (i in 0 until gifDecoder.frameCount) {
            gifDecoder.advance()

            gifDecoder.nextFrame?.let {
                val frameInfo = FrameInfo(gifDecoder.getDelay(i))
                frameInfos.add(frameInfo)
                frames.add(it.takeIf { mWidth == 0 } ?: it.scaleTo(mWidth, mHeight, recycle = true))
            }
        }

        if (frames.size <= 0) {
            Log.d(TAG, "SimpleGifFrameLoader: Load frames failed.")
        } else {
            val totalMemory = frames.sumOf { it.allocationByteCount }
            Log.d(TAG, "SimpleGifFrameLoader: Load frames finished. \n" +
                    "\tframeCount = ${frameInfos.size} \n" +
                    "\tframes.size = ${frames[0].width}x${frames[0].height} \n" +
                    "\tmemoryUsage = ${totalMemory.beautifyByteCount()}")
        }
    }

    override val frameCount: Int
        get() = frames.size

    override fun getFrame(index: Int): Bitmap = frames[index]

    override fun getFrameDelay(index: Int): Int = frameInfos[index].delay

    fun getFrameThumbnail(index: Int, width: Int, height: Int): Bitmap =
        frameInfos[index].thumbnail?.takeIf { it.width == width && it.height == height }
            ?: frames[index].scaleTo(width, height, recycle = false).also {
                frameInfos[index].thumbnail?.recycle()
                frameInfos[index].thumbnail = it
            }


    class SimpleBitmapProvider : GifDecoder.BitmapProvider {

        override fun obtain(width: Int, height: Int, config: Bitmap.Config): Bitmap {
            Log.d(ImageActivity.TAG, "obtain: width = $width, height = $height, config = $config")
            return Bitmap.createBitmap(width, height, config)
        }

        override fun release(bitmap: Bitmap) {
            Log.d(ImageActivity.TAG, "release: ")
            bitmap.recycle()
        }

        override fun release(bytes: ByteArray) {
            // no op
        }

        override fun release(array: IntArray) {
            // no op
        }

        override fun obtainByteArray(size: Int): ByteArray {
            return ByteArray(size)
        }

        override fun obtainIntArray(size: Int): IntArray {
            return IntArray(size)
        }
    }
}