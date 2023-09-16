package xixiaxixi.github.gfv.biz

import androidx.lifecycle.ViewModelProvider
import xixiaxixi.github.gfv.biz.image.ImageActivityVM

val viewModelFactory = ViewModelProvider.Factory.from(
    ImageActivityVM.initializer
)