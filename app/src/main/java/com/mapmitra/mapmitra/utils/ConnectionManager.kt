package com.mapmitra.mapmitra.utils

import android.content.Context
import android.net.ConnectivityManager

class ConnectionManager {
    fun checkConnectivity(context:Context):Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetworkInfo

        if (activeNetwork?.isConnected != null){
            return activeNetwork.isConnected
        }else{
            return false
        }
    }
}