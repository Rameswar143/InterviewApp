package database;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DataAlarmReceiver extends BroadcastReceiver {
    public DataAlarmReceiver() {

    }
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            context.startService(new Intent(context, DataSynchService.class));
        } catch(Exception e) { }
    }
}
