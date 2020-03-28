package com.comida.indice.syncyourlights.hue

import android.content.Context
import com.comida.indice.syncyourlights.helper.SharedPrefsController
import com.comida.indice.syncyourlights.interfaces.HueInterface
import com.philips.lighting.hue.sdk.*
import com.philips.lighting.model.PHBridge
import com.philips.lighting.model.PHHueError
import com.philips.lighting.model.PHHueParsingError
import java.lang.ref.WeakReference

class HueManager(context: Context) : PHSDKListener {

    private var listener: WeakReference<HueInterface>? = null
    private val sharedPref = SharedPrefsController(context)
    private lateinit var hueSdk: PHHueSDK
    private var lastSearchWasIp = false

    init {
        setUpHueSdk(context)
    }

    fun setUpListener(callback: HueInterface) {
        this.listener = WeakReference(callback)
    }

    fun removeListener() {
        this.listener = null
    }

    private fun setUpHueSdk(context: Context) {
        if (!this::hueSdk.isInitialized) {
            hueSdk = PHHueSDK.create()
            hueSdk.notificationManager.registerSDKListener(this)
            hueSdk.appName = "SyncYourLights"
            hueSdk.deviceName = android.os.Build.MODEL
        }

        if (!isHueSdkConnected(context)) {
            listener?.let {
                it.get()?.onHueConnect()
            }
        }

    }

    private fun isHueSdkConnected(context: Context): Boolean {

        val lastIp = sharedPref.getLastIp()
        val lastUserName = sharedPref.getLastUserName()

        if (lastIp.isNotBlank() && lastUserName.isNotBlank()) {
            val lastAccessPoint = PHAccessPoint()
            lastAccessPoint.ipAddress = lastIp
            lastAccessPoint.username = lastUserName

            if (!hueSdk.isAccessPointConnected(lastAccessPoint)) {
                hueSdk.connect(lastAccessPoint)
            } else {
                return true
            }

        } else {
            searchForBridge()
        }
        return false
    }

    private fun searchForBridge() {
        val searchManager = hueSdk.getSDKService(PHHueSDK.SEARCH_BRIDGE) as PHBridgeSearchManager
        searchManager.search(true, true)

    }

    override fun onBridgeConnected(phBridge: PHBridge?, userName: String?) {
        val connectedIp = phBridge?.resourceCache?.bridgeConfiguration?.ipAddress

        hueSdk.selectedBridge = phBridge
        hueSdk.enableHeartbeat(phBridge, PHHueSDK.HB_INTERVAL.toLong())
        hueSdk.lastHeartbeat.put(connectedIp, System.currentTimeMillis())

        sharedPref.saveLastIp(connectedIp!!)
        sharedPref.saveLastUserName(userName!!)

        listener?.let {
            it.get()?.onHueConnect()
        }
    }

    override fun onParsingErrors(p0: MutableList<PHHueParsingError>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onAccessPointsFound(accessPoints: MutableList<PHAccessPoint>?) {
        if (!accessPoints.isNullOrEmpty()) {
            hueSdk.accessPointsFound.clear()
            hueSdk.accessPointsFound.add(accessPoints[0])

            val ip = accessPoints[0].ipAddress
            val name = accessPoints[0].username

            val currentAccessPoint = PHAccessPoint()
            currentAccessPoint.apply {
                ipAddress = ip
                username = name
            }
            hueSdk.connect(currentAccessPoint)
        }
    }

    override fun onConnectionLost(p0: PHAccessPoint?) {
        if (hueSdk.disconnectedAccessPoint.contains(p0)) {
            hueSdk.disconnectedAccessPoint.add(p0)
        }
    }

    override fun onCacheUpdated(p0: MutableList<Int>?, p1: PHBridge?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onAuthenticationRequired(p0: PHAccessPoint?) {
        hueSdk.startPushlinkAuthentication(p0)
    }

    override fun onError(code: Int, message: String?) {
        when (code) {
            PHHueError.NO_CONNECTION, PHHueError.AUTHENTICATION_FAILED,
            PHMessageType.PUSHLINK_AUTHENTICATION_FAILED, PHHueError.BRIDGE_NOT_RESPONDING,
            PHMessageType.BRIDGE_NOT_FOUND -> {
                if (!lastSearchWasIp) {
                    val searchManager =
                        hueSdk.getSDKService(PHHueSDK.SEARCH_BRIDGE) as PHBridgeSearchManager
                    searchManager.search(false, false, true)
                    lastSearchWasIp = true
                } else {
                    //todo: no bridge found listener
                }
            }
        }
    }

    override fun onConnectionResumed(p0: PHBridge?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}