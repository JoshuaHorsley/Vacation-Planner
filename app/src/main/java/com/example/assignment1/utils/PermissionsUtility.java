package com.example.assignment1.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Utility class for handling runtime permissions
 */
public class PermissionsUtility {
    private static final String TAG = "PermissionsUtility";

    // Request codes for permission requests
    public static final int REQUEST_CONTACTS_PERMISSION = 100;
    public static final int REQUEST_STORAGE_PERMISSION = 101;
    public static final int REQUEST_LOCATION_PERMISSION = 102;

    /**
     * Check if the app has permission to read contacts
     * @param context Application context
     * @return true if permission is granted, false otherwise
     */
    public static boolean hasContactsPermission(Context context) {
        // For Android 6.0+ (API 23+), check for runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        // For pre-Marshmallow devices, permissions are granted at install time
        return true;
    }

    /**
     * Request contacts permission
     * @param activity Current activity
     */
    public static void requestContactsPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "Requesting READ_CONTACTS permission");
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_CONTACTS_PERMISSION);
        }
    }

    /**
     * Check if the app has permission to read/write external storage
     * @param context Application context
     * @return true if permission is granted, false otherwise
     */
    public static boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // For Android 13+, we need READ_MEDIA_* permissions instead
                return true; // Just return true as we're using scoped storage APIs
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // For Android 11+, use scoped storage instead
                return true;
            } else {
                // For Android 6.0 - 10
                return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;
            }
        }
        return true;
    }

    /**
     * Request storage permission
     * @param activity Current activity
     */
    public static void requestStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Log.d(TAG, "Requesting WRITE_EXTERNAL_STORAGE permission");
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        }
    }

    /**
     * Check if the app has permission to access fine and coarse location
     * @param context Application context
     * @return true if permission is granted, false otherwise
     */
    public static boolean hasLocationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * Request location permissions
     * @param activity Current activity
     */
    public static void requestLocationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "Requesting Location permissions");
            ActivityCompat.requestPermissions(activity,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    /**
     * Handle permission result
     * @param requestCode Request code from onRequestPermissionsResult
     * @param grantResults Results from onRequestPermissionsResult
     * @return true if permission is granted, false otherwise
     */
    public static boolean handlePermissionResult(int requestCode, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission granted for request code: " + requestCode);
            return true;
        } else {
            Log.d(TAG, "Permission denied for request code: " + requestCode);
            return false;
        }
    }
}