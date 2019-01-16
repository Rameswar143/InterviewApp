package com.app.vst.christapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import database.DBInfo;
import database.DataSynchService;
import database.DeviceDataManager;
import database.LocalDataHolder;
import domain.ExUser;
import domain.ExUserInfo;
import utils.Common;
import utils.ExCrypto;
import utils.SyncStatusUpdater;

public class UILoginActivity extends AppCompatActivity {
    public static UILoginActivity Instance = null;
    private static ExUserInfo Users = new ExUserInfo();
    private static String USER_DATA_PATH = "UserData";
    private boolean IsActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(Instance != null) { Instance.finish(); }
        Instance = this;
        super.onCreate(savedInstanceState);
        try { this.getSupportActionBar().setBackgroundDrawable(Common.getDrawable(UILoginActivity.this, R.drawable.dr_actionbar)); }catch(Exception e) { }
        this.setContentView(R.layout.ui_login_activity);
        DeviceDataManager.init(UILoginActivity.this);

        try {
            startService(new Intent(this, DataSynchService.class));
        } catch(Exception e) { }

        this.init();
    }

    @Override
    protected void onResume() {
        SyncStatusUpdater.start(updateSyncPendingStatusHandler);
        super.onResume();
    }

    private Handler updateSyncPendingStatusHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateSyncPendingStatus();
                }
            });
            return true;
        }
    });

    public void updateSyncPendingStatus() {
        try {
            int count = DBInfo.createInstance(this).getUnsavedDataCount();
            TextView lbl_data_sync_info = (TextView) this.findViewById(R.id.lbl_data_sync_info);
            if (lbl_data_sync_info != null) {
                if (count > 0) {
                    lbl_data_sync_info.setText("Sync Pending : " + Integer.toString(count) + " Record(s)");
                }
            }
        }catch(Exception ex) {
        }
    }
    private void init() {
        final Button btn_login = (Button) this.findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        final Button btn_synch_login = (Button) this.findViewById(R.id.btn_synch_login);
        btn_synch_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                synchAndLogin();
            }
        });

        try {
            Object data = Common.readObjectFromDevice(UILoginActivity.this, UILoginActivity.USER_DATA_PATH);
            if (data != null && data instanceof ExUserInfo) {
                UILoginActivity.Users = (ExUserInfo) data;
            }
        }
        catch(Exception ex) { }

        final EditText txt_password = (EditText) this.findViewById(R.id.txt_password);
        if(txt_password != null) {
            txt_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            txt_password.setTypeface(Typeface.DEFAULT);
        }

        final CheckBox chk_show_pwd = (CheckBox) this.findViewById(R.id.chk_show_pwd);
        if(chk_show_pwd != null) {
            chk_show_pwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        txt_password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    } else {
                        txt_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        txt_password.setTypeface(Typeface.DEFAULT);
                    }
                    try {
                        txt_password.setSelection(txt_password.getText().length());
                    } catch (Exception ex) {
                    }
                }
            });
        }

        try {
            TextView lbl_info = (TextView) this.findViewById(R.id.lbl_info);
            String info = "( ";
            try {
                PackageInfo pInfo = null;
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                info += "Version " + pInfo.versionName + ", ";
            } catch (Exception e) {
            }
            info += "Last synchronized on: " + Common.getSynchDate(this) + " )";
            lbl_info.setText(info);
        }
        catch(Exception ex) { }
    }
    private void login() {
        if(this.Users == null || this.Users.Data == null || this.Users.Data.size() == 0) {
            if(Common.isNetworkConnected(UILoginActivity.this) == false) {
                Common.alert(UILoginActivity.this, "Please check your internet connection!");
                return;
            }
        }

        final ProgressDialog wait = ProgressDialog.show(UILoginActivity.this, "", "Please wait...", true, false);
        try {
            final EditText txt_user_name = (EditText) this.findViewById(R.id.txt_user_name);
            final EditText txt_password = (EditText) this.findViewById(R.id.txt_password);

            String username = txt_user_name.getText().toString();
            String password = txt_password.getText().toString();

            password = ExCrypto.encrypt(password, "l15h9ji4kl6hj5k1");
            this.isValidUser(username, password, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    try {
                        if (DeviceDataManager.Data != null && DeviceDataManager.Data.SelectedUser != null) {
                            ExUser userinfo = DeviceDataManager.Data.SelectedUser;
                            if (userinfo != null && userinfo.ID != null && userinfo.ID.trim().length() > 0) {
                                //DeviceDataManager.saveLocalData(UILoginActivity.this);
                                UILoginActivity.this.startActivity(new Intent(UILoginActivity.this, UIHomeActivity.class));
                                UILoginActivity.this.finish();
                                return true;
                            }
                        }
                    } catch (Exception ex) {
                    } finally {
                        wait.dismiss();
                    }

                    Common.alert(UILoginActivity.this, "Invalid Login ID or Password!");
                    txt_user_name.requestFocus();
                    return false;
                }
            }));
        } catch (Exception ex) {
            wait.dismiss();
        }
    }
    private void synchAndLogin() {
        if(Common.isNetworkConnected(UILoginActivity.this) == false) {
            Common.alert(UILoginActivity.this, "Please check your internet connection!");
            return;
        }
        else {
            UILoginActivity.Users = new ExUserInfo();
            Common.writeObjectToDevice(UILoginActivity.this, UILoginActivity.USER_DATA_PATH, UILoginActivity.Users);
            login();
        }
    }
    private void isValidUser(final String loginid, final String password, final Handler handler) {
        try {
            if(UILoginActivity.Users != null && UILoginActivity.Users.Data != null && UILoginActivity.Users.Data.size() > 0) {
                UILoginActivity.this.isValidUserEx(loginid, password, handler);
            }
            else {
                final Handler srvhandler = new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        if(UILoginActivity.Users == null) {
                            UILoginActivity.Users = new ExUserInfo();
                        }
                        Common.writeObjectToDevice(UILoginActivity.this, UILoginActivity.USER_DATA_PATH, UILoginActivity.Users);
                        UILoginActivity.this.isValidUserEx(loginid, password, handler);
                        return true;
                    }
                });
                loadUsers(srvhandler);
            }
        }
        catch(Exception ex) { }
    }
    private void loadUsers(final Handler handler) {
        this.readFromServer(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                try {
                    if (msg != null && msg.obj != null && msg.obj instanceof String) {
                        String xml = (String) msg.obj;
                    /*----- INVOKE CALLBACK METHOD -----*/
                        UILoginActivity.Users = new ExUserInfo();
                        UILoginActivity.Users.Data = new ArrayList<ExUser>();

                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = null;
                        try {
                            builder = factory.newDocumentBuilder();
                            ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes("UTF-8"));
                            Document doc = builder.parse(input);
                            NodeList nodes = doc.getElementsByTagName("userList");
                            if (nodes != null) {
                                for (int i = 0; i < nodes.getLength(); i++) {
                                    ExUser userInfo = new ExUser();
                                    UILoginActivity.Users.Data.add(userInfo);

                                    Node node = nodes.item(i);
                                    NodeList children = node.getChildNodes();
                                    if (children != null) {
                                        for (int j = 0; j < children.getLength(); j++) {
                                            Node child = children.item(j);
                                            String name = child.getNodeName().toUpperCase();
                                            String value = child.getTextContent();
                                            switch (name) {
                                                case "ADMISSION_ADMIN":
                                                    userInfo.IsAdmin = "1".compareToIgnoreCase(value) == 0;
                                                    break;
                                                case "PANELIST":
                                                    userInfo.IsPannelist = "1".compareToIgnoreCase(value) == 0;
                                                    break;
                                                case "PASSWORD":
                                                    userInfo.Password = value;
                                                    break;
                                                case "USERID":
                                                    userInfo.ID = value;
                                                    break;
                                                case "USERNAME":
                                                    userInfo.Name = userInfo.LoginID = value;
                                                    break;
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                }
                catch(Exception e) { }
                finally {
                    if (handler != null) { handler.sendEmptyMessage(0); }
                }
                return false;
            }
        }));
    }
    private void readFromServer(final Handler handler) {
        if(Common.isNetworkConnected(UILoginActivity.this))
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String resultXml = null;
                    try {
					    /*----- READ STREAM FROM SERVER -----*/
                        byte[] bytes = null;
                        try {
                            URL url = new URL(Common.USER_DATA_URL);
                            if (url != null) {
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                if (connection != null) {
                                    connection.setReadTimeout(20 * 1000);
                                    connection.setConnectTimeout(20 * 1000);
                                    connection.setDoOutput(false);
                                    connection.setRequestMethod("GET");
                                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
                                    connection.setRequestProperty("Accept","*/*");
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
                                            if (in != null) { in.close(); in = null; }
                                        } catch (Exception ex) { }
                                        try {
                                            if (out != null) { out.close(); out = null; }
                                        } catch (Exception ex) { }
                                    }
                                }
                            }
                        } catch (Exception ex) { }
					    /*----- CONVERT BYTES TO JSON TEXT -----*/
                        try {
                            if (bytes != null && bytes.length > 0) { resultXml = new String(bytes); }
                        } catch (Exception ex) { }
                    }
                    catch(Exception ex) { }
                    finally {
                        if(handler != null) {
                            Message msg = new Message();
                            msg.obj = resultXml;
                            handler.sendMessage(msg);
                        }
                    }
                }
            }).start();
        }
    }
    private void isValidUserEx(String loginid, String password, Handler handler) {
        if(DeviceDataManager.Data == null) {
            DeviceDataManager.Data = new LocalDataHolder();
        }
        DeviceDataManager.Data.SelectedUser = null;
        if(this.Users != null && this.Users.Data != null && this.Users.Data.size() > 0) {
            for (ExUser user : this.Users.Data) {
                if (user.LoginID != null && user.Password != null && user.LoginID.compareToIgnoreCase(loginid) == 0 && user.Password.compareToIgnoreCase(password) == 0) {
                    DeviceDataManager.Data.SelectedUser = user;
                    break;
                }
            }
        }
        if(handler != null) { handler.sendEmptyMessage(0); }
    }
}
