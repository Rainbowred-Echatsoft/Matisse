package com.echat.matisse.internal.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

/**
 * @author JoongWon Baik
 */
public class Platform {


    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    static int getTargetSdkVersionCode(Context context) {
        return context.getApplicationInfo().targetSdkVersion;
    }

    static boolean checkPermissionInAndroidManifest(Context context, String permissionName) {
        try {
            PackageInfo packageInfo          = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[]    requestedPermissions = packageInfo.requestedPermissions;
            if (requestedPermissions != null && requestedPermissions.length > 0) {
                for (String requestedPermission : requestedPermissions) {
                    if (requestedPermission.equals(permissionName)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean initialized     = false;
    private static boolean readMediaImages = false;
    private static boolean readMediaVideo  = false;

    public static boolean isPartialMediaAccess(Context context) {
        if (getTargetSdkVersionCode(context) < 34) {
            return false;
        } else {
            if (!initialized) {
                readMediaImages = checkPermissionInAndroidManifest(context, Manifest.permission.READ_MEDIA_IMAGES);
                readMediaVideo  = checkPermissionInAndroidManifest(context, Manifest.permission.READ_MEDIA_VIDEO);
                initialized     = true;
            }

            boolean partialAllow = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED;
            boolean imageAllow   = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
            boolean videoAllow   = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;

            // media permission correctly configured
            if (readMediaImages && readMediaVideo) {
                return partialAllow && !imageAllow && !videoAllow;
            } else if (readMediaImages) {
                return partialAllow && !imageAllow;
            } else if (readMediaVideo) {
                return partialAllow && !videoAllow;
            } else {
                return false;
            }
        }
    }
}
