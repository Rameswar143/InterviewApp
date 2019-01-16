package database;

import android.content.Context;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import utils.Common;

public class AttendanceManager {
    private static Context AppContext = null;
    private static String FileName = "ATTENDANCE_INFO";
    private static ArrayList<AbsentInfo> AbsentList = null;
    public static void init(Context context) {
        if(AppContext == null) {
            AppContext = context;
            AbsentList = (ArrayList<AbsentInfo>) Common.readObjectFromDevice(AppContext, FileName);
            if (AbsentList == null) {
                AbsentList = new ArrayList<AbsentInfo>();
            }
        }
    }
    public static boolean isAbsent(Long appNo) {
        try {
            if(AppContext != null) {
                if(AbsentList == null) {
                    AbsentList = new ArrayList<AbsentInfo>();
                }

                DateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
                String date = formatter.format(new Date());
                for (AbsentInfo info : AbsentList) {
                    if (info.Date.compareTo(date) == 0 && Long.toString(info.AppNo).compareTo(Long.toString(appNo)) == 0) {
                        return true;
                    }
                }
            }
        }
        catch(Exception ex) { }
        return false;
    }
    public static void setAbsent(Long appNo) {
        if(AppContext != null) {
            if (isAbsent(appNo) == false) {
                if(AbsentList == null) {
                    AbsentList = new ArrayList<AbsentInfo>();
                }

                DateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
                String date = formatter.format(new Date());
                AbsentInfo info = new AbsentInfo();
                info.Date = date;
                info.AppNo = appNo;
                AbsentList.add(info);
                Common.writeObjectToDevice(AppContext, FileName, AbsentList);
            }
        }
    }
    public static void setPresent(Long appNo) {
        if(AppContext != null) {
            if(AbsentList == null) {
                AbsentList = new ArrayList<AbsentInfo>();
            }

            DateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
            String date = formatter.format(new Date());
            AbsentInfo exist = null;
            for (AbsentInfo info : AbsentList) {
                if (info.Date.compareTo(date) == 0 && Long.toString(info.AppNo).compareTo(Long.toString(appNo)) == 0) {
                    exist = info;
                    break;
                }
            }
            if (exist != null) {
                AbsentList.remove(exist);
                Common.writeObjectToDevice(AppContext, FileName, AbsentList);
            }
        }
    }

    public static class AbsentInfo implements Serializable {
        public String Date;
        public Long AppNo;
    }
}
