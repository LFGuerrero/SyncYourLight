package com.comida.indice.syncyourlights.interfaces

interface HueInterface {
    fun onHueConnect()
    fun onHueError(message: String)
}