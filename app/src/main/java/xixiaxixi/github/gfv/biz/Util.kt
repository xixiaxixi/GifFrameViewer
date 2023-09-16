package xixiaxixi.github.gfv.biz

import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras

val Number.dp: Float get() = this.toFloat() * Resources.getSystem().displayMetrics.density

inline fun <reified T : AndroidViewModel> AppCompatActivity.viewModel(creationExtras: MutableCreationExtras = MutableCreationExtras(CreationExtras.Empty)): T {
    creationExtras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] = application
    return ViewModelProvider(this.viewModelStore, viewModelFactory, creationExtras).get()
}