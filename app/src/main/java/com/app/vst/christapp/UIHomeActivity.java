package com.app.vst.christapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import database.DeviceDataManager;
import database.LocalDataHolder;
import domain.ExBaseObject;
import domain.ExCourseRange;
import domain.ExGroup;
import domain.ExScanData;
import domain.ExSelectionProcessTypes;
import domain.ExStudentInfo;
import utils.Common;
import utils.JsonUtils;

public class UIHomeActivity extends UIBaseActivity {
    public static UIHomeActivity Instance = null;
    private String ListMode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //if(Instance != null) { Instance.finish(); }
        Instance = this;

        super.onCreate(savedInstanceState);
        try { this.getSupportActionBar().setBackgroundDrawable(Common.getDrawable(UIHomeActivity.this, R.drawable.dr_actionbar));
        } catch (Exception e) { }
        this.setContentView(R.layout.ui_home_activity);
        this.init();
    }

    @Override
    public void onBackPressed() {
        if(DeviceDataManager.Data.SelectionProcessType == ExSelectionProcessTypes.Group) {
            if (ListMode.compareTo("G") == 0) {
                this.loadGroupTimes();
                return;
            }
        }
        else if(DeviceDataManager.Data.SelectionProcessType == ExSelectionProcessTypes.Individual) {
            final View pnl_application = this.findViewById(R.id.pnl_application);
            final View pnl_popup = this.findViewById(R.id.pnl_popup);
            if(pnl_popup.getVisibility() == View.VISIBLE) {
                pnl_application.setVisibility(View.VISIBLE);
                pnl_popup.setVisibility(View.GONE);
                return;
            }
        }

        super.signOutWithConfirm();
        //super.onBackPressed();
    }
    @Override
    protected void onResume() {
        View lbl_error = this.findViewById(R.id.lbl_error);
        View lvw_groups = this.findViewById(R.id.lvw_groups);
        View pnl_application = this.findViewById(R.id.pnl_application);

        lbl_error.setVisibility(View.GONE);
        lvw_groups.setVisibility(View.GONE);
        pnl_application.setVisibility(View.GONE);

        if(DeviceDataManager.Data == null || DeviceDataManager.Data.SelectionProcessType == ExSelectionProcessTypes.None) {
            try { this.getSupportActionBar().setTitle("Christ University"); } catch(Exception e) { }
            lbl_error.setVisibility(View.VISIBLE);
        }
        else if(DeviceDataManager.Data.SelectionProcessType == ExSelectionProcessTypes.Group) {
            lvw_groups.setVisibility(View.VISIBLE);
            try { this.getSupportActionBar().setTitle("Please Select Time"); } catch(Exception e) { }
            this.loadGroupTimes();
        }
        else if(DeviceDataManager.Data.SelectionProcessType == ExSelectionProcessTypes.Individual) {
            pnl_application.setVisibility(View.VISIBLE);
            try { this.getSupportActionBar().setTitle("Please Enter Application No"); } catch(Exception e) { }
            EditText txt_appl_num = (EditText) this.findViewById(R.id.txt_appl_num);
            txt_appl_num.setText("");
            txt_appl_num.requestFocus();
        }
        super.onResume();
    }
    private void init() {
        final TextView txt_appl_num = (TextView) this.findViewById(R.id.txt_appl_num);
        final View btn_continue = this.findViewById(R.id.btn_continue);
        if(btn_continue != null) {
            btn_continue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String appl_no = txt_appl_num.getText().toString();
                    if (appl_no == null || appl_no.trim().length() == 0) {
                        Common.alert(UIHomeActivity.this, "Invalid Application Number!");
                        txt_appl_num.requestFocus();
                    }
                    loadApplication(appl_no.trim());
                }
            });
        }
    }
    private void loadGroupTimes() {
        try { this.getSupportActionBar().setTitle("Please Select Time"); } catch(Exception e) { }
        ListMode = "T";
        final View pnl_popup = this.findViewById(R.id.pnl_popup);
        final ListView lvw_groups = (ListView) this.findViewById(R.id.lvw_groups);
        final ArrayList<String> times = new ArrayList<>();
        if(DeviceDataManager.DeviceData.GroupList != null && DeviceDataManager.DeviceData.GroupList.size() > 0) {
            for(ExGroup group : DeviceDataManager.DeviceData.GroupList) {
                if(group.ExamCenterID == DeviceDataManager.Data.SelectedCenter.ID) {
                    if(!times.contains(group.Time)) { times.add(group.Time); }
                }
            }
        }

        if(times.size() > 0) {
            lvw_groups.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return times.size();
                }

                @Override
                public String getItem(int position) {
                    return times.get(position);
                }

                @Override
                public long getItemId(int position) {
                    return position;
                }

                @Override
                public View getView(int position, View view, ViewGroup parent) {
                    String item = this.getItem(position);
                    if (view == null) {
                        view = UIHomeActivity.this.getLayoutInflater().inflate(R.layout.view_program_item, null);
                    }

                    view.setTag(item);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String time = (String) v.getTag();
                            if (time != null) {
                                loadGroups(time);
                            }
                        }
                    });

                    TextView lbl_text = (TextView) view.findViewById(R.id.lbl_text);
                    if (lbl_text != null) {
                        lbl_text.setText(item);
                    }

                    return view;
                }
            });
        }
    }
    private void loadGroups(String time) {
        try { this.getSupportActionBar().setTitle("Please Select Group"); } catch(Exception e) { }
        ListMode = "G";
        final View pnl_popup = this.findViewById(R.id.pnl_popup);
        final ListView lvw_groups = (ListView) this.findViewById(R.id.lvw_groups);
        final ArrayList<ExGroup> groups = new ArrayList<>();
        if(DeviceDataManager.DeviceData.GroupList != null && DeviceDataManager.DeviceData.GroupList.size() > 0) {
            for(ExGroup group : DeviceDataManager.DeviceData.GroupList) {
                if(group.ExamCenterID == DeviceDataManager.Data.SelectedCenter.ID && group.Time.compareToIgnoreCase(time) == 0) {
                    groups.add(group);
                }
            }
        }

        if(groups.size() > 0) {
            lvw_groups.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return groups.size();
                }

                @Override
                public ExBaseObject getItem(int position) {
                    return groups.get(position);
                }

                @Override
                public long getItemId(int position) {
                    return position;
                }

                @Override
                public View getView(int position, View view, ViewGroup parent) {
                    ExBaseObject item = this.getItem(position);
                    if (view == null) {
                        view = UIHomeActivity.this.getLayoutInflater().inflate(R.layout.view_program_item, null);
                    }

                    view.setTag(item);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (pnl_popup != null) {
                                pnl_popup.setVisibility(View.GONE);
                            }

                            ExGroup group = (ExGroup) v.getTag();
                            if (group != null) {
                                UIGroupActivity.SelectedGroup = group;
                                startActivity(new Intent(UIHomeActivity.this, UIGroupActivity.class));
                            }
                        }
                    });

                    TextView lbl_text = (TextView) view.findViewById(R.id.lbl_text);
                    if (lbl_text != null) {
                        lbl_text.setText(item.Name);
                    }

                    return view;
                }
            });
        }
    }
    private void loadApplication(String applicationNo) {
        final ProgressDialog wait = ProgressDialog.show(UIHomeActivity.this, "", "Wait while loading...", true, false);
        try {
            final int applNo = Integer.parseInt(applicationNo.trim());
            if(Common.isNetworkConnected(UIHomeActivity.this)) {
                DeviceDataManager.get(UIHomeActivity.this, "SCAN_DATA", applicationNo.trim(), new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        try {
                            ExScanData result = new ExScanData();
                            if (msg.obj != null) {
                                String json = (String) msg.obj;
                                if (json != null && json.trim().length() > 0) {
                                    JsonUtils.getObject(result, json);
                                }
                            }

                            if (result.Course == null || result.Course.ID == 0) {
                                loadApplicationOffline(applNo);
                            } else {
                                ExStudentInfo studentInfo = new ExStudentInfo();
                                studentInfo.CourseID = result.Course.ID;
                                studentInfo.AppNo = result.ApplicationNo;
                                studentInfo.AppID = result.ApplicationID;
                                studentInfo.Name = result.FirstName;
                                loadTemplates(studentInfo);
                            }
                        } catch (Exception ex) {
                        } finally {
                            wait.dismiss();
                        }
                        return true;
                    }
                }));
            }
            else {
                loadApplicationOffline(applNo);
                wait.dismiss();
            }
        }
        catch(Exception ex) {
            wait.dismiss();
            final TextView txt_appl_num = (TextView) this.findViewById(R.id.txt_appl_num);
            Common.alert(UIHomeActivity.this, "Invalid Application Number!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    txt_appl_num.requestFocus();
                }
            });
        }
    }
    private void loadApplicationOffline(final int applNo) {
        if (DeviceDataManager.DeviceData != null && DeviceDataManager.DeviceData.CourseRangeList != null) {
            final ArrayList<ExBaseObject> courses = new ArrayList<ExBaseObject>();
            for (ExCourseRange range : DeviceDataManager.DeviceData.CourseRangeList) {
                if ((applNo >= range.OfflineNoFrom && applNo <= range.OfflineNoTo) ||
                        (applNo >= range.OnlineNoFrom && applNo <= range.OnlineNoTo)) {
                    ExBaseObject course = new ExBaseObject();
                    course.ID = range.ID;
                    course.Name = range.Name;
                    courses.add(course);
                }
            }

            final TextView txt_appl_num = (TextView) this.findViewById(R.id.txt_appl_num);
            if (courses == null || courses.size() == 0) {
                Common.alert(UIHomeActivity.this, "Invalid Application Number!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        txt_appl_num.requestFocus();
                    }
                });
            } else if (courses.size() == 1) {
                ExStudentInfo studentInfo = new ExStudentInfo();
                studentInfo.CourseID = courses.get(0).ID;
                studentInfo.AppNo = applNo;
                studentInfo.Name = "Not Available";
                loadTemplates(studentInfo);
            } else {
                final View pnl_application = this.findViewById(R.id.pnl_application);
                final View pnl_popup = this.findViewById(R.id.pnl_popup);
                final ListView lvw_popup_items = (ListView) this.findViewById(R.id.lvw_popup_items);
                pnl_application.setVisibility(View.GONE);
                pnl_popup.setVisibility(View.VISIBLE);
                if (lvw_popup_items != null) {
                    lvw_popup_items.setAdapter(new BaseAdapter() {
                        @Override
                        public int getCount() {
                            return courses.size();
                        }

                        @Override
                        public ExBaseObject getItem(int position) {
                            return courses.get(position);
                        }

                        @Override
                        public long getItemId(int position) {
                            return position;
                        }

                        @Override
                        public View getView(int position, View view, ViewGroup parent) {
                            ExBaseObject item = this.getItem(position);
                            if (view == null) {
                                view = UIHomeActivity.this.getLayoutInflater().inflate(R.layout.view_program_item, null);
                            }

                            view.setTag(item);
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ExBaseObject tag = (ExBaseObject) v.getTag();
                                    if (tag != null) {
                                        ExStudentInfo studentInfo = new ExStudentInfo();
                                        studentInfo.CourseID = tag.ID;
                                        studentInfo.AppNo = applNo;
                                        studentInfo.Name = "Not Available";
                                        loadTemplates(studentInfo);
                                    }
                                    if (pnl_popup != null) {
                                        pnl_popup.setVisibility(View.GONE);
                                    }
                                }
                            });
                            TextView lbl_text = (TextView) view.findViewById(R.id.lbl_text);
                            if (lbl_text != null) {
                                lbl_text.setText(item.Name);
                            }

                            return view;
                        }
                    });
                }
            }
        }
    }
    private void loadTemplates(ExStudentInfo studentInfo) {
        UISingleActivity.SelectedStudent = studentInfo;
        startActivity(new Intent(this, UISingleActivity.class));
    }
}
