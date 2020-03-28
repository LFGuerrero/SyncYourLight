package com.comida.indice.syncyourlights.hue

import com.comida.indice.syncyourlights.interfaces.HueInterface
import java.lang.ref.WeakReference

class HueManager {

    private var listener: WeakReference<HueInterface>? = null

    fun setUpListener(callback: HueInterface) {
        this.listener = WeakReference(callback)
    }

    fun removeListener() {
        this.listener = null
    }
}