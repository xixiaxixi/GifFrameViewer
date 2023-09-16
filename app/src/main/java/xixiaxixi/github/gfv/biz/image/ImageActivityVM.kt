package xixiaxixi.github.gfv.biz.image

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.ViewModelInitializer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import xixiaxixi.github.gfv.base.LoopHelper
import xixiaxixi.github.gfv.base.SimpleGifFrameLoader
import xixiaxixi.github.gfv.base.TwoWayBindingLiveData
import xixiaxixi.github.gfv.base.TwoWayBindingMutableLiveData
import xixiaxixi.github.gfv.base.Wrapper

class ImageActivityVM(
    private val mOption: ImageActivityVMOption,
    application: Application
) : AndroidViewModel(application) {

    init {
        require(mOption.maxThumbWidth > 0 && mOption.maxThumbHeight > 0) { "thumbWidth and thumbHeight must be greater than 0" }
    }

    private var mLoader: SimpleGifFrameLoader? = null
    private var mLoopControl: Channel<LoopHelper.LoopControl>? = null

    private val _isGifPlaying = MutableLiveData(false)
    val isGifPlaying: LiveData<Boolean> get() = _isGifPlaying

    private val _gifThumbs = MutableLiveData<List<Bitmap>>()
    val gifThumbs: LiveData<List<Bitmap>> get() = _gifThumbs

    private val _currentFrameIdx = TwoWayBindingMutableLiveData(0)
    val currentFrameIdx : TwoWayBindingLiveData<Int>
        get() = _currentFrameIdx

    // Use a wrapper to avoid redraw on bitmap. This is for future
    private val _currentDisplayImage = MutableLiveData<Wrapper<Bitmap>>()
    val currentDisplayImage: LiveData<Wrapper<Bitmap>> get() = _currentDisplayImage

    fun setIsGifPlaying(isPlaying: Boolean) {
        _isGifPlaying.value = isPlaying
        viewModelScope.launch {
            mLoopControl?.send(LoopHelper.LoopControl.PlayState(isPlaying))
        }
    }

    fun setGifUri(uri: Uri) {
        getApplication<Application>().contentResolver.openInputStream(uri).use {
            val byteArray = it?.readBytes() ?: return@use
            mLoader = SimpleGifFrameLoader(byteArray)
        }

        mLoader?.takeIf { it.frameCount > 0 }?.let {
            val thumbs = mutableListOf<Bitmap>()
            for (i in 0 until it.frameCount) {
                thumbs.add(it.getFrameThumbnail(i, mOption.maxThumbWidth, mOption.maxThumbHeight))
            }
            _gifThumbs.value = thumbs

            // Prepare loop gif
            val frameDelays = (0 until it.frameCount).map { i -> it.getFrameDelay(i).toLong() }
            mLoopControl = LoopHelper(frameDelays) { i ->
                setCurrentFrameIdx(i)
            }.build(viewModelScope).control
        }
    }

    fun setCurrentFrameIdx(frameIdxInCenter: Int, isUserScroll: Boolean = false) {
        if (isUserScroll) {
            _currentFrameIdx.setValueWithoutDispatching(frameIdxInCenter)
            viewModelScope.launch {
                mLoopControl?.send(LoopHelper.LoopControl.SeekTo(frameIdxInCenter))
            }
        } else {
            // is from auto-loop
            _currentFrameIdx.setValue(frameIdxInCenter)
        }
        mLoader?.getFrame(frameIdxInCenter)?.let {
            _currentDisplayImage.value = Wrapper(it)
        }
    }

    class ImageActivityVMOption {
        var maxThumbWidth: Int = 0
        var maxThumbHeight: Int = 0
    }

    companion object ImageActivityVMInitializer {
        val optionKey = object : CreationExtras.Key<ImageActivityVMOption> {}

        val initializer = ViewModelInitializer(ImageActivityVM::class.java) {
            val option = this[optionKey]
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
            require(option != null && app != null) { "option and application must not be null" }
            ImageActivityVM(option, app)
        }
    }
}