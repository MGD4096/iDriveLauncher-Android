package net.mateusz_grzymala.bmwidrive.Launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;

import static android.content.Context.AUDIO_SERVICE;

public class BooTReciever extends BroadcastReceiver {
    public final static String PREFERENCE_NAME = "com.example.mypackage.preference";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences("App Settings",Context.MODE_PRIVATE);
            if(sharedPref.getBoolean("PlayOnStart",false)) {
                AudioManager mAudioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                Log.d("Reciever","Start Audio");
                KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mAudioManager.dispatchMediaKeyEvent(event);
                }
            }
        }
    }
}
