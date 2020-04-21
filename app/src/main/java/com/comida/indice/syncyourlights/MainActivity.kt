package com.comida.indice.syncyourlights

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import com.comida.indice.syncyourlights.spotify.SpotifyInterpreter
import com.comida.indice.syncyourlights.spotify.SpotifyVMStates
import com.comida.indice.syncyourlights.spotify.SpotifyViewModel
import com.philips.lighting.hue.sdk.PHAccessPoint
import com.philips.lighting.hue.sdk.PHHueSDK
import com.philips.lighting.hue.sdk.PHSDKListener
import com.philips.lighting.model.PHBridge
import com.philips.lighting.model.PHHueParsingError
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Track

class MainActivity : AppCompatActivity(), View.OnClickListener, PHSDKListener {

    private lateinit var spotifyRemote: SpotifyAppRemote
    private var isGettingImage = false
    private val spotifyManager: SpotifyViewModel by lazy { SpotifyViewModel() }
    private val hueSdk: PHHueSDK by lazy { PHHueSDK.getInstance() }

    private val spotifyViewModel: SpotifyViewModel by lazy {
        ViewModelProvider(this).get(
            SpotifyViewModel::class.java
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setViews()

        spotifyViewModel.state.observe(this, Observer { vmStates ->
            vmStates?.let {
                when (it) {
                    is SpotifyVMStates.SpotifyConnected -> {
                        spotifyRemote = it.spotifyAppRemote
                        setUpSpotifyListener()
                    }
                    is SpotifyVMStates.SpotifyConnectionFail -> {
                        Toast.makeText(this, it.error, LENGTH_LONG).show()
                        //todo: stop application
                    }
                    is SpotifyVMStates.SpotifyDownloadedAlbum -> {
                        //todo: get album color
                    }
                    is SpotifyVMStates.SpotifyNotDownloadedAlbum -> {
                        //todo: set default color
                    }
                }
            }
        })
    }

    private fun setViews() {
        findViewById<Button>(R.id.btn_sync_spotify).setOnClickListener(this)
        findViewById<Button>(R.id.btn_sync_hue).setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        hueSdk.notificationManager.registerSDKListener(this)
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
                    if (!isGettingImage) {
                        isGettingImage = true
                        spotifyManager.downloadAlbumCover(spotifyRemote.imagesApi, track.imageUri)
                    }
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
                spotifyViewModel.interpreter(SpotifyInterpreter.ConnectSpotify(this))
            }
            R.id.btn_sync_hue -> {

            }
        }
    }

    private fun removeCallbacks() {
        hueSdk.notificationManager.unregisterSDKListener(this)
        if (this::spotifyRemote.isInitialized) {
            SpotifyAppRemote.disconnect(spotifyRemote)
        }
    }

//    override fun onSpotifyRemoteConnect(spotifyAppRemote: SpotifyAppRemote) {
//        spotifyRemote = spotifyAppRemote
//        setUpSpotifyListener()
//    }
//
//    override fun onSpotifyRemoteError(message: String?) {
//        Log.e(APP_TAG, message ?: "Error")
//    }
//
//    override fun onNoSpotifyInstalled(message: String) {
//        Toast.makeText(this, "Please, install Spotify app  to continue.", LENGTH_LONG).show()
//        //todo: Create an Activity for this error
//    }
//
//    override fun onImageDownload(bitmap: Bitmap) {
//        findViewById<ImageView>(R.id.iv_album_cover).setImageBitmap(bitmap)
//        getPredominantAlbumColor(bitmap)
//    }
//
//    override fun onImageError(message: String) {
//        Toast.makeText(this, message, LENGTH_LONG).show()
//        isGettingImage = false
//    }

    override fun onBridgeConnected(p0: PHBridge?, p1: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onParsingErrors(p0: MutableList<PHHueParsingError>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onAccessPointsFound(p0: MutableList<PHAccessPoint>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionLost(p0: PHAccessPoint?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCacheUpdated(p0: MutableList<Int>?, p1: PHBridge?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onAuthenticationRequired(p0: PHAccessPoint?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onError(p0: Int, p1: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionResumed(p0: PHBridge?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
