package com.comida.indice.syncyourlights.spotify

sealed class SpotifyVMStates {
    object SpotifyConnected : SpotifyVMStates()
    object SpotifyDisconnected : SpotifyVMStates()
    object SpotifyDownloadedAlbum : SpotifyVMStates()
    object SpotifyNotDownloadedAlbum : SpotifyVMStates()
}