package com.hqumath.demo.service

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class ServiceLifecycleOwner : LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)

    init {
        // 初始状态
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
    //override fun getLifecycle(): Lifecycle = lifecycleRegistry

    /** 服务启动时调用 */
    fun handleServiceStart() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    /** 服务销毁时调用 */
    fun handleServiceStop() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }
}
