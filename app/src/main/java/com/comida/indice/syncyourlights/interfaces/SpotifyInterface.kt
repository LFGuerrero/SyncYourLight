package com.comida.indice.syncyourlights.interfaces

import com.spotify.android.appremote.api.SpotifyAppRemote

interface SpotifyInterface {
    fun onSpotifyRemoteConnect(spotifyAppRemote: SpotifyAppRemote)
    fun onSpotifyRemoteError(message: String?)
    fun onNoSpotifyInstalled(message: String)
}