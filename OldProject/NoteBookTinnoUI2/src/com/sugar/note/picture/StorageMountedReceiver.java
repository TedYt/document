package com.sugar.note.picture;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Created by user on 8/8/14.
 */
public class StorageMountedReceiver extends BroadcastReceiver {

    private MountedChangeListener mListener;

    public StorageMountedReceiver(MountedChangeListener listener) {
        mListener = listener;
    }

    public interface MountedChangeListener {
        public void onMountedChanged(String state);
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (mListener != null) {
            mListener.onMountedChanged(action);
        }
    }

    public void registerMountReceiver(Context context) {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addDataScheme("file");
        context.registerReceiver(this, intentFilter);
        /*if (OptionsUtils.isMtkSDSwapSurpported()) {
            IntentFilter intentFilterSDSwap = new IntentFilter();
            intentFilterSDSwap.addAction(INTENT_SD_SWAP);
            context.registerReceiver(receiver, intentFilterSDSwap);
        }*/
    }

    public void unRegisterMountReceiver(Context context) {
        context.unregisterReceiver(this);
    }

}
