package com.comida.indice.syncyourlights.spotify

import android.content.Context
import android.util.Log
import com.comida.indice.syncyourlights.helper.Constants
import com.comida.indice.syncyourlights.helper.Constants.APP_TAG
import com.comida.indice.syncyourlights.helper.orElse
import com.comida.indice.syncyourlights.interfaces.SpotifyInterface
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import java.lang.ref.WeakReference

class SpotifyManager(callback: SpotifyInterface) {

    private var listener: WeakReference<SpotifyInterface>? = WeakReference(callback)

    fun setUpListener(callback: SpotifyInterface) {
        this.listener = WeakReference(callback)
    }

    fun removeListener() {
        this.listener = null
    }

    fun connectSpotifyRemote(context: Context) {
        SpotifyAppRemote.connect(context, setUpSpotifyConnectionParams(),
            object : Connector.ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    listener?.let {
                        it.get()?.onSpotifyRemoteConnect(spotifyAppRemote)
                    }.orElse {
                        Log.e(APP_TAG, "No listener found: ${javaClass.name}")
                    }
                }

                override fun onFailure(throwable: Throwable) {
                    listener?.let {
                        it.get()?.onSpotifyRemoteError(throwable.message)
                    }.orElse {
                        Log.e(APP_TAG, "No listener found: ${javaClass.name}")
                    }
                }
            })
    }

    private fun setUpSpotifyConnectionParams() =
        ConnectionParams.Builder(Constants.CLIENT_ID)
            .setRedirectUri(Constants.REDIRECT_URI)
            .showAuthView(true)
            .build()
}