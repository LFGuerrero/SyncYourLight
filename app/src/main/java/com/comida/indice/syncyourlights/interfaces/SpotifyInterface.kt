package com.comida.indice.syncyourlights.interfaces

import android.graphics.Bitmap
import com.spotify.android.appremote.api.SpotifyAppRemote

interface SpotifyInterface {
    fun onSpotifyRemoteConnect(spotifyAppRemote: SpotifyAppRemote)
    fun onSpotifyRemoteError(message: String?)
    fun onNoSpotifyInstalled(message: String)
    fun onImageDownload(bitmap: Bitmap)
    fun onImageError(message: String)
}