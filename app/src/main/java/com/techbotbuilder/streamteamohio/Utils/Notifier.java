package com.techbotbuilder.streamteamohio.Utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.StringRes;

public class Notifier {
    private Notifier(){}

    public static void notify(Context context, CharSequence s){
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
    public static void notify(Context context, @StringRes int s){
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
    public static void longNotify(Context context, CharSequence s){
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
    }
    public static void longNotify(Context context, @StringRes int s){
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
    }
}
