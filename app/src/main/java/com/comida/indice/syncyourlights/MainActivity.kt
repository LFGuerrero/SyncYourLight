package com.comida.indice.syncyourlights

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
import com.comida.indice.syncyourlights.helper.Constants.CLIENT_ID
import com.comida.indice.syncyourlights.helper.Constants.REDIRECT_URI
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector.ConnectionListener
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.Track

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var spotifyRemote: SpotifyAppRemote
    private var isGettingImage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setViews()
    }

    private fun setViews() {
        findViewById<Button>(R.id.btn_sync_spotify).setOnClickListener(this)
        findViewById<Button>(R.id.btn_sync_hue).setOnClickListener(this)
    }

    override fun onStop() {
        super.onStop()
        removeCallbacks()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeCallbacks()
    }

    private fun setUpSpotifyConnectionParams() =
        ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

    private fun connectSpotifyRemote() {
        SpotifyAppRemote.connect(this, setUpSpotifyConnectionParams(),
            object : ConnectionListener {
                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                    spotifyRemote = spotifyAppRemote
                    setUpSpotifyListener()
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e("MainActivity", throwable.message, throwable)
                }
            })
    }

    private fun setUpSpotifyListener() {
        spotifyRemote.playerApi.subscribeToPlayerState().setEventCallback { playerState ->
            run {
                val track: Track? = playerState.track
                track?.let {
                    getAlbumImage(track.imageUri)
                }
            }
        }
    }

    private fun getAlbumImage(imgUri: ImageUri) {
        if (!isGettingImage) {
            isGettingImage = true
            spotifyRemote.imagesApi.getImage(imgUri).setResultCallback { bitmap: Bitmap? ->
                bitmap?.let {
                    findViewById<ImageView>(R.id.iv_album_cover).setImageBitmap(it)
                    getPredominantAlbumColor(it)
                }
            }
        }
    }

    private fun getPredominantAlbumColor(bitmap: Bitmap) {
        Palette.from(bitmap).generate { palette ->
            palette?.let {
                val dominantSwatch = it.dominantSwatch
                dominantSwatch?.let { swatch ->
                    findViewById<TextView>(R.id.tv_app_title).setTextColor(swatch.rgb)
                }
            }
        }
        isGettingImage = false
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btn_sync_spotify -> {
                connectSpotifyRemote()
            }
            R.id.btn_sync_hue -> {

            }
        }
    }

    private fun removeCallbacks() {
        if (this::spotifyRemote.isInitialized) {
            SpotifyAppRemote.disconnect(spotifyRemote)
        }
    }
}
