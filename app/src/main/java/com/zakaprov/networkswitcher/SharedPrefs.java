package com.zakaprov.networkswitcher;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefs
{
    private static final String SHARED_PREF_NAME = "network_switcher_preferences";
    public static final String RECEIVER_ENABLED_KEY = "isReceiverEnabled";

    public static boolean getSharedPref(Context context, String key)
    {
        SharedPreferences sharedPrefs = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        boolean result = false;

            switch (key)
            {
                case RECEIVER_ENABLED_KEY:
                    result = sharedPrefs.getBoolean(RECEIVER_ENABLED_KEY, true);
                    break;
            }

        return result;
    }

    public static void setSharedPref(Context context, String key, boolean value)
    {
        SharedPreferences sharedPrefs = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

            switch (key)
            {
                case RECEIVER_ENABLED_KEY:
                    editor.putBoolean(RECEIVER_ENABLED_KEY, value);
                    break;
            }

        editor.commit();
    }

    public static void clearSharedPrefs(Context context)
    {
        SharedPreferences sharedPrefs = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.clear();
        editor.commit();
    }
}
