package com.comida.indice.syncyourlights.spotify

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.comida.indice.syncyourlights.helper.Constants
import com.comida.indice.syncyourlights.helper.orElse
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.ImagesApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.ImageUri
import java.net.URI

class SpotifyViewModel : ViewModel() {
    //todo: Create fun to detects is Spotify app is installed on device

    private val spotifyRepository = SpotifyRepository()

    private val states: MutableLiveData<SpotifyVMStates> = MutableLiveData()

    val state: LiveData<SpotifyVMStates> = states

    fun interpreter(interpreter: SpotifyInterpreter) {
        when (interpreter) {
            is SpotifyInterpreter.ConnectSpotify -> {
                val connected = spotifyRepository.connectSpotifyRemote(interpreter.context)
            }
            is SpotifyInterpreter.DownloadAlbumCover -> {

            }
        }
    }

    fun downloadAlbumCover(imagesApi: ImagesApi, imgUri: ImageUri) {
        imagesApi.getImage(imgUri).setResultCallback { bitmap: Bitmap? ->
            bitmap?.let {
                //                listener?.let { listener ->
//                    listener.get()?.onImageDownload(bitmap)
//                }
            }.orElse {
                //                listener?.let { listener ->
//                    listener.get()?.onImageError("Unable to download album cover")
//                }
            }
        }
    }

    //todo: move to SpotifyRepository
    private fun connectSpotifyRemote(context: Context) {
        SpotifyAppRemote.connect(context, setUpSpotifyConnectionParams(),
            object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    states.postValue(SpotifyVMStates.SpotifyConnected(spotifyAppRemote))
                }

                override fun onFailure(throwable: Throwable) {
                    states.postValue(SpotifyVMStates.SpotifyConnectionFail(throwable.message ?: ""))
                }
            })
    }

    private fun setUpSpotifyConnectionParams() =
        ConnectionParams.Builder(Constants.CLIENT_ID)
            .setRedirectUri(Constants.REDIRECT_URI)
            .showAuthView(true)
            .build()
}

sealed class SpotifyInterpreter {
    data class ConnectSpotify(val context: Context) : SpotifyInterpreter()
    data class DownloadAlbumCover(val uri: URI) : SpotifyInterpreter()
}