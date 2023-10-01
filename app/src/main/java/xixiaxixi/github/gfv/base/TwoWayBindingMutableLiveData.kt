package xixiaxixi.github.gfv.base

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer


/**
 * The design of livedata of Android is fucking poor.
 * [T] is type of wrapped livedata, [R] is super type of updating reason
 */
class TwoWayBindingMutableLiveData<T, R> : MutableLiveData<T>, TwoWayBindingLiveData<T, R> {
    constructor() : super()
    constructor(value: T) : super(value)

    private var mLastIgnoreReason: R? = null
    private val mTwoWayBindingObservers = mutableListOf<TwoWayBindingObserverRecord<T, R>>()

    override fun observeTwoWayBinding(owner: LifecycleOwner, observerIgnore: R, observer: Observer<in T>) {
        mTwoWayBindingObservers.add(TwoWayBindingObserverRecord(owner, observer, observerIgnore))
        super.observe(owner, observer)
    }

    override fun observeTwoWayBindingForever(observerIgnore: R, observer: Observer<in T>) {
        mTwoWayBindingObservers.add(TwoWayBindingObserverRecord(null, observer, observerIgnore))
        super.observeForever(observer)
    }

    @MainThread
    override fun setValueWithoutDispatching(value: T, observerIgnore: R) {
        removeAllObserversIfNeeded(observerIgnore)
        super.setValue(value)
    }

    override fun setValue(value: T) {
        restoreAllObserversIfNeeded()
        super.setValue(value)
    }

    override fun removeObserver(observer: Observer<in T>) {
        mTwoWayBindingObservers.removeAll { it.observer == observer }
        super.removeObserver(observer)
    }

    override fun removeObservers(owner: LifecycleOwner) {
        mTwoWayBindingObservers.removeAll { it.lifecycleOwner == owner }
        super.removeObservers(owner)
    }

    private fun removeAllObserversIfNeeded(observerIgnore: R) {
        if (mLastIgnoreReason != observerIgnore) {
            restoreAllObserversIfNeeded()
            mLastIgnoreReason = observerIgnore
            mTwoWayBindingObservers.filter { it.observerIgnore == observerIgnore }.forEach {
                super.removeObserver(it.observer)
            }
        }
    }

    private fun restoreAllObserversIfNeeded() {
        if (mLastIgnoreReason != null) {
            mTwoWayBindingObservers.filter { it.observerIgnore == mLastIgnoreReason }.forEach {
                val (owner, observer) = it
                if (owner == null) {
                    super.observeForever(observer)
                } else {
                    super.observe(owner, observer)
                }
            }
            mLastIgnoreReason = null
        }
    }

}

interface TwoWayBindingLiveData<T, R> {
    fun getValue(): T?
    fun observe(owner: LifecycleOwner, observer: Observer<in T>)
    fun observeTwoWayBinding(owner: LifecycleOwner, observerIgnore: R, observer: Observer<in T>)
    fun observeTwoWayBindingForever(observerIgnore: R, observer: Observer<in T>)
    fun setValue(value: T)
    fun setValueWithoutDispatching(value: T, observerIgnore: R)
}

private data class TwoWayBindingObserverRecord<T, R>(
    val lifecycleOwner: LifecycleOwner?,
    val observer: Observer<in T>,
    val observerIgnore: R
)