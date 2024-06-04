package com.praveenpayasi.simpleimageloaderdemo.util.networkhelper

import android.content.Context
import android.net.ConnectivityManager

class NetworkHelper(private val context: Context) {

    fun isNetworkConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork?.isConnected ?: false
    }
}