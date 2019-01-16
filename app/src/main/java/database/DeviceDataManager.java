package database;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import domain.ExBaseObject;
import domain.ExCourseRangeInfo;
import domain.ExGroupInfo;
import domain.ExProgramInfo;
import domain.ExScore;
import domain.ExScoreCardCourseInfo;
import domain.ExScoreInfo;
import domain.ExSelectionProcessInfo;
import domain.ExTemplateInfo;
import utils.Common;
import utils.JsonUtils;

public class DeviceDataManager {
    public static Object Lock = new Object();
    private static final String SCORECARD_DATD_PATH = "ChristScoreCardData";
    private static final String DEVICE_DATD_PATH = "ChristDeviceData";
    private static final String CONFIG_PATH = "ChristLocalDB";

    public static LocalDataHolder Data = new LocalDataHolder();
    public static DeviceDataHolder DeviceData = new DeviceDataHolder();

    public static void loadData(final Context context, final Handler progressHandler) {
        DeviceDataManager.DeviceData = new DeviceDataHolder();

        /*----- Message Handler -----*/
        final Handler completedHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                DeviceDataManager.saveDeviceData(context);
                return true;
            }
        });

        /*----- Data Loading -----*/
        progressHandler.sendMessage(getMessage(0, "Loading selection process list..."));
        DeviceDataManager.loadSelectionProcessList(context, new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == -1) {
                    Common.alert(context, "Operation Failed!");
                } else {
                    progressHandler.sendMessage(getMessage(0, "Loading score card templates..."));
                    DeviceDataManager.loadTemplateList(context, new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            progressHandler.sendMessage(getMessage(0, "Loading programs..."));
                            DeviceDataManager.loadProgramList(context, new Handler(new Handler.Callback() {
                                @Override
                                public boolean handleMessage(Message msg) {
                                    progressHandler.sendMessage(getMessage(0, "Loading application numbers..."));
                                    DeviceDataManager.loadCourseRangeList(context, new Handler(new Handler.Callback() {
                                        @Override
                                        public boolean handleMessage(Message msg) {
                                            progressHandler.sendMessage(getMessage(0, "Loading score cards and related courses..."));
                                            DeviceDataManager.loadScoreCardCourseList(context, new Handler(new Handler.Callback() {
                                                @Override
                                                public boolean handleMessage(Message msg) {
                                                    completedHandler.sendEmptyMessage(0);
                                                    progressHandler.sendMessage(getMessage(1, "Data loading completed..."));
                                                    return true;
                                                }
                                            }));
                                            return true;
                                        }
                                    }));
                                    return true;
                                }
                            }));
                            return true;
                        }
                    }));
                }
                return true;
            }
        }));
    }
    public static void init(final Context context) {
        DeviceDataManager.readDeviceData(context);
        DeviceDataManager.readLocalData(context);
    }
    public static Message getMessage(int status, String message) {
        Message msg = new Message();
        ExBaseObject object = new ExBaseObject();
        object.ID = status;
        object.Name = message;
        msg.obj = object;
        return msg;
    }
    public static void loadSelectionProcessList(Context context, final Handler handler) {
        try {
            DeviceDataManager.get(context, "PROCESS_LIST", "", new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    try {
                        if (msg.obj != null) {
                            String json = (String) msg.obj;
                            if (json != null && json.trim().length() > 0) {
                                ExSelectionProcessInfo result = new ExSelectionProcessInfo();
                                JsonUtils.getObject(result, json);
                                try {
                                    DeviceDataManager.DeviceData.SelectionProcessList = result.Data;
                                } catch (Exception ex) {
                                }
                                return true;
                            }
                        }
                    } catch (Exception ex) {
                    } finally {
                        if (handler != null) {
                            handler.sendEmptyMessage(0);
                        }
                    }
                    return false;
                }
            }));
        }
        catch(Exception ex) { }
    }
    public static void loadGroupList(Context context, String centerID, final Handler handler) {
        try {
            DeviceDataManager.get(context, "GROUP_LIST", centerID, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    try {
                        if (msg.obj != null) {
                            String json = (String) msg.obj;
                            if (json != null && json.trim().length() > 0) {
                                ExGroupInfo result = new ExGroupInfo();
                                JsonUtils.getObject(result, json);
                                try {
                                    DeviceDataManager.DeviceData.GroupList = result.Data;
                                } catch (Exception ex) {
                                }
                                return true;
                            }
                        }
                    } catch (Exception ex) {
                    } finally {
                        if (handler != null) {
                            handler.sendEmptyMessage(0);
                        }
                    }
                    return false;
                }
            }));
        }
        catch(Exception ex) { }
    }
    public static void loadTemplateList(Context context, final Handler handler) {
        try {
            DeviceDataManager.get(context, "TEMPLATE_LIST", "", new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    try {
                        if (msg.obj != null) {
                            String json = (String) msg.obj;
                            if (json != null && json.trim().length() > 0) {
                                ExTemplateInfo result = new ExTemplateInfo();
                                JsonUtils.getObject(result, json);
                                try {
                                    DeviceDataManager.DeviceData.TemplateList = result.Data;
                                } catch (Exception ex) {
                                }
                                return true;
                            }
                        }
                    } catch (Exception ex) {
                    } finally {
                        if (handler != null) {
                            handler.sendEmptyMessage(0);
                        }
                    }
                    return false;
                }
            }));
        }
        catch(Exception ex) { }
    }
    public static void loadProgramList(Context context, final Handler handler) {
        try {
            DeviceDataManager.get(context, "PROGRAM_LIST", "", new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    try {
                        if (msg.obj != null) {
                            String json = (String) msg.obj;
                            if (json != null && json.trim().length() > 0) {
                                ExProgramInfo result = new ExProgramInfo();
                                JsonUtils.getObject(result, json);
                                try {
                                    DeviceDataManager.DeviceData.ProgramList = result.Data;
                                } catch (Exception ex) {
                                }
                                return true;
                            }
                        }
                    } catch (Exception ex) {
                    } finally {
                        if (handler != null) {
                            handler.sendEmptyMessage(0);
                        }
                    }
                    return false;
                }
            }));
        }
        catch(Exception ex) { }
    }
    public static void loadCourseRangeList(Context context, final Handler handler) {
        try {
            DeviceDataManager.get(context, "COURSE_RANGE_LIST", "", new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    try {
                        if (msg.obj != null) {
                            String json = (String) msg.obj;
                            if (json != null && json.trim().length() > 0) {
                                ExCourseRangeInfo result = new ExCourseRangeInfo();
                                JsonUtils.getObject(result, json);
                                try {
                                    DeviceDataManager.DeviceData.CourseRangeList = result.Data;
                                } catch (Exception ex) {
                                }
                                return true;
                            }
                        }
                    } catch (Exception ex) {
                    } finally {
                        if (handler != null) {
                            handler.sendEmptyMessage(0);
                        }
                    }
                    return false;
                }
            }));
        }
        catch(Exception ex) { }
    }
    public static void loadScoreCardCourseList(Context context, final Handler handler) {
        try {
            DeviceDataManager.get(context, "SCORECARD_COURSE_LIST", "", new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    try {
                        if (msg.obj != null) {
                            String json = (String) msg.obj;
                            if (json != null && json.trim().length() > 0) {
                                ExScoreCardCourseInfo result = new ExScoreCardCourseInfo();
                                JsonUtils.getObject(result, json);
                                try {
                                    DeviceDataManager.DeviceData.ScoreCardCourseList = result.Data;
                                } catch (Exception ex) {
                                }
                                return true;
                            }
                        }
                    } catch (Exception ex) {
                    } finally {
                        if (handler != null) {
                            handler.sendEmptyMessage(0);
                        }
                    }
                    return false;
                }
            }));
        }
        catch(Exception ex) { }
    }

    public static void saveDeviceData(Context context) {
        try {
            if(DeviceDataManager.DeviceData == null) {
                DeviceDataManager.DeviceData = new DeviceDataHolder();
            }
            File installation = new File(context.getFilesDir(), DEVICE_DATD_PATH);
            FileOutputStream out = null;
            ObjectOutputStream oos = null;
            try {
                out = new FileOutputStream(installation);
                oos = new ObjectOutputStream(out);
                oos.writeObject(DeviceDataManager.DeviceData);
            }
            catch(Exception ex) { }
            finally {
                if(oos != null) { oos.close(); }
                if(out != null) { out.close(); }
            }

            readDeviceData(context);
        } catch (Exception error) {}
    }
    public static void readDeviceData(Context context) {
        try {
            DeviceDataManager.DeviceData = new DeviceDataHolder();
            File installation = new File(context.getFilesDir(), DEVICE_DATD_PATH);
            FileInputStream in = null;
            ObjectInputStream ois = null;
            try {
                in = new FileInputStream(installation);
                ois = new ObjectInputStream(in);
                DeviceDataManager.DeviceData = (DeviceDataHolder) ois.readObject();
            }
            catch(Exception ex) { }
            finally {
                if(ois != null) { ois.close(); }
                if(in != null) { in.close(); }
            }
        } catch (Exception error) {}
    }
    public static void saveLocalData(Context context) {
        try {
            if(DeviceDataManager.Data == null) {
                DeviceDataManager.Data = new LocalDataHolder();
            }
            File installation = new File(context.getFilesDir(), CONFIG_PATH);
            FileOutputStream out = new FileOutputStream(installation);
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(DeviceDataManager.Data);
            oos.close();
            out.close();
        } catch (Exception error) {}
    }
    public static void readLocalData(Context context) {
        DeviceDataManager.Data = new LocalDataHolder();
        File installation = new File(context.getFilesDir(), CONFIG_PATH);
        try {
            FileInputStream in = new FileInputStream(installation);
            ObjectInputStream ois = new ObjectInputStream(in);
            DeviceDataManager.Data = (LocalDataHolder) ois.readObject();
            ois.close();
            in.close();
        } catch (Exception error) {}
    }
    public static void get(Context context, final String key, final String jsonArgs, final Handler handler) {
        if(Common.isNetworkConnected(context)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String json = "";
                        try {
                            String urlText = Common.WS_GET_URL + "?key=" + key + "&args=" + URLEncoder.encode(jsonArgs, "UTF-8");
                            URL url = new URL(urlText);
                            if (url != null) {
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                if (connection != null) {
                                    byte[] bytes = null;

                                    connection.setReadTimeout(10 * 60 * 1000);
                                    connection.setConnectTimeout(10 * 60 * 1000);
                                    connection.setDoOutput(false);
                                    connection.setRequestMethod("GET");
                                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
                                    connection.setRequestProperty("Accept", "*/*");
                                    connection.connect();

                                /*----- READ OUTPUT FROM SERVER -----*/
                                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                                    InputStream in = connection.getInputStream();
                                    try {
                                        byte[] buffer = new byte[1024];
                                        int read = 0;
                                        while ((read = in.read(buffer)) != -1) {
                                            out.write(buffer, 0, read);
                                        }
                                        bytes = out.toByteArray();
                                    } catch (Exception ex) {
                                    } finally {
                                        try {
                                            if (in != null) {
                                                in.close();
                                            }
                                        } catch (Exception ex) {
                                        }
                                        try {
                                            if (out != null) {
                                                out.close();
                                            }
                                        } catch (Exception ex) {
                                        }
                                    }
                                    if (bytes != null && bytes.length > 0) {
                                        json = new String(bytes);
                                    }
                                }
                            }
                        } catch (Exception ex) {
                        }

                        if (handler != null) {
                            handler.sendMessage(handler.obtainMessage(1, json));
                        }
                    }
                    catch(Exception ex) {
                    }
                }
            }).start();
        }
    }
    public static String set(Context context, final String jsonArgs) {
        String json = "";
        try {
            String urlText = Common.WS_SET_URL;
            URL url = new URL(urlText);
            if (url != null) {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                if (connection != null) {
                    byte[] bytes = null;

                    connection.setReadTimeout(10 * 1000);
                    connection.setConnectTimeout(10 * 1000);
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "text/plain");
                    connection.setRequestProperty("charset", "utf-8");
                    connection.connect();

                    /*----- WRITE INPUT INTO SERVER -----*/
                    if (jsonArgs != null && jsonArgs.trim().length() > 0) {
                        OutputStreamWriter outsw = new OutputStreamWriter(connection.getOutputStream());
                        outsw.write(jsonArgs);
                        outsw.flush();
                        outsw.close();
                    }

                    /*----- READ OUTPUT FROM SERVER -----*/
                    InputStream in = connection.getInputStream();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try {
                        byte[] buffer = new byte[1024];
                        int read = 0;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                        bytes = out.toByteArray();
                    } catch (Exception ex) { }

                    finally {
                        try { if (in != null) { in.close(); } }
                        catch (Exception ex) { }
                        try { if (out != null) { out.close(); } }
                        catch (Exception ex) { }
                    }
                    if(bytes != null && bytes.length > 0) {
                        json = new String(bytes);
                    }
                }
            }
        }
        catch(Exception ex) { }

        return json;
    }
}
