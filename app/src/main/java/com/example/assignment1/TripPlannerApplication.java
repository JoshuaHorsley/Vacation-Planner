package com.example.assignment1;

import android.app.Application;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

import com.example.assignment1.receivers.NetworkChangeReceiver;
import com.example.assignment1.utils.ConnectivityUtils;

/**
 * Application class to handle app-wide functionality.
 * This class manages global broadcast receivers and other app-wide components.
 */
public class TripPlannerApplication extends Application {
    private static final String TAG = "TripPlannerApplication";

    private NetworkChangeReceiver globalNetworkReceiver;
    private static TripPlannerApplication instance;

    // Network status flag that can be checked from anywhere in the app
    private boolean isNetworkConnected = false;

    public static TripPlannerApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "Application created");

        // Initialize the global network receiver
        setupGlobalNetworkReceiver();

        // Initialize network status
        isNetworkConnected = ConnectivityUtils.isNetworkConnected(this);
    }

    private void setupGlobalNetworkReceiver() {
        globalNetworkReceiver = new NetworkChangeReceiver(new NetworkChangeReceiver.NetworkChangeListener() {
            @Override
            public void onNetworkConnectionChanged(boolean isConnected) {
                // Update the global network status
                isNetworkConnected = isConnected;
                Log.d(TAG, "Global network status changed: " + (isConnected ? "Connected" : "Disconnected"));
            }
        });

        // Register the global receiver
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(globalNetworkReceiver, intentFilter);

        Log.d(TAG, "Global network receiver registered");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        // Unregister the global receiver
        try {
            unregisterReceiver(globalNetworkReceiver);
            Log.d(TAG, "Global network receiver unregistered");
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error unregistering network receiver: " + e.getMessage());
        }
    }

    /**
     * Get the current network connection status
     * @return true if the device is connected to the internet, false otherwise
     */
    public boolean isNetworkConnected() {
        return isNetworkConnected;
    }

    /**
     * Get connection type as a user-friendly string
     * @return a string indicating the type of connection
     */
    public String getConnectionInfo() {
        return ConnectivityUtils.getConnectionType(this);
    }
}