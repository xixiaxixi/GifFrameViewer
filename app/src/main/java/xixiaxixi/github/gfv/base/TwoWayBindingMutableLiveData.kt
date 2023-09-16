package xixiaxixi.github.gfv.base

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer


/**
 * The design of livedata of Android is fucking poor.
 */
class TwoWayBindingMutableLiveData<T> : MutableLiveData<T>, TwoWayBindingLiveData<T> {
    constructor() : super()
    constructor(value: T) : super(value)

    private var mIsObserversRemoved = false
    private val mTwoWayBindingObservers = mutableListOf<Pair<LifecycleOwner?, Observer<in T>>>()

    override fun observeTwoWayBinding(owner: LifecycleOwner, observer: Observer<in T>) {
        mTwoWayBindingObservers.add(owner to observer)
        if (!mIsObserversRemoved) {
            super.observe(owner, observer)
        }
    }

    override fun observeTwoWayBindingForever(observer: Observer<in T>) {
        mTwoWayBindingObservers.add(null to observer)
        if (!mIsObserversRemoved) {
            super.observeForever(observer)
        }
    }

    @MainThread
    override fun setValueWithoutDispatching(value: T) {
        removeAllObserversIfNeeded()
        super.setValue(value)
    }

    override fun setValue(value: T) {
        restoreAllObserversIfNeeded()
        super.setValue(value)
    }

    override fun removeObserver(observer: Observer<in T>) {
        mTwoWayBindingObservers.removeAll { it.second == observer }
        super.removeObserver(observer)
    }

    override fun removeObservers(owner: LifecycleOwner) {
        mTwoWayBindingObservers.removeAll { it.first == owner }
        super.removeObservers(owner)
    }

    private fun removeAllObserversIfNeeded() {
        if (!mIsObserversRemoved) {
            mIsObserversRemoved = true
            mTwoWayBindingObservers.forEach { super.removeObserver(it.second) }
        }
    }

    private fun restoreAllObserversIfNeeded() {
        if (mIsObserversRemoved) {
            mIsObserversRemoved = false
            mTwoWayBindingObservers.forEach {
                val (owner, observer) = it
                if (owner == null) {
                    super.observeForever(observer)
                } else {
                    super.observe(owner, observer)
                }
            }
        }
    }

}

interface TwoWayBindingLiveData<T> {
    fun getValue(): T?
    fun observe(owner: LifecycleOwner, observer: Observer<in T>)
    fun observeTwoWayBinding(owner: LifecycleOwner, observer: Observer<in T>)
    fun observeTwoWayBindingForever(observer: Observer<in T>)
    fun setValue(value: T)
    fun setValueWithoutDispatching(value: T)
}