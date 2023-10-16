package com.krewie.tellmewhen

import android.util.Log
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint

class MapsEventReceiverImpl : MapEventsReceiver, MapListener {

    override fun longPressHelper(p: GeoPoint): Boolean {
        Log.e("LongPress", "Lat:"+p.latitude+", Long:"+p.longitude)
        return false
    }

    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
        Log.e("SingleTap", "Lat:"+p.latitude+", Long:"+p.longitude)
        return false
    }

    override fun onScroll(event: ScrollEvent): Boolean {
        Log.e("onScroll", "onCreate:la ${event?.source?.getMapCenter()?.latitude}")
        Log.e("onScroll", "onCreate:lo ${event?.source?.getMapCenter()?.longitude}")
        return true
    }

    override fun onZoom(event: ZoomEvent): Boolean {
        Log.e("onZoom", "onZoom zoom level: ${event?.zoomLevel}   source:  ${event?.source}")
        return false;
    }
}