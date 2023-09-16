package xixiaxixi.github.gfv.base

import android.graphics.Bitmap

interface GifFrameLoader {

    val frameCount: Int

    fun getFrame(index: Int): Bitmap

    fun getFrameDelay(index: Int): Int

}