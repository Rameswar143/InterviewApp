package database;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.SystemClock;

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.List;

import domain.ExBaseObject;
import domain.ExScore;
import domain.ExScoreInfo;
import utils.Common;
import utils.JsonUtils;

public class  DataSynchService extends IntentService {
    public DataSynchService() {
        super(DataSynchService.class.getName());
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        saveData();
    }
    private void saveData() {
        final DataSynchService pointer = this;
        try {
            if (Common.isNetworkConnected(pointer)) {
                try {
                    final DBInfo dbinfo = DBInfo.createInstance(pointer);
                    List<ExScore> scorecards = dbinfo.readScoreCards();
                    if(scorecards != null && scorecards.size() > 0) {
                        for (int i = 0; i < scorecards.size(); i++) {
                            final ExScore scorecard = scorecards.get(i);
                            if (scorecard.Updated == false) {
                                if (Common.isNetworkConnected(pointer)) {
                                    String json = new Gson().toJson(scorecard);
                                    if (json != null && json.trim().length() > 0) {
                                        String resultJson = DeviceDataManager.set(pointer, json);
                                        if (resultJson != null && resultJson.trim().length() > 0) {
                                            ExBaseObject result = new Gson().fromJson(resultJson, ExBaseObject.class);
                                            if (result != null && result.ID > 0) {
                                                dbinfo.setUpdateStatus(scorecard, true);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch(Exception ex) { }
        finally { this.setAlarm(); }
    }
    private void setAlarm() {
        try {
            Intent intent = new Intent(getApplicationContext(), DataAlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
            AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(getApplicationContext().ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 200, pendingIntent);
        }
        catch(Exception ex) { }
    }
}
