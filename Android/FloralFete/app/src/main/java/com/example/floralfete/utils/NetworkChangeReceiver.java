package com.example.floralfete.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.floralfete.CheckoutActivity;
import com.example.floralfete.SingleProductViewActivity;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private static NoInternetDialog noInternetDialog;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NetworkChangeReceiver", "onReceive triggered");

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Activity activity = getActivityFromContext(context);

                if (activity instanceof CheckoutActivity || activity instanceof SingleProductViewActivity) {
                    if (!isConnected) {
                        Log.d("NetworkChangeReceiver", "Internet is OFF");

                        if (noInternetDialog == null || !noInternetDialog.isShowing()) {
                            noInternetDialog = new NoInternetDialog(activity);
                        }
                        if (!activity.isFinishing() && !activity.isDestroyed()) {
                            noInternetDialog.show();
                        }
                    } else {
                        Log.d("NetworkChangeReceiver", "Internet is ON");
                        if (noInternetDialog != null) {
                            noInternetDialog.dismiss();
                            noInternetDialog = null;
                        }
                    }
                }
            }
        });
    }

    private Activity getActivityFromContext(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return getActivityFromContext(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }
}
