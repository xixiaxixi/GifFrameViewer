package xixiaxixi.github.gfv.biz.image

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ThumbRvDecorator : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildLayoutPosition(view)
        when (position) {
            0 -> {
                outRect.left = parent.width / 2 - view.layoutParams.width / 2
            }
            state.itemCount - 1 -> {
                outRect.right = parent.width / 2 - view.layoutParams.width / 2
            }
        }
    }
}