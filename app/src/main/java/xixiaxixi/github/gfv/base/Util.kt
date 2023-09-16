package xixiaxixi.github.gfv.base

import android.graphics.Bitmap
import kotlin.math.ln
import kotlin.math.pow


fun Int.beautifyByteCount(): String {
    val unit = 1024
    if (this < unit) return "$this B"
    val exp = (ln(this.toDouble()) / ln(unit.toDouble())).toInt()
    val pre = "KMGTPE"[exp - 1]
    return String.format("%.1f %sB", this / unit.toDouble().pow(exp.toDouble()), pre)
}

fun Bitmap.scaleTo(width: Int, height: Int, recycle: Boolean = false): Bitmap = Bitmap.createScaledBitmap(this, width, height, true).also {
    if (recycle && it != this) {
        this.recycle()
    }
}

fun getFitCenterSize(srcWidth: Int, srcHeight: Int, maxWidth: Int, maxHeight: Int): Pair<Int, Int> {
    val srcRatio = srcWidth.toFloat() / srcHeight.toFloat()
    val maxRatio = maxWidth.toFloat() / maxHeight.toFloat()
    return if (srcRatio > maxRatio) {
        Pair(maxWidth, (maxWidth / srcRatio).toInt())
    } else {
        Pair((maxHeight * srcRatio).toInt(), maxHeight)
    }
}

class Wrapper<T>(val value: T?)
