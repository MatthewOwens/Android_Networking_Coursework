package com.a1400971example.android_networking_coursework;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Matthew Owens on 14/03/16.
 */
public class NetworkHelper
{
    private static final String service = Context.CONNECTIVITY_SERVICE;
    private ConnectivityManager connectivityManager;
    private boolean isConnected = false;
    private boolean isWifi = false;

    public NetworkHelper(Context context)
    {
        // Creating our connectivity manager instance
        connectivityManager = (ConnectivityManager)context.getSystemService(service);

        // Determining the state of our connection
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        isConnected = (netInfo != null && netInfo.isConnected());

        if(netInfo != null)
            isWifi = netInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }
}
