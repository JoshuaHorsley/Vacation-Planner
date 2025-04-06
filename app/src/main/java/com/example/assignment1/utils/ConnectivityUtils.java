package com.example.assignment1.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Utility class for network connectivity operations.
 * Provides helper methods for checking network status.
 */
public class ConnectivityUtils {

    /**
     * Check if the device has an active network connection
     *
     * @param context Application context
     * @return true if connected, false otherwise
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    /**
     * Check if the device is connected to WiFi
     *
     * @param context Application context
     * @return true if connected to WiFi, false otherwise
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifiNetworkInfo != null && wifiNetworkInfo.isConnected();
        }
        return false;
    }

    /**
     * Check if the device is connected to mobile data
     *
     * @param context Application context
     * @return true if connected to mobile data, false otherwise
     */
    public static boolean isMobileDataConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo mobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            return mobileNetworkInfo != null && mobileNetworkInfo.isConnected();
        }
        return false;
    }

    /**
     * Get a description of the current network connection type
     *
     * @param context Application context
     * @return String describing the connection type ("WiFi", "Mobile Data", or "Not Connected")
     */
    public static String getConnectionType(Context context) {
        if (isWifiConnected(context)) {
            return "WiFi";
        } else if (isMobileDataConnected(context)) {
            return "Mobile Data";
        } else {
            return "Not Connected";
        }
    }
}