package com.app.vst.christapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import database.DBInfo;
import database.DeviceDataManager;
import database.StudentDataHandler;
import domain.ExBaseObject;
import domain.ExProgram;
import domain.ExSelectionProcess;
import domain.ExSelectionProcessInfo;
import domain.ExSelectionProcessTypes;
import domain.ExStudentDataArgs;
import domain.ExTemplate;
import utils.Common;
import utils.JsonUtils;

public class UIConfigActivity extends UIBaseActivity {
    private static UIConfigActivity Instance = null;
    private SimpleDateFormat dateFormatter = null;
    private String ListMode = "S"; //S:Selection Process, P:Program, C:Center, T:Template
    private ExSelectionProcessTypes SelectionProcessType = ExSelectionProcessTypes.Individual;
    private ExBaseObject SelectedProgram = new ExBaseObject();
    private ExBaseObject SelectedCenter = new ExBaseObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(Instance != null) { Instance.finish(); }
        Instance = this;

        super.onCreate(savedInstanceState);
        try { this.getSupportActionBar().setBackgroundDrawable(Common.getDrawable(UIConfigActivity.this, R.drawable.dr_actionbar)); }catch(Exception e) { }
        setContentView(R.layout.ui_config_activity);
        this.init();
    }
    private void init() {
        Calendar newCalendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("dd MMM yyyy", Locale.US);
        final EditText txt_from_date = (EditText) findViewById(R.id.txt_from_date);
        final EditText txt_to_date = (EditText) findViewById(R.id.txt_to_date);
        if(txt_from_date != null) {
            final DatePickerDialog fromDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year, monthOfYear, dayOfMonth);
                    txt_from_date.setText(dateFormatter.format(newDate.getTime()));
                }

            }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
            fromDatePickerDialog.getDatePicker().setCalendarViewShown(false);
            fromDatePickerDialog.setTitle("");
            txt_from_date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fromDatePickerDialog.show();
                }
            });
        }
        if(txt_to_date != null) {
            final DatePickerDialog toDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year, monthOfYear, dayOfMonth);
                    txt_to_date.setText(dateFormatter.format(newDate.getTime()));
                }

            }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
            toDatePickerDialog.getDatePicker().setCalendarViewShown(false);
            toDatePickerDialog.setTitle("");
            txt_to_date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toDatePickerDialog.show();
                }
            });
        }

        View btn_load_info = this.findViewById(R.id.btn_load_info);
        if(btn_load_info != null) {
            btn_load_info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadStudentData();
                }
            });
        }
        View btn_skip_info = this.findViewById(R.id.btn_skip_info);
        if(btn_skip_info != null) {
            btn_skip_info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    findViewById(R.id.pnl_info_layout).setVisibility(View.GONE);
                }
            });
        }

        final RadioButton rb_group = (RadioButton) this.findViewById(R.id.rb_group);
        final RadioButton rb_individual = (RadioButton) this.findViewById(R.id.rb_individual);
        final View btn_back = this.findViewById(R.id.btn_back);
        final View btn_submit = this.findViewById(R.id.btn_submit);
        final View btn_load = this.findViewById(R.id.btn_load);
        final View btn_skip = this.findViewById(R.id.btn_skip);
        final View btn_load_from_file = this.findViewById(R.id.btn_load_from_file);

        if(btn_skip != null) {
            btn_skip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIConfigActivity.this.startActivity(new Intent(UIConfigActivity.this, UIHomeActivity.class));
                    UIConfigActivity.this.finish();
                }
            });
        }

        if(btn_load != null) {
            btn_load.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadData();
                }
            });
        }

        if(btn_load_from_file != null) {
            btn_load_from_file.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Common.confirm(UIConfigActivity.this, "Do you want load data from files?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(i == Dialog.BUTTON_POSITIVE) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
                                        String today = formatter.format(Calendar.getInstance().getTime());
                                        String date = Common.getExternalDataCreatedDate(StudentDataHandler.STUDENT_DTLS);
                                        if(date == null || today.compareToIgnoreCase(date) != 0) {
                                            Common.alert(UIConfigActivity.this, "No latest files are available!");
                                            return;
                                        }

                                        date = Common.getExternalDataCreatedDate(StudentDataHandler.EDUCATION_DTLS);
                                        if(date == null || today.compareToIgnoreCase(date) != 0) {
                                            Common.alert(UIConfigActivity.this, "No latest files are available!");
                                            return;
                                        }

                                        findViewById(R.id.pnl_inputs).setVisibility(View.GONE);
                                        findViewById(R.id.pnl_waiting_overlay).setVisibility(View.VISIBLE);
                                        StudentDataHandler.loadDataFromFiles(UIConfigActivity.this, new Handler(new Handler.Callback() {
                                            @Override
                                            public boolean handleMessage(Message message) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        findViewById(R.id.pnl_info_layout).setVisibility(View.GONE);
                                                    }
                                                });
                                                return true;
                                            }
                                        }));
                                    }
                                });
                            }
                        }
                    });
                }
            });
        }

        if(btn_back != null) {
            btn_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadSelectionProcess(rb_individual.isChecked());
                }
            });
        }

        if(btn_submit != null) {
            btn_submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    continueClick();
                }
            });
        }

        if(rb_individual != null) {
            if(DeviceDataManager.Data.SelectionProcessList == null || DeviceDataManager.Data.SelectionProcessList.size() == 0) {
                DeviceDataManager.Data.SelectionProcessList = getSelectionProcessListClone();
            }

            if(DeviceDataManager.Data.SelectionProcessType == ExSelectionProcessTypes.Individual) {
                rb_individual.setChecked(true);
                loadSelectionProcess(true);
            }
            else if(DeviceDataManager.Data.SelectionProcessType == ExSelectionProcessTypes.Group) {
                rb_group.setChecked(true);
                loadSelectionProcess(false);
            }

            rb_individual.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SelectionProcessType = isChecked ? ExSelectionProcessTypes.Individual : ExSelectionProcessTypes.Group;
                    DeviceDataManager.Data.SelectionProcessList = getSelectionProcessListClone();
                    loadSelectionProcess(isChecked);
                }
            });
        }
    }
    private void loadStudentData() {
        final EditText txt_from_date = (EditText) findViewById(R.id.txt_from_date);
        final EditText txt_to_date = (EditText) findViewById(R.id.txt_to_date);

        final ExStudentDataArgs args = new ExStudentDataArgs();
        args.Start = txt_from_date.getText().toString();
        args.End = txt_to_date.getText().toString();

        if(args.Start == null || args.Start.trim().length() == 0) {
            Common.alert(UIConfigActivity.this, "Please select valid start date", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    txt_from_date.performClick();
                }
            });
            return;
        }
        else if(args.End == null || args.End.trim().length() == 0) {
            Common.alert(UIConfigActivity.this, "Please select valid end date", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    txt_to_date.performClick();
                }
            });
            return;
        }

        long startDate = 0;
        long endDate = 0;

        try {
            startDate = dateFormatter.parse(args.Start).getTime();
            endDate = dateFormatter.parse(args.End).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(startDate > endDate) {
            Common.alert(UIConfigActivity.this, "Please select valid end date", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    txt_to_date.performClick();
                }
            });
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.pnl_inputs).setVisibility(View.GONE);
                findViewById(R.id.pnl_waiting_overlay).setVisibility(View.VISIBLE);
                StudentDataHandler.loadDataFromServer(UIConfigActivity.this,
                new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.pnl_info_layout).setVisibility(View.GONE);
                            }
                        });
                        return true;
                    }
                }), args);
            }
        });
    }
    private void continueClick() {
        final RadioButton rb_individual = (RadioButton) this.findViewById(R.id.rb_individual);

        SelectionProcessType = rb_individual.isChecked() ? ExSelectionProcessTypes.Individual : ExSelectionProcessTypes.Group;

        if (rb_individual.isChecked() == true || ListMode.compareToIgnoreCase("T") == 0) {
            final ProgressDialog wait = ProgressDialog.show(UIConfigActivity.this, "", "Wait while loading groups...", true, false);
            if(rb_individual.isChecked() == false) {
                try {
                    DeviceDataManager.loadGroupList(UIConfigActivity.this, Integer.toString(SelectedCenter.ID), new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            try {
                                DeviceDataManager.saveDeviceData(UIConfigActivity.this);

                                DeviceDataManager.Data.SelectionProcessType = SelectionProcessType;
                                DeviceDataManager.Data.SelectedProgram = SelectedProgram;
                                DeviceDataManager.Data.SelectedCenter = SelectedCenter;

                                DeviceDataManager.saveLocalData(UIConfigActivity.this);
                                UIConfigActivity.this.startActivity(new Intent(UIConfigActivity.this, UIHomeActivity.class));
                                UIConfigActivity.this.finish();
                            }
                            catch(Exception ex) { }
                            wait.dismiss();
                            return true;
                        }
                    }));
                } catch(Exception ex) { wait.dismiss(); }
            }
            else {
                DeviceDataManager.Data.SelectionProcessType = SelectionProcessType;
                DeviceDataManager.Data.SelectedProgram = SelectedProgram;
                DeviceDataManager.Data.SelectedCenter = SelectedCenter;

                DeviceDataManager.saveLocalData(UIConfigActivity.this);
                wait.dismiss();
                UIConfigActivity.this.startActivity(new Intent(UIConfigActivity.this, UIHomeActivity.class));
                UIConfigActivity.this.finish();
            }
        } else {
            DeviceDataManager.Data.SelectionProcessType = SelectionProcessType;
            loadPrograms();
        }
    }
    private ArrayList<ExSelectionProcess> getSelectionProcessListClone() {
        ArrayList<ExSelectionProcess> clone = null;
        try {
            ExSelectionProcessInfo info = new ExSelectionProcessInfo();
            info.Data = DeviceDataManager.DeviceData.SelectionProcessList;
            String json = JsonUtils.getJson(info);
            info = new ExSelectionProcessInfo();
            info = new Gson().fromJson(json, ExSelectionProcessInfo.class);
            //JsonUtils.getObject(info, json);
            clone = info.Data;
        }
        catch(Exception ex) { }
        return clone;
    }
    private void loadData() {
        if(Common.isNetworkConnected(UIConfigActivity.this) == true) {
            final ProgressDialog wait = ProgressDialog.show(UIConfigActivity.this, "", "Connecting to server...", true, false);
            final UIConfigActivity pointer = this;
            DeviceDataManager.loadData(pointer, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if (msg.obj instanceof ExBaseObject) {
                        ExBaseObject object = (ExBaseObject) msg.obj;
                        wait.setMessage(object.Name);

                        if (object.ID == 1) {
                            try {
                                RadioButton rb_group = (RadioButton) findViewById(R.id.rb_group);
                                RadioButton rb_individual = (RadioButton) findViewById(R.id.rb_individual);
                                rb_group.setChecked(false);
                                //rb_individual.setChecked(false);
                                rb_individual.setChecked(true);
                                rb_individual.setSelected(true);
                                DeviceDataManager.Data.SelectionProcessType = ExSelectionProcessTypes.Individual;
                                SelectionProcessType = ExSelectionProcessTypes.Individual;
                                DeviceDataManager.Data.SelectionProcessList = getSelectionProcessListClone();
                                loadSelectionProcess(true);

                                Common.setSynchDate(UIConfigActivity.this);
                            } catch (Exception ex) {
                            }
                            wait.dismiss();
                        }
                    }
                    return true;
                }
            }));
        }
        else {
            Common.alert(UIConfigActivity.this, "Please check your internet connection!");
        }
    }
    private void loadTemplates() {
        this.ListMode = "T";
        final View btn_back = this.findViewById(R.id.btn_back);
        final ListView lvw_items = (ListView) findViewById(R.id.lvw_items);
        final TextView lbl_title = (TextView) findViewById(R.id.lbl_title);

        if(btn_back != null) {
            btn_back.setVisibility(View.VISIBLE);
        }

        DeviceDataManager.Data.SelectedGroupScoreCards = new ArrayList<>();
        if(DeviceDataManager.Data != null && DeviceDataManager.DeviceData.ProgramList != null) {
            for(ExTemplate template : DeviceDataManager.DeviceData.TemplateList) {
                if(template.IsGroup) {
                    DeviceDataManager.Data.SelectedGroupScoreCards.add(template);
                }
            }
        }

        lvw_items.setAdapter(new ArrayAdapter<ExBaseObject>(UIConfigActivity.this, R.layout.view_process_item));
        if(lbl_title != null) {
            lbl_title.setText("SELECT TEMPLATE");
        }

        if (lvw_items != null) {
            lvw_items.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return DeviceDataManager.Data.SelectedGroupScoreCards.size();
                }

                @Override
                public ExTemplate getItem(int position) {
                    return DeviceDataManager.Data.SelectedGroupScoreCards.get(position);
                }

                @Override
                public long getItemId(int position) {
                    return position;
                }

                @Override
                public View getView(int position, View view, ViewGroup parent) {
                    ExTemplate item = this.getItem(position);
                    if (view == null) {
                        view = UIConfigActivity.this.getLayoutInflater().inflate(R.layout.view_process_item, null);
                    }
                    view.setTag(item);

                    TextView lbl_text = (TextView) view.findViewById(R.id.lbl_text);
                    if (lbl_text != null) {
                        lbl_text.setText(item.Name);
                    }

                    CheckBox chk_selected = (CheckBox) view.findViewById(R.id.chk_selected);
                    if (chk_selected != null) {
                        chk_selected.setTag(item);
                        chk_selected.setChecked(item.IsSelected);
                        chk_selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                ExTemplate tag = (ExTemplate) buttonView.getTag();
                                if (tag != null) {
                                    tag.IsSelected = isChecked;
                                }
                            }
                        });
                    }

                    return view;
                }
            });
        }
    }
    private void loadPrograms() {
        this.ListMode = "P";
        final View btn_back = this.findViewById(R.id.btn_back);
        final ListView lvw_items = (ListView) findViewById(R.id.lvw_items);
        final TextView lbl_title = (TextView) findViewById(R.id.lbl_title);

        if(btn_back != null) {
            btn_back.setVisibility(View.VISIBLE);
        }

        ArrayList<ExBaseObject> programs = new ArrayList<ExBaseObject>();
        if(DeviceDataManager.Data != null && DeviceDataManager.DeviceData.ProgramList != null) {
            for(ExProgram program : DeviceDataManager.DeviceData.ProgramList) {
                programs.add(program);
            }
        }

        lvw_items.setAdapter(new ArrayAdapter<ExBaseObject>(UIConfigActivity.this, R.layout.view_program_item));
        if(lbl_title != null) {
            lbl_title.setText("SELECT PROGRAM");
        }

        if (lvw_items != null) {
            final ArrayList<ExBaseObject> listItems = programs;
            lvw_items.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return listItems.size();
                }

                @Override
                public ExBaseObject getItem(int position) {
                    return listItems.get(position);
                }

                @Override
                public long getItemId(int position) {
                    return position;
                }

                @Override
                public View getView(int position, View view, ViewGroup parent) {
                    ExBaseObject item = this.getItem(position);
                    if (view == null) {
                        view = UIConfigActivity.this.getLayoutInflater().inflate(R.layout.view_program_item, null);
                    }

                    view.setTag(item);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ExProgram tag = (ExProgram) v.getTag();
                            if (tag != null) {
                                SelectedProgram = tag;
                                loadCenters(tag);
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
    private void loadCenters(ExProgram program) {
        this.ListMode = "C";
        final View btn_back = this.findViewById(R.id.btn_back);
        final TextView lbl_title = (TextView) findViewById(R.id.lbl_title);
        final ListView lvw_items = (ListView) findViewById(R.id.lvw_items);

        if(btn_back != null) {
            btn_back.setVisibility(View.VISIBLE);
        }

        ArrayList<ExBaseObject> centers = new ArrayList<ExBaseObject>();
        if(program != null && program.Centers != null) {
            for(ExBaseObject center : program.Centers) {
                centers.add(center);
            }
        }

        lvw_items.setAdapter(new ArrayAdapter<ExBaseObject>(UIConfigActivity.this, R.layout.view_program_item));
        if(lbl_title != null) {
            lbl_title.setText("SELECT CENTER");
        }

        if (lvw_items != null) {
            final ArrayList<ExBaseObject> listItems = centers;
            lvw_items.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return listItems.size();
                }

                @Override
                public ExBaseObject getItem(int position) {
                    return listItems.get(position);
                }

                @Override
                public long getItemId(int position) {
                    return position;
                }

                @Override
                public View getView(int position, View view, ViewGroup parent) {
                    ExBaseObject item = this.getItem(position);
                    if (view == null) {
                        view = UIConfigActivity.this.getLayoutInflater().inflate(R.layout.view_program_item, null);
                    }

                    view.setTag(item);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ExBaseObject tag = (ExBaseObject) v.getTag();
                            if (tag != null) {
                                SelectedCenter = tag;
                                loadTemplates();
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
    private void loadSelectionProcess(boolean individual) {
        final RadioButton rb_group = (RadioButton) this.findViewById(R.id.rb_group);
        final RadioButton rb_individual = (RadioButton) this.findViewById(R.id.rb_individual);
        this.ListMode = "S";
        final ListView lvw_items = (ListView) findViewById(R.id.lvw_items);
        final TextView lbl_title = (TextView) findViewById(R.id.lbl_title);
        final View btn_back = this.findViewById(R.id.btn_back);

        if(btn_back != null) {
            btn_back.setVisibility(View.GONE);
        }

        lvw_items.setAdapter(new ArrayAdapter<ExSelectionProcess>(UIConfigActivity.this, R.layout.view_process_item));
        if(lbl_title != null) {
            lbl_title.setText("SELECTION PROCESS");
        }

        ArrayList<ExSelectionProcess> items = new ArrayList<ExSelectionProcess>();

        if(rb_group.isChecked() || rb_individual.isChecked()) {
            if (DeviceDataManager.Data != null && DeviceDataManager.Data.SelectionProcessList != null && DeviceDataManager.Data.SelectionProcessList.size() > 0) {
                for (ExSelectionProcess item : DeviceDataManager.Data.SelectionProcessList) {
                    if ((individual == true && item.IsGroup == false) || (individual == false && item.IsGroup == true)) {
                        items.add(item);
                    }
                }
            }
        }

        if (lvw_items != null) {
            final ArrayList<ExSelectionProcess> listItems = items;
            lvw_items.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return listItems.size();
                }

                @Override
                public ExSelectionProcess getItem(int position) {
                    return listItems.get(position);
                }

                @Override
                public long getItemId(int position) {
                    return position;
                }

                @Override
                public View getView(int position, View view, ViewGroup parent) {
                    ExSelectionProcess item = this.getItem(position);
                    if (view == null) {
                        view = UIConfigActivity.this.getLayoutInflater().inflate(R.layout.view_process_item, null);
                    }
                    view.setTag(item);

                    TextView lbl_text = (TextView) view.findViewById(R.id.lbl_text);
                    if (lbl_text != null) {
                        lbl_text.setText(item.Name);
                    }

                    CheckBox chk_selected = (CheckBox) view.findViewById(R.id.chk_selected);
                    if (chk_selected != null) {
                        chk_selected.setTag(item);
                        chk_selected.setChecked(item.IsSelected);
                        chk_selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                ExSelectionProcess tag = (ExSelectionProcess) buttonView.getTag();
                                if (tag != null) {
                                    tag.IsSelected = isChecked;
                                }
                            }
                        });
                    }

                    return view;
                }
            });
        }
    }
}
