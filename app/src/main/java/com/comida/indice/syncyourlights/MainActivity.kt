package com.comida.indice.syncyourlights

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import androidx.palette.graphics.Palette
import com.comida.indice.syncyourlights.helper.Constants.APP_TAG
import com.comida.indice.syncyourlights.helper.orElse
import com.comida.indice.syncyourlights.interfaces.SpotifyInterface
import com.comida.indice.syncyourlights.spotify.SpotifyManager
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.Track

class MainActivity : AppCompatActivity(), View.OnClickListener, SpotifyInterface {

    private lateinit var spotifyRemote: SpotifyAppRemote
    private var isGettingImage = false
    private val spotifyManager: SpotifyManager by lazy { SpotifyManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setViews()
    }

    private fun setViews() {
        findViewById<Button>(R.id.btn_sync_spotify).setOnClickListener(this)
        findViewById<Button>(R.id.btn_sync_hue).setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        spotifyManager.setUpListener(this)
    }

    override fun onStop() {
        super.onStop()
        removeCallbacks()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeCallbacks()
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
                }.orElse {
                    Toast.makeText(this, "Unable to download cover image", LENGTH_LONG).show()
                    isGettingImage = false
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
                spotifyManager.connectSpotifyRemote(this)
            }
            R.id.btn_sync_hue -> {

            }
        }
    }

    private fun removeCallbacks() {
        spotifyManager.removeListener()
        if (this::spotifyRemote.isInitialized) {
            SpotifyAppRemote.disconnect(spotifyRemote)
        }
    }

    override fun onSpotifyRemoteConnect(spotifyAppRemote: SpotifyAppRemote) {
        spotifyRemote = spotifyAppRemote
        setUpSpotifyListener()
    }

    override fun onSpotifyRemoteError(message: String?) {
        Log.e(APP_TAG, message ?: "Error")
    }

    override fun onNoSpotifyInstalled(message: String) {
        Toast.makeText(this, "Please, install Spotify app  to continue.", LENGTH_LONG).show()
        //todo: Create an Activity for this error
    }
}
