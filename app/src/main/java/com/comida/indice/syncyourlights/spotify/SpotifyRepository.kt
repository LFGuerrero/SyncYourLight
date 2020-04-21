package com.comida.indice.syncyourlights.spotify

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.comida.indice.syncyourlights.helper.Constants
import com.comida.indice.syncyourlights.helper.orElse
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.Track

class SpotifyRepository : ViewModel() {
    private lateinit var spotifyRemote: SpotifyAppRemote
    private var isGettingImage = false

    private val states: MutableLiveData<SpotifyRepStates> = MutableLiveData()

    fun connectSpotifyRemote(context: Context) :  Boolean {
        var isConnected = false
        SpotifyAppRemote.connect(context, setUpSpotifyConnectionParams(),
            object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    spotifyRemote = spotifyAppRemote
                    setUpSpotifyListener()
                    isConnected = true
                }

                override fun onFailure(throwable: Throwable) {
                }
            })
        return isConnected
    }

    private fun setUpSpotifyConnectionParams() =
        ConnectionParams.Builder(Constants.CLIENT_ID)
            .setRedirectUri(Constants.REDIRECT_URI)
            .showAuthView(true)
            .build()


    private fun setUpSpotifyListener() {
        spotifyRemote.playerApi.subscribeToPlayerState().setEventCallback { playerState ->
            run {
                val track: Track? = playerState.track
                track?.let {
                    if (!isGettingImage) {
                        isGettingImage = true
                        downloadAlbumCover(track.imageUri)
                    }
                }
            }
        }
    }

    private fun downloadAlbumCover(imgUri: ImageUri) {
        spotifyRemote.imagesApi.getImage(imgUri).setResultCallback { bitmap: Bitmap? ->
            bitmap?.let {
                //todo: send state to Viewmodel
            }.orElse {
                //todo: send state to Viewmodel
            }
            isGettingImage = false
        }
    }

}