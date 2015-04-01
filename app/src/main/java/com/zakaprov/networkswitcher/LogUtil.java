package com.zakaprov.networkswitcher;

import android.util.Log;

public class LogUtil
{
    private static final String TAG = "zakaprov.codes";
    private static final boolean IS_DEBUG = true;

    public static void d(String message)
    {
            if (IS_DEBUG)
                Log.d(TAG, message);
    }
}
