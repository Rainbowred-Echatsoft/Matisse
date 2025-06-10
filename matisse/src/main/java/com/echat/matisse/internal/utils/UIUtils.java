/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.echat.matisse.internal.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TypedValue;
import android.view.DisplayCutout;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

public class UIUtils {

    public static int spanCount(Context context, int gridExpectedSize) {
        int   screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        float expected    = (float) screenWidth / (float) gridExpectedSize;
        int   spanCount   = Math.round(expected);
        if (spanCount == 0) {
            spanCount = 1;
        }
        return spanCount;
    }


    // 适配Android 15 EdgeToEdge
    public static void supportAndroid15EdgeToEdge(Context context, Window window, View topView, View bottomView) {
        if (context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.VANILLA_ICE_CREAM
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;

            window.getDecorView().setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsets onApplyWindowInsets(@NonNull View view, @NonNull WindowInsets windowInsets) {
                    Insets systemBars = windowInsets.getInsets(WindowInsets.Type.systemBars());
                    int    top        = systemBars.top;
                    int    bottom     = systemBars.bottom;

                    DisplayCutout cutout = windowInsets.getDisplayCutout();
                    if (cutout != null && cutout.getBoundingRects() != null && !cutout.getBoundingRects().isEmpty()) {
                        if (cutout.getSafeInsetTop() > top) top = cutout.getSafeInsetTop();
                    }

                    if (topView != null) {
                        topView.setPadding(topView.getPaddingLeft(), top, topView.getPaddingRight(), topView.getPaddingBottom());
                    }
                    if (bottomView != null) {
                        bottomView.setPadding(bottomView.getPaddingLeft(), bottomView.getPaddingTop(), bottomView.getPaddingRight(), bottomView.getPaddingBottom() + bottom);
                    }

                    return windowInsets;
                }
            });
        }
    }

    public static void supportAndroid15LightMode(Activity activity) {
        if (activity.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.VANILLA_ICE_CREAM
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            TypedValue typedValue = new TypedValue();
            activity.getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
            int     colorPrimary = typedValue.data;
            boolean lightColor   = UIUtils.isLightColor(colorPrimary);
            Log.i("Matisse", "onCreate: lightColor = " + lightColor);
            UIUtils.setStatusBarLightMode(activity.getWindow(), lightColor);
        }
    }

    public static boolean isLightColor(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness < 0.5;
    }

    public static void setStatusBarLightMode(@NonNull final Window window,
                                             final boolean isLightMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = window.getDecorView();
            int  vis       = decorView.getSystemUiVisibility();
            if (isLightMode) {
                vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decorView.setSystemUiVisibility(vis);
        }
    }

}
