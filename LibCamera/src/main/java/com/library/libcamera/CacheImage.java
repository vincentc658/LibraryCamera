package com.library.libcamera;

import android.content.Context;
import android.content.SharedPreferences;

public class CacheImage {
    static final String PREFERENCES_NAME = "lib_image";

    private static SharedPreferences getSharedPreference(Context context){
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
    public static void setCacheImage(Context context, String image){
        SharedPreferences.Editor editor = getSharedPreference(context).edit();
        editor.putString("image_capture", image);
        editor.apply();
    }
    public static String getCacheImage(Context context){
        return getSharedPreference(context).getString("image_capture","");
    }
}
