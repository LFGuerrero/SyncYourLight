package com.comida.indice.syncyourlights.spotify

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.comida.indice.syncyourlights.helper.Constants
import com.comida.indice.syncyourlights.helper.Constants.APP_TAG
import com.comida.indice.syncyourlights.helper.orElse
import com.comida.indice.syncyourlights.interfaces.SpotifyInterface
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.ImagesApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.ImageUri
import java.lang.ref.WeakReference
import java.net.URI

class SpotifyViewModel : ViewModel() {
    //todo: Create fun to detects is Spotify app is installed on device

    private var listener: WeakReference<SpotifyInterface>? = null

    private val states: MutableLiveData<SpotifyVMStates> = MutableLiveData()

    val state: LiveData<SpotifyVMStates> = states

    fun interpreter(interpreter: SpotifyInterpreter) {
        when (interpreter){
            is SpotifyInterpreter.ConnectSpotify -> {
                connectSpotifyRemote(context = interpreter.context)
            }
            is SpotifyInterpreter.DownloadAlbumCover -> {

            }
        }
    }

    fun setUpListener(callback: SpotifyInterface) {
        this.listener = WeakReference(callback)
    }

    fun removeListener() {
        this.listener = null
    }

    fun downloadAlbumCover(imagesApi: ImagesApi, imgUri: ImageUri) {
        imagesApi.getImage(imgUri).setResultCallback { bitmap: Bitmap? ->
            bitmap?.let {
                listener?.let { listener ->
                    listener.get()?.onImageDownload(bitmap)
                }.orElse {
                    throwListenerError("onImageDownload")
                }
            }.orElse {
                listener?.let {listener ->
                    listener.get()?.onImageError("Unable to download album cover")
                }
            }
        }
    }

    fun connectSpotifyRemote(context: Context) {
        SpotifyAppRemote.connect(context, setUpSpotifyConnectionParams(),
            object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    listener?.let {
                        it.get()?.onSpotifyRemoteConnect(spotifyAppRemote)
                    }.orElse {
                        throwListenerError("onConnect")
                    }
                }

                override fun onFailure(throwable: Throwable) {
                    listener?.let {
                        it.get()?.onSpotifyRemoteError(throwable.message)
                    }.orElse {
                        throwListenerError("onFailure")
                    }
                }
            })
    }

    private fun setUpSpotifyConnectionParams() =
        ConnectionParams.Builder(Constants.CLIENT_ID)
            .setRedirectUri(Constants.REDIRECT_URI)
            .showAuthView(true)
            .build()

    private fun throwListenerError(origin: String){
        Log.e(APP_TAG, "No $origin listener found: ${javaClass.name}")
    }
}

sealed class SpotifyInterpreter{
    data class ConnectSpotify(val context: Context) : SpotifyInterpreter()
    data class DownloadAlbumCover(val uri: URI) : SpotifyInterpreter()
}