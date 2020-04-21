package com.comida.indice.syncyourlights.spotify

import android.graphics.Bitmap
import com.spotify.android.appremote.api.SpotifyAppRemote

sealed class SpotifyVMStates {
    data class SpotifyConnected(val spotifyAppRemote: SpotifyAppRemote) : SpotifyVMStates()
    data class SpotifyConnectionFail(val error: String) : SpotifyVMStates()
    object SpotifyDownloadedAlbum : SpotifyVMStates()
    object SpotifyNotDownloadedAlbum : SpotifyVMStates()
}

sealed class SpotifyRepStates {
    data class onImageDownload(val bitmap: Bitmap) : SpotifyRepStates()
    data class onImageError(val error: String) : SpotifyRepStates()
}