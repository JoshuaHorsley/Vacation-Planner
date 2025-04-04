package com.example.assignment1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

/**
 * Broadcast Receiver that monitors network connectivity changes.
 * This is particularly useful for weather features that require internet access.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkChangeReceiver";

    // Callback interface to notify activities about network state changes
    public interface NetworkChangeListener {
        void onNetworkConnectionChanged(boolean isConnected);
    }

    private NetworkChangeListener networkChangeListener;

    public NetworkChangeReceiver() {
        // Default constructor required for BroadcastReceiver
    }

    public NetworkChangeReceiver(NetworkChangeListener listener) {
        this.networkChangeListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Network connectivity change detected");

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean isConnected = isNetworkConnected(context);

            // Display appropriate toast message based on connectivity
            if (isConnected) {
                Log.i(TAG, "Network is connected");
                Toast.makeText(context, "Network is available", Toast.LENGTH_SHORT).show();
            } else {
                Log.w(TAG, "Network is disconnected");
                Toast.makeText(context, "No network connection", Toast.LENGTH_SHORT).show();
            }

            // Notify the listener (if set)
            if (networkChangeListener != null) {
                networkChangeListener.onNetworkConnectionChanged(isConnected);
            }
        }
    }

    /**
     * Check if the network is currently connected
     * @param context The application context
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
}
