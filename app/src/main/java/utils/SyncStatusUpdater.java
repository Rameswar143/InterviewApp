package utils;

import android.os.Handler;
import android.os.Looper;

import com.app.vst.christapp.UILoginActivity;

public class SyncStatusUpdater {
    private static Handler CallbackHandler;
    private static SyncStatusUpdater Instance;
    private SyncStatusUpdater() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        if(CallbackHandler != null) {
                            CallbackHandler.sendEmptyMessage(0);
                        }
                    }catch(Exception ex) {
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public static void start(Handler callback) {
        CallbackHandler = callback;
        if(SyncStatusUpdater.Instance == null) {
            SyncStatusUpdater.Instance = new SyncStatusUpdater();
        }
    }
}
