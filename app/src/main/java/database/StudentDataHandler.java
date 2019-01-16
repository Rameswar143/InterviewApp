package database;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;

import domain.ExStudentDataArgs;
import domain.ExStudentDtlInfoList;
import domain.ExStudentEduInfoList;
import utils.Common;

public class StudentDataHandler {
    private static boolean _isStudentsLoaded = false;
    private static boolean _isQualificationsLoaded = false;
    private static Handler _callback;
    private static Context _context;
    private static ExStudentDataArgs _args;

    public static final String STUDENT_DTLS = "STUDENT_DTLS";
    public static final String EDUCATION_DTLS = "EDUCATION_DTLS";

    public static void loadDataFromFiles(Context context, Handler callback) {
        StudentDataHandler._context = context;
        StudentDataHandler._callback = callback;

        StudentDataHandler._isStudentsLoaded = false;
        StudentDataHandler._isQualificationsLoaded = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                String json = Common.getExternalData(STUDENT_DTLS);
                ExStudentDtlInfoList students = new Gson().fromJson(json, ExStudentDtlInfoList.class);
                DBInfo.createInstance(StudentDataHandler._context).saveStudents(students);

                json = Common.getExternalData(EDUCATION_DTLS);
                ExStudentEduInfoList data = new Gson().fromJson(json, ExStudentEduInfoList.class);
                DBInfo.createInstance(StudentDataHandler._context).saveQualifications(data);

                StudentDataHandler._isStudentsLoaded = true;
                StudentDataHandler._isQualificationsLoaded = true;
                if(StudentDataHandler._callback != null) {
                    StudentDataHandler._callback.sendMessage(StudentDataHandler._callback.obtainMessage(0));
                }
            }
        }).start();
    }

    public static void loadDataFromServer(Context context, Handler callback, ExStudentDataArgs args) {
        StudentDataHandler._context = context;
        StudentDataHandler._args = args;
        StudentDataHandler._callback = callback;

        StudentDataHandler._isStudentsLoaded = false;
        StudentDataHandler._isQualificationsLoaded = false;

        StudentDataHandler.loadStudents();
        StudentDataHandler.loadQualifications();
    }

    private static void invokeCallback() {
        if(StudentDataHandler._isStudentsLoaded == true && StudentDataHandler._isQualificationsLoaded == true) {
            if(StudentDataHandler._callback != null) {
                StudentDataHandler._callback.sendMessage(StudentDataHandler._callback.obtainMessage(0));
            }
        }
    }
    private static void loadStudents() {
        String json = new Gson().toJson(StudentDataHandler._args);
        DeviceDataManager.get(StudentDataHandler._context, STUDENT_DTLS, json, loadStudentsResultHandler);
    }
    private static void loadQualifications() {
        String json = new Gson().toJson(StudentDataHandler._args);
        DeviceDataManager.get(StudentDataHandler._context, EDUCATION_DTLS, json, loadQualificationsResultHandler);
    }
    private static Handler loadStudentsResultHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg != null && msg.obj != null && msg.obj instanceof String) {
                final String json = (String) msg.obj;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (json != null && json.trim().length() > 0) {
                            Common.setExternalData(STUDENT_DTLS, json);
                            ExStudentDtlInfoList data = new Gson().fromJson(json, ExStudentDtlInfoList.class);
                            DBInfo.createInstance(StudentDataHandler._context).saveStudents(data);
                        }
                        StudentDataHandler._isStudentsLoaded = true;
                        StudentDataHandler.invokeCallback();
                    }
                }).start();
            }
            return true;
        }
    });
    private static Handler loadQualificationsResultHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(final Message msg) {
            if(msg != null && msg.obj != null && msg.obj instanceof String) {
                final String json = (String) msg.obj;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(json != null && json.trim().length() > 0) {
                            Common.setExternalData(EDUCATION_DTLS, json);
                            ExStudentEduInfoList data = new Gson().fromJson(json, ExStudentEduInfoList.class);
                            DBInfo.createInstance(StudentDataHandler._context).saveQualifications(data);
                        }
                        StudentDataHandler._isQualificationsLoaded = true;
                        StudentDataHandler.invokeCallback();
                    }
                }).start();
            }
            return true;
        }
    });
}
