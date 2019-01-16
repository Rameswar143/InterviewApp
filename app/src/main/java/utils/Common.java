package utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.app.vst.christapp.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class Common {
    public final static String URL_IP = "https://kp.christuniversity.in";
//    public final static String URL_IP = "http://10.5.13.51";
    //public final static String URL_IP = "http://172.16.10.2:8080";
    public final static String USER_DATA_URL = URL_IP + "/KPServiceNew/rest/checkUser/getAdmissionAndPanelistUser/key=6B32B/test/cpD4Lg0M5DWIMAi6MYY3cg==";
    //public final static String USER_DATA_URL = URL_IP + "/KPServiceNew/rest/checkUser/getAdmissionAndPanelistUser/key=6B32B/anirban.ghatak/CQHSk_E5Fa-llDwbStfCvw==";
    public final static String IMAGE_URL = URL_IP + "/KnowledgePro/images/StudentPhotos/";
    public final static String WS_GET_URL = URL_IP + "/ExChristWSNew/services/get";
    public final static String WS_SET_URL = URL_IP + "/ExChristWSNew/services/set";
    public static String SYNCH_DATE_FILE_NAME = "SYNCH_DATE_INFO";

    public static File getDataFile(String filename) {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return new File(path, "CHRIST_DATA_" + filename + ".DAT");
    }
    public static void setExternalData(String filename, String data) {
        try {
            FileOutputStream stream = new FileOutputStream(getDataFile(filename), false);
            stream.write(data.getBytes());
            stream.close();
        } catch (Exception ex) {
        }
    }
    public static String getExternalDataCreatedDate(String filename) {
        Date createdOn = null;
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(2000, 1, 1);
            createdOn = calendar.getTime();

            File file = getDataFile(filename);
            if (file.exists()) {
                createdOn = new Date(file.lastModified());
            }
        } catch (Exception ex) {
        }
        if (createdOn != null) {
            SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
            return format.format(createdOn);
        } else {
            return "";
        }
    }
    public static String getExternalData(String filename) {
        StringBuilder data = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            File file = getDataFile(filename);
            bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                data.append(line);
            }
        } catch (Exception ex) {
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception ex) {
                }
            }
        }
        return data.toString();
    }
    public static void setSynchDate(Context context) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy hh:mm a");
            String date = format.format(new Date());
            writeObjectToDevice(context, SYNCH_DATE_FILE_NAME, date);
        }
        catch(Exception ex) { }
    }
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            myBitmap = Bitmap.createScaledBitmap(myBitmap, 100, 100, false);//This is only if u want to set the image size.
            return myBitmap;
        } catch (IOException e) {
            return null;
        }
    }
    public static String getSynchDate(Context context) {
        String date = "NA";
        try { date = (String) readObjectFromDevice(context, SYNCH_DATE_FILE_NAME); } catch(Exception ex) { }
        if(date == null || date.trim().length() == 0) { date = "NA"; }
        return date;
    }
    public static void loadActionBarWithTimer(AppCompatActivity activity) {
        ActionBar actionbar = activity.getSupportActionBar();
        actionbar.setBackgroundDrawable(Common.getDrawable(activity, R.drawable.dr_actionbar));
        final ViewGroup actionbarview = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.view_timer_action_bar, null);
        actionbar.setDisplayShowCustomEnabled(true);
        actionbar.setCustomView(actionbarview);

        final TextView lbl_time = (TextView) actionbarview.findViewById(R.id.lbl_time);
        if(lbl_time != null) {
            lbl_time.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/nk57-monospace-cd-bd.ttf"));
        }
        final CheckBox chk_action = (CheckBox) actionbarview.findViewById(R.id.chk_action);
        if(chk_action != null) {
            chk_action.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        lbl_time.setText("00:00");
                    }
                }
            });
        }

        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                lbl_time.setText(msg.obj.toString());
                return false;
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                long start = 0;

                while(true) {
                    if(chk_action.isChecked()) {
                        if(start == 0) start = new Date().getTime();
                        long now = new Date().getTime();
                        long total_seconds = ((now - start) / 1000);
                        Message msg = new Message();
                        msg.obj = "00:00";

                        if(total_seconds == 0) {
                            handler.sendMessage(msg);
                        }
                        else {
                            String minutes = Integer.toString((int) (total_seconds / 60));
                            String seconds = Integer.toString((int) (total_seconds % 60));

                            if(minutes.length() == 1) minutes = "0" + minutes;
                            if(seconds.length() == 1) seconds = "0" + seconds;

                            msg.obj = minutes + ":" + seconds;
                            handler.sendMessage(msg);
                        }
                    }
                    else { start = 0; }
                    try {
                        Thread.currentThread().sleep(50);
                    } catch (Exception e) { }
                }
            }
        }).start();
    }
    public static Typeface getDigitalFontface(Context context) {
        return Typeface.DEFAULT_BOLD; // Typeface.createFromAsset(context.getAssets(), "fonts/digital-7.ttf");
    }
    public static Drawable getDrawable(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getResources().getDrawable(id, context.getTheme());
        }
        else {
            return context.getResources().getDrawable(id);
        }
    }
    public static ArrayList<View> getChildren(ViewGroup parent) {
        ArrayList<View> children = new ArrayList<View>();
        if(parent != null) {
            int max = parent.getChildCount();
            for(int i=0; i<max; i++) {
                View view = parent.getChildAt(i);
                children.add(view);
                if(view instanceof ViewGroup) {
                    children.addAll(getChildren((ViewGroup) view));
                }
            }
        }
        return children;
    }
    public static boolean isNetworkConnected(Context context) {
        try
        {
            if(context != null)
            {
                ConnectivityManager connection = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if(connection != null)
                {
                    NetworkInfo[] networks = (NetworkInfo[]) connection.getAllNetworkInfo();
                    if(networks != null && networks.length > 0)
                    {
                        for(int i = 0; i < networks.length; i++)
                        {
                            if(networks[i].getState() == NetworkInfo.State.CONNECTED) return true;
                        }
                    }
                }
            }
        }
        catch(Exception ex) { }
        return false;
    }
    public static void confirm(Context context, String message, final DialogInterface.OnClickListener callback) {
        try
        {
            AlertDialog dlg = new AlertDialog.Builder(context)
                .setTitle("Confirm")
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null) {
                            callback.onClick(dialog, which);
                        }
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback != null) {
                            callback.onClick(dialog, which);
                        }
                        dialog.dismiss();
                    }
                }).create();

            dlg.show();

            styleButton(context, dlg.getButton(Dialog.BUTTON_POSITIVE));

            Button button = dlg.getButton(Dialog.BUTTON_NEGATIVE);
            styleButton(context, button);
            button.setAlpha(.7F);
        }
        catch(Exception ex) { }
    }
    public static void alert(Context context, String message) {
        alert(context, message, null);
    }
    public static void alert(Context context, String message, final DialogInterface.OnClickListener callback) {
        try
        {
            AlertDialog dlg = new AlertDialog.Builder(context)
                    .setTitle("Alert")
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (callback != null) {
                                callback.onClick(dialog, which);
                            }

                            dialog.dismiss();
                        }
                    }).create();
            dlg.show();

            styleButton(context, dlg.getButton(Dialog.BUTTON_POSITIVE));
        }
        catch(Exception ex) { }
    }
    public static void writeObjectToDevice(Context context, String path, Object data) {
        try {
            File installation = new File(context.getFilesDir(), path);
            FileOutputStream out = null;
            ObjectOutputStream oos = null;
            try {
                out = new FileOutputStream(installation);
                oos = new ObjectOutputStream(out);
                oos.writeObject(data);
            }
            catch(Exception ex) { }
            finally {
                if(oos != null) { oos.close(); }
                if(out != null) { out.close(); }
            }
        } catch (Exception error) {}
    }
    public static Object readObjectFromDevice(Context context, String path) {
        Object data = null;
        try {
            File installation = new File(context.getFilesDir(), path);
            FileInputStream in = null;
            ObjectInputStream ois = null;
            try {
                in = new FileInputStream(installation);
                ois = new ObjectInputStream(in);
                data = ois.readObject();
            }
            catch(Exception ex) { }
            finally {
                if(ois != null) { ois.close(); }
                if(in != null) { in.close(); }
            }
        } catch (Exception error) {}
        return data;
    }

    public static void styleButton(Context context, Button button) {
        button.setGravity(Gravity.CENTER);
        button.setTextColor(Color.parseColor("#FFFFFF"));
        button.setTextSize(16);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setFocusable(true);
        button.setClickable(true);
        button.setMinimumWidth(100);
        button.setBackgroundDrawable(Common.getDrawable(context, R.drawable.dr_exbtn_bg));
    }
}
