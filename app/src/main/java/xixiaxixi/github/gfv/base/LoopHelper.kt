package xixiaxixi.github.gfv.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class LoopHelper(private val mDelays: List<Long>, private val mCallback: (Int) -> Unit) {

    val control: Channel<LoopControl> = Channel()

    private val mMutex = Mutex(locked = true)
    private var mScope: CoroutineScope? = null

    private var mCurrentIdx = 0

    fun build(scope: CoroutineScope): LoopHelper {
        if (mScope != null) {
            throw IllegalStateException("LoopHelper has been built, please don't build it twice")
        }

        mScope = scope
        if (!scope.isActive) {
            throw IllegalStateException("CoroutineScope is not active")
        }

        scope.launch {
            while (true) {
                delay(mDelays[mCurrentIdx])
                mMutex.lock()
                mCallback.invoke(mCurrentIdx)
                mMutex.unlock()
                mCurrentIdx = (mCurrentIdx + 1) % mDelays.size
            }
        }

        scope.launch {
            while (true) {
                when (val control = control.receive()) {
                    is LoopControl.PlayState -> {
                        if (control.isPlaying) {
                            if (mMutex.isLocked) {
                                mMutex.unlock()
                            }
                        } else {
                            if (!mMutex.isLocked) {
                                mMutex.lock()
                            }
                        }
                    }
                    is LoopControl.SeekTo -> {
                        mCurrentIdx = control.frameIdx
                    }
                }
            }
        }

        return this
    }

    sealed interface LoopControl {
        data class PlayState(val isPlaying: Boolean) : LoopControl
        data class SeekTo(val frameIdx: Int) : LoopControl
    }
}