package com.app.vst.christapp;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import database.DBInfo;
import database.DeviceDataManager;
import domain.ExBaseObject;
import domain.ExParam;
import domain.ExScore;
import domain.ExScoreCardCourse;
import domain.ExSelectionProcess;
import domain.ExStudentInfo;
import domain.ExTemplate;
import utils.Common;
import utils.ExSeekBar;
import utils.StudentReport;

public class UISingleActivity extends UIBaseActivity {
    public static UISingleActivity Instance = null;
    public static ExStudentInfo SelectedStudent;
    public static ArrayList<ExTemplate> ScoreCards = new ArrayList<>();
    public static ExTemplate ScoreCard;
    public Date Now;
    public boolean Saved = false;
    public boolean ManageState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Instance != null) {
            Instance.finish();
        }
        Instance = this;
        ScoreCard = null;
        ScoreCards = new ArrayList<>();

        super.onCreate(savedInstanceState);
        try {
            Common.loadActionBarWithTimer(this);
        } catch (Exception e) {
        }

        this.Now = new Date();
        this.loadScoreCards();
        if (this.ScoreCards != null && this.ScoreCards.size() > 0) {
            if (this.defaultScoreCard()) {
                this.init();
                /*----- State Management -----*/
                IPStateManager.loadInstance(this).loadState();
            }
        } else {
            Common.alert(UISingleActivity.this, "Selection process score card is not defined for this candidate!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UISingleActivity.this.finish();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        Common.confirm(this, "Are you sure, do you want to leave this page?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == Dialog.BUTTON_POSITIVE) {
                    onSuperBackPressed();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        this.ManageState = false;
        super.onStop();
    }

    private void onSuperBackPressed() {
        super.onBackPressed();
    }

    private void setTimerStatus(boolean status) {
        try {
            View view = this.getSupportActionBar().getCustomView();
            if (view != null) {
                CheckBox chk_action = (CheckBox) view.findViewById(R.id.chk_action);
                if (chk_action != null) {
                    if (status == true && chk_action.isChecked() == true) {
                        chk_action.setChecked(false);
                    }
                    chk_action.setChecked(status);
                }
            }
        } catch (Exception ex) {
        }
    }

    private boolean defaultScoreCard() {
        if (this.ScoreCards != null && this.ScoreCards.size() > 0) {
            for (ExTemplate template : this.ScoreCards) {
                if (isSubmitted(template) == false) {
                    setContentView(R.layout.ui_single_activity);
                    loadScoreCardInfo(template);
                    return true;
                }
            }

            Common.alert(UISingleActivity.this, "Sorry! Score card is already submitted; You cannot continue.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UISingleActivity.this.finish();
                }
            });
        }
        return false;
    }

    private boolean isSubmitted(ExTemplate template) {
        ExScore data = new ExScore();
        try {
            data.AppID = SelectedStudent.AppID;
            data.AppNo = SelectedStudent.AppNo;
            data.ScoreCardID = template.ID;
            data.GroupDetailID = 0;
        } catch (Exception ex) {
        }
        if (DBInfo.createInstance(UISingleActivity.this).isScoreCardExist(data.AppNo, data.AppID, data.ScoreCardID) == true) {
            return true;
        }
        return false;
    }

    private void init() {
        if (SelectedStudent != null) {
            TextView lbl_app_no = (TextView) this.findViewById(R.id.lbl_app_no);
            if (lbl_app_no != null) {
                lbl_app_no.setText(Integer.toString(SelectedStudent.AppNo));
            }

            TextView lbl_name = (TextView) this.findViewById(R.id.lbl_name);
            if (lbl_name != null) {
                lbl_name.setText(SelectedStudent.Name);
            }
        }

        final ImageView img_profile = (ImageView) this.findViewById(R.id.img_profile);
        img_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    StudentReport.show(UISingleActivity.this, SelectedStudent.AppNo);
                } catch (Exception ex) {
                }
            }
        });

        try {
            if (DBInfo.createInstance(this).isProfileExist(SelectedStudent.AppNo)) {
                img_profile.setImageDrawable(Common.getDrawable(this, R.drawable.img_profile));
            } else {
                img_profile.setImageDrawable(Common.getDrawable(this, R.drawable.img_dprofile));
            }
        }
        catch(Exception ex) {
        }

        final TextView lbl_total = (TextView) this.findViewById(R.id.lbl_total);
        if (lbl_total != null) {
            lbl_total.setTypeface(Common.getDigitalFontface(this));
        }

        final TextView txt_mng_pos = (TextView) this.findViewById(R.id.txt_mng_pos);
        if (txt_mng_pos != null) {
            txt_mng_pos.setTypeface(Common.getDigitalFontface(this));
        }
        final SeekBar sb_mng_pos = (SeekBar) this.findViewById(R.id.sb_mng_pos);
        sb_mng_pos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExTemplate tagInfo = (ExTemplate) sb_mng_pos.getTag();
                if (tagInfo.MngWorkExpInterval > 0) {
                    txt_mng_pos.setText(Double.toString(tagInfo.MngWorkExpInterval * sb_mng_pos.getProgress()));
                }
            }
        });
        sb_mng_pos.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ExTemplate tagInfo = (ExTemplate) sb_mng_pos.getTag();
                if (tagInfo.MngWorkExpInterval > 0) {
                    txt_mng_pos.setText(Double.toString(tagInfo.MngWorkExpInterval * sb_mng_pos.getProgress()));
                }
                return false;
            }
        });
        sb_mng_pos.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ExTemplate tagInfo = (ExTemplate) seekBar.getTag();
                if (tagInfo.MngWorkExpInterval > 0) {
                    txt_mng_pos.setText(Double.toString(tagInfo.MngWorkExpInterval * progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        final TextView txt_work_exp = (TextView) this.findViewById(R.id.txt_work_exp);
        if (txt_work_exp != null) {
            txt_work_exp.setTypeface(Common.getDigitalFontface(this));
        }
        final SeekBar sb_work_exp = (SeekBar) this.findViewById(R.id.sb_work_exp);
        sb_work_exp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExTemplate tagInfo = (ExTemplate) sb_work_exp.getTag();
                if (tagInfo.WorkExpMaxValue > 0) {
                    txt_work_exp.setText(Double.toString(tagInfo.WorkExpInterval * sb_work_exp.getProgress()));
                }
            }
        });
        sb_work_exp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ExTemplate tagInfo = (ExTemplate) sb_work_exp.getTag();
                if (tagInfo.WorkExpMaxValue > 0) {
                    txt_work_exp.setText(Double.toString(tagInfo.WorkExpInterval * sb_work_exp.getProgress()));
                }
                return false;
            }
        });
        sb_work_exp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ExTemplate tagInfo = (ExTemplate) seekBar.getTag();
                if (tagInfo.WorkExpMaxValue > 0) {
                    txt_work_exp.setText(Double.toString(tagInfo.WorkExpInterval * progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        View btn_back = this.findViewById(R.id.btn_back);
        if (btn_back != null) {
            btn_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UISingleActivity.this.startActivity(new Intent(UISingleActivity.this, UIHomeActivity.class));
                    UISingleActivity.this.finish();
                }
            });
        }

        Button btn_submit = (Button) this.findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitScoreCard();
            }
        });

        final View pnl_popup = this.findViewById(R.id.pnl_popup);
        final TextView lbl_title = (TextView) this.findViewById(R.id.lbl_title);
        final View pnl_scoresheets = this.findViewById(R.id.pnl_scoresheets);
        final ListView lvw_popup_items = (ListView) this.findViewById(R.id.lvw_popup_items);
        final View btn_exit_scorecardlist = this.findViewById(R.id.btn_exit_scorecardlist);

        if(btn_exit_scorecardlist != null) {
            btn_exit_scorecardlist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pnl_popup.setVisibility(View.GONE);
                }
            });
        }

        if (lvw_popup_items != null && ScoreCards.size() > 1) {

            lvw_popup_items.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return ScoreCards.size();
                }

                @Override
                public ExBaseObject getItem(int position) {
                    return ScoreCards.get(position);
                }

                @Override
                public long getItemId(int position) {
                    return position;
                }

                @Override
                public View getView(int position, View view, ViewGroup parent) {
                    ExBaseObject item = this.getItem(position);
                    if (view == null) {
                        view = UISingleActivity.this.getLayoutInflater().inflate(R.layout.view_scorecard_item, null);
                    }

                    view.setTag(item);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final ExTemplate template = (ExTemplate) v.getTag();
                            if (template != null) {
                                if (isSubmitted(template)) {
                                    Common.alert(UISingleActivity.this, "Sorry! Score card is already submitted; You cannot continue.");
                                    return;
                                } else {
                                    if (pnl_popup != null) {
                                        pnl_popup.setVisibility(View.GONE);
                                    }
                                    loadScoreCardInfo(template);
                                }
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
                    final View img_done = view.findViewById(R.id.img_done);
                    if (img_done != null) {
                        ExTemplate template = (ExTemplate) view.getTag();
                        img_done.setVisibility(isSubmitted(template) ? View.VISIBLE : View.GONE);
                    }

                    return view;
                }
            });

            if (pnl_scoresheets != null) {
                pnl_scoresheets.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ScoreCards == null || ScoreCards.size() == 0) {
                            Common.alert(UISingleActivity.this, "No selection process selected, Please contact your administrator!");
                        } else {
                            if (pnl_popup != null) {
                                pnl_popup.setVisibility(View.VISIBLE);
                                BaseAdapter adpater = (BaseAdapter) lvw_popup_items.getAdapter();
                                if (adpater != null) {
                                    adpater.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    private void submitScoreCard() {
        final ExScore data = new ExScore();
        try {
            data.AppID = SelectedStudent.AppID;
            data.AppNo = SelectedStudent.AppNo;
            try {
                data.UserID = Integer.parseInt(DeviceDataManager.Data.SelectedUser.ID);
            } catch (Exception e) {
            }
            data.ScoreCardID = ScoreCard.ID;
            data.GroupDetailID = 0;
        } catch (Exception ex) {
        }
        if (DBInfo.createInstance(UISingleActivity.this).isScoreCardExist(data.AppNo, data.AppID, data.ScoreCardID) == true) {
            Common.alert(UISingleActivity.this, "Sorry! Score card is already submitted; You cannot continue.");
            return;
        }

        final ProgressDialog wait = ProgressDialog.show(UISingleActivity.this, "", "Wait while submitting...", true, false);
        try {
            /*----- Loading Params ----*/
            ViewGroup pnl_params = (ViewGroup) this.findViewById(R.id.pnl_params);
            if (pnl_params != null) {
                data.Params = new ArrayList<ExBaseObject>();
                ArrayList<View> views = Common.getChildren(pnl_params);
                if (views != null) {
                    for (View view : views) {
                        if (view instanceof SeekBar) {
                            final SeekBar seekbar = (SeekBar) view;
                            ExParam tagInfo = (ExParam) seekbar.getTag();
                            data.Params.add(new ExBaseObject(tagInfo.ID, Double.toString(seekbar.getProgress() * tagInfo.Interval)));

                            try {
                                if (tagInfo.Value == -1) {
                                    wait.dismiss();
                                    Common.alert(UISingleActivity.this, "Please enter parameter value (" + tagInfo.Name + ")", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            seekbar.requestFocus();
                                        }
                                    });
                                    return;
                                }
                            } catch (Exception ex) {
                            }
                        }
                        else if (view instanceof EditText) {
                            final EditText editText = (EditText) view;
                            ExParam tagInfo = (ExParam) editText.getTag();
                            String text = editText.getText().toString();
                            if(text != null && text.length() > 0) {
                                try {
                                    double scoreValue = Double.parseDouble(text);
                                    if(scoreValue <= tagInfo.MaxValue) {
                                        data.Params.add(new ExBaseObject(tagInfo.ID, Double.toString(scoreValue)));
                                    }
                                    else {
                                        try {
                                            wait.dismiss();
                                            Common.alert(UISingleActivity.this, "Invalid value, Maximum parameter (" + tagInfo.Name + ") value is " + Integer.toString(tagInfo.MaxValue), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    editText.requestFocus();
                                                }
                                            });
                                            return;
                                        } catch (Exception ex) {
                                        }
                                    }
                                }
                                catch(Exception ex) {
                                }
                            }
                            else {
                                try {
                                    wait.dismiss();
                                    Common.alert(UISingleActivity.this, "Please enter parameter value (" + tagInfo.Name + ")", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            editText.requestFocus();
                                        }
                                    });
                                    return;
                                } catch (Exception ex) {
                                }
                            }
                        }
                    }
                }
            }

            /*----- Loading Marks -----*/
            if (ScoreCard.IsMarkPerReq == true && ScoreCard.EduMarkList != null && ScoreCard.EduMarkList.size() > 0) {
                ViewGroup pnl_marks = (ViewGroup) this.findViewById(R.id.pnl_marks);
                if (pnl_marks != null) {
                    data.Marks = new ArrayList<ExBaseObject>();
                    ArrayList<View> views = Common.getChildren(pnl_marks);
                    if (views != null) {
                        for (View view : views) {
                            if (view instanceof EditText) {
                                final EditText edittext = (EditText) view;
                                String v = edittext.getText().toString();
                                boolean invalid = (v == null || v.trim().length() == 0);
                                float rate = 0.00F;

                                try {
                                    if (invalid == false) {
                                        rate = Float.parseFloat(v.trim());
                                        invalid = (rate <= 0 || rate > 100);
                                    }
                                } catch (Exception ex) {
                                }

                                if (invalid) {
                                    wait.dismiss();
                                    Common.alert(UISingleActivity.this, "Please enter valid mark percentage", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            edittext.requestFocus();
                                        }
                                    });
                                    return;
                                }

                                ExBaseObject tagInfo = (ExBaseObject) edittext.getTag();
                                data.Marks.add(new ExBaseObject(tagInfo.ID, edittext.getText().toString()));
                            }
                        }
                    }
                }
            }

            /*----- Education Background -----*/
            if (ScoreCard.IsEduBgReq == true && ScoreCard.EduBgList != null && ScoreCard.EduBgList.size() > 0) {
                RadioGroup pnl_edu_backgrounds_grp = (RadioGroup) this.findViewById(R.id.pnl_edu_backgrounds_grp);
                if (pnl_edu_backgrounds_grp != null) {
                    Object tag = pnl_edu_backgrounds_grp.getTag();
                    if (tag != null && tag instanceof RadioButton) {
                        RadioButton button = (RadioButton) tag;
                        if (button.isChecked()) {
                            ExBaseObject tagInfo = (ExBaseObject) button.getTag();
                            data.EduBgID = tagInfo.ID;
                        }
                    }
                }

                if (data.EduBgID == 0) {
                    wait.dismiss();
                    Common.alert(UISingleActivity.this, "Please select education background!");
                    return;
                }
            }

            /*----- Commerce Background -----*/
            if (ScoreCard.IsCommerceBgReq == true) {
                ViewGroup pnl_commerce_bg = (ViewGroup) this.findViewById(R.id.pnl_commerce_bg);
                if (pnl_commerce_bg.getVisibility() == View.VISIBLE) {
                    CheckBox chk_commerce_bg = (CheckBox) this.findViewById(R.id.chk_commerce_bg);
                    if (chk_commerce_bg != null) {
                        data.IsCommerceBgExist = chk_commerce_bg.isChecked() ? "1" : "0";
                    }
                }
            }

            /*----- Backlog During Degree -----*/
            if (ScoreCard.IsBacklogReq == true) {
                ViewGroup pnl_degree_backlog = (ViewGroup) this.findViewById(R.id.pnl_degree_backlog);
                if (pnl_degree_backlog.getVisibility() == View.VISIBLE) {
                    CheckBox chk_degree_backlog = (CheckBox) this.findViewById(R.id.chk_degree_backlog);
                    if (chk_degree_backlog != null) {
                        data.IsBacklogExist = chk_degree_backlog.isChecked() ? "1" : "0";
                    }
                }
            }

            /*----- Break in Studies UG to PG -----*/
            if (ScoreCard.IsUgPgBreakReq == true) {
                ViewGroup pnl_breakin_ug_pg = (ViewGroup) this.findViewById(R.id.pnl_breakin_ug_pg);
                if (pnl_breakin_ug_pg.getVisibility() == View.VISIBLE) {
                    CheckBox chk_breakin_ug_pg = (CheckBox) this.findViewById(R.id.chk_breakin_ug_pg);
                    if (chk_breakin_ug_pg != null) {
                        data.IsUgPgBreakExist = chk_breakin_ug_pg.isChecked() ? "1" : "0";
                    }
                }
            }

            /*----- Break in Studies/Second Attempt during Class 10 to Class 12 -----*/
            if (ScoreCard.Is1012BreakReq == true) {
                ViewGroup pnl_breakin_10_12 = (ViewGroup) this.findViewById(R.id.pnl_breakin_10_12);
                if (pnl_breakin_10_12.getVisibility() == View.VISIBLE) {
                    CheckBox chk_breakin_10_12 = (CheckBox) this.findViewById(R.id.chk_breakin_10_12);
                    if (chk_breakin_10_12 != null) {
                        data.Is1012BreakExist = chk_breakin_10_12.isChecked() ? "1" : "0";
                    }
                }
            }

            /*----- Reason for Break in Studies -----*/
            if (ScoreCard.IsBreakReasonReq == true) {
                final EditText txt_break_reason = (EditText) this.findViewById(R.id.txt_break_reason);
                ViewGroup pnl_break_reason = (ViewGroup) this.findViewById(R.id.pnl_break_reason);
                if (pnl_break_reason.getVisibility() == View.VISIBLE) {
                    if (txt_break_reason != null) {
                        data.BreakInStudiesReason = txt_break_reason.getText().toString();
                    }
                }

                if (data.IsUgPgBreakExist.compareTo("1") == 0 || data.Is1012BreakExist.compareTo("1") == 0) {
                    if (data.BreakInStudiesReason == null || data.BreakInStudiesReason.trim().length() == 0) {
                        wait.dismiss();
                        Common.alert(UISingleActivity.this, "Please enter reason for break in studies!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                txt_break_reason.requestFocus();
                            }
                        });
                        return;
                    }
                }
            }

            /*----- Specialization Preferred -----*/
            if (ScoreCard.IsSpecReq == true && ScoreCard.SpecList != null && ScoreCard.SpecList.size() > 0) {
                RadioGroup pnl_specialization_grp = (RadioGroup) this.findViewById(R.id.pnl_specialization_grp);
                if (pnl_specialization_grp != null) {
                    Object tag = pnl_specialization_grp.getTag();
                    if (tag != null && tag instanceof RadioButton) {
                        RadioButton button = (RadioButton) tag;
                        if (button.isChecked()) {
                            ExBaseObject tagInfo = (ExBaseObject) button.getTag();
                            data.SpecID = tagInfo.ID;
                        }
                    }
                }

                if (data.SpecID == 0) {
                    wait.dismiss();
                    Common.alert(UISingleActivity.this, "Please select preferred specialization!");
                    return;
                }
            }

            /*----- Work Experience -----*/
            if (ScoreCard.IsWorkExpReq == true) {
                ViewGroup pnl_work_exp = (ViewGroup) this.findViewById(R.id.pnl_work_exp);
                if (pnl_work_exp.getVisibility() == View.VISIBLE) {
                    TextView txt_work_exp = (TextView) this.findViewById(R.id.txt_work_exp);
                    if (txt_work_exp != null && txt_work_exp.getText().toString().trim().length() == 0) {
                        wait.dismiss();
                        Common.alert(UISingleActivity.this, "Please enter work experience!");
                        return;
                    }

                    SeekBar sb_work_exp = (SeekBar) this.findViewById(R.id.sb_work_exp);
                    if (sb_work_exp != null) {
                        ExTemplate tagInfo = (ExTemplate) sb_work_exp.getTag();
                        data.WorkExp = tagInfo.WorkExpInterval * sb_work_exp.getProgress();
                    }
                }
            }

            /*----- Managerial Position -----*/
            if (ScoreCard.IsMngWorkExpReq == true) {
                ViewGroup pnl_mng_pos = (ViewGroup) this.findViewById(R.id.pnl_mng_pos);
                if (pnl_mng_pos.getVisibility() == View.VISIBLE) {
                    TextView txt_mng_pos = (TextView) this.findViewById(R.id.txt_mng_pos);
                    if (txt_mng_pos != null && txt_mng_pos.getText().toString().trim().length() == 0) {
                        wait.dismiss();
                        Common.alert(UISingleActivity.this, "Please enter managerial work experience!");
                        return;
                    }

                    SeekBar sb_mng_pos = (SeekBar) this.findViewById(R.id.sb_mng_pos);
                    if (sb_mng_pos != null) {
                        ExTemplate tagInfo = (ExTemplate) sb_mng_pos.getTag();
                        data.MngWorkExp = tagInfo.MngWorkExpInterval * sb_mng_pos.getProgress();
                    }
                }
            }

            /*----- Present Designation -----*/
            if (ScoreCard.IsDesignationReq == true) {
                ViewGroup pnl_designation = (ViewGroup) this.findViewById(R.id.pnl_designation);
                final EditText txt_designation = (EditText) this.findViewById(R.id.txt_designation);
                if (pnl_designation.getVisibility() == View.VISIBLE) {
                    if (txt_designation != null) {
                        data.Designation = txt_designation.getText().toString();
                    }
                }

                if (data.Designation == null || data.Designation.trim().length() == 0) {
                    wait.dismiss();
                    Common.alert(UISingleActivity.this, "Please enter designation!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            txt_designation.requestFocus();
                        }
                    });
                    return;
                }
            }

            /*----- Grooming -----*/
            if (ScoreCard.IsGroomingReq == true && ScoreCard.GroomingList != null && ScoreCard.GroomingList.size() > 0) {
                RadioGroup pnl_grooming_grp = (RadioGroup) this.findViewById(R.id.pnl_grooming_grp);
                if (pnl_grooming_grp != null) {
                    Object tag = pnl_grooming_grp.getTag();
                    if (tag != null && tag instanceof RadioButton) {
                        RadioButton button = (RadioButton) tag;
                        if (button.isChecked()) {
                            ExBaseObject tagInfo = (ExBaseObject) button.getTag();
                            data.GroomingID = tagInfo.ID;
                        }
                    }
                }

                if (data.GroomingID == 0) {
                    wait.dismiss();
                    Common.alert(UISingleActivity.this, "Please select grooming!");
                    return;
                }
            }

            /*----- Dress Code -----*/
            if (ScoreCard.IsDressCodeReq == true && ScoreCard.DressCodeList != null && ScoreCard.DressCodeList.size() > 0) {
                RadioGroup pnl_dress_code_grp = (RadioGroup) this.findViewById(R.id.pnl_dress_code_grp);
                if (pnl_dress_code_grp != null) {
                    Object tag = pnl_dress_code_grp.getTag();
                    if (tag != null && tag instanceof RadioButton) {
                        RadioButton button = (RadioButton) tag;
                        if (button.isChecked()) {
                            ExBaseObject tagInfo = (ExBaseObject) button.getTag();
                            data.DressCodeID = tagInfo.ID;
                        }
                    }
                }

                if (data.DressCodeID == 0) {
                    wait.dismiss();
                    Common.alert(UISingleActivity.this, "Please select dress code!");
                    return;
                }
            }

            /*----- Panelist Comments -----*/
            if (ScoreCard.IsCommentReq == true) {
                Spinner sp_comments = (Spinner) this.findViewById(R.id.sp_comments);
                if (sp_comments != null) {
                    Object selectedItem = sp_comments.getSelectedItem();
                    if (selectedItem != null && selectedItem instanceof ExBaseObject) {
                        data.CommentID = ((ExBaseObject) selectedItem).ID;
                    }
                }

                if (data.CommentID == 0) {
                    wait.dismiss();
                    Common.alert(UISingleActivity.this, "Please select panelist comment!");
                    return;
                }
            }

            /*----- Additional Comments -----*/
            if (ScoreCard.IsExCommentReq == true) {
                ViewGroup pnl_extra_comments = (ViewGroup) this.findViewById(R.id.pnl_extra_comments);
                EditText txt_extra_comments = (EditText) this.findViewById(R.id.txt_extra_comments);
                if (pnl_extra_comments.getVisibility() == View.VISIBLE) {
                    if (txt_extra_comments != null) {
                        data.ExComment = txt_extra_comments.getText().toString();
                    }
                }
            }

            /*----- Saving Data to Device Memory -----*/
            this.resetAndSetEllapsedTime();
            try {
                Common.confirm(UISingleActivity.this, "Do you want to submit the score card?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Dialog.BUTTON_POSITIVE == which) {
                            if (DBInfo.createInstance(UISingleActivity.this).isScoreCardExist(data.AppNo, data.AppID, data.ScoreCardID)) {
                                wait.dismiss();
                                Saved = true;
                                Common.alert(UISingleActivity.this, "Sorry! Score card is already submitted; You cannot continue.");
                            } else {
                                data.TotalTimeTaken = ScoreCard.TotalTimeTaken;
                                DBInfo.createInstance(UISingleActivity.this).saveScoreCard(data);
                                wait.dismiss();
                                Saved = true;
                                    /*----- Remove The State Information -----*/
                                IPStateManager.loadInstance(UISingleActivity.this).removeState();
                                Common.alert(UISingleActivity.this, "Scorecard successfully submitted.", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (ScoreCards.size() == 1) {
                                            UISingleActivity.this.finish();
                                        } else {
                                            showScoreCardPopup();
                                        }
                                    }
                                });
                            }
                        } else {
                            wait.dismiss();
                        }
                    }
                });
            } catch (Exception ex) {
                wait.dismiss();
            }
        } catch (Exception ex) {
            wait.dismiss();
        }
    }

    private void showScoreCardPopup() {
        if (this.ScoreCards != null && this.ScoreCards.size() > 0) {
            for (ExTemplate template : this.ScoreCards) {
                if (isSubmitted(template) == false) {
                    View pnl_scoresheets = findViewById(R.id.pnl_scoresheets);
                    pnl_scoresheets.performClick();
                    return;
                }
            }
        }

        UISingleActivity.this.finish();
    }

    private void clearFields() {
        TextView lbl_total = (TextView) this.findViewById(R.id.lbl_total);
        if (lbl_total != null) {
            lbl_total.setText("0.00");
        }
        ViewGroup pnl_params = (ViewGroup) this.findViewById(R.id.pnl_params);
        if (pnl_params != null) {
            pnl_params.removeAllViews();
        }

        /*----- Education Background Information -----*/
        ViewGroup pnl_marks = (ViewGroup) this.findViewById(R.id.pnl_marks);
        if (pnl_marks != null) {
            pnl_marks.removeAllViews();
        }

        ViewGroup pnl_edu_backgrounds_grp = (ViewGroup) this.findViewById(R.id.pnl_edu_backgrounds_grp);
        if (pnl_edu_backgrounds_grp != null) {
            pnl_edu_backgrounds_grp.removeAllViews();
        }

        CheckBox chk_commerce_bg = (CheckBox) this.findViewById(R.id.chk_commerce_bg); //pnl_commerce_bg
        if (chk_commerce_bg != null) {
            chk_commerce_bg.setChecked(false);
        }

        CheckBox chk_degree_backlog = (CheckBox) this.findViewById(R.id.chk_degree_backlog); //pnl_degree_backlog
        if (chk_degree_backlog != null) {
            chk_degree_backlog.setChecked(false);
        }

        CheckBox chk_breakin_ug_pg = (CheckBox) this.findViewById(R.id.chk_breakin_ug_pg); //pnl_breakin_ug_pg
        if (chk_breakin_ug_pg != null) {
            chk_breakin_ug_pg.setChecked(false);
        }

        CheckBox chk_breakin_10_12 = (CheckBox) this.findViewById(R.id.chk_breakin_10_12); //pnl_breakin_10_12
        if (chk_breakin_10_12 != null) {
            chk_breakin_10_12.setChecked(false);
        }

        EditText txt_break_reason = (EditText) this.findViewById(R.id.txt_break_reason); //pnl_break_reason
        if (txt_break_reason != null) {
            txt_break_reason.setText("");
        }

        ViewGroup pnl_specialization_grp = (ViewGroup) this.findViewById(R.id.pnl_specialization_grp);
        if (pnl_specialization_grp != null) {
            pnl_specialization_grp.removeAllViews();
        }

        /*----- Work Experience Information -----*/
        TextView txt_work_exp = (TextView) this.findViewById(R.id.txt_work_exp);
        if (txt_work_exp != null) {
            txt_work_exp.setText("");
        }

        SeekBar sb_work_exp = (SeekBar) this.findViewById(R.id.sb_work_exp);
        if (sb_work_exp != null) {
            sb_work_exp.setProgress(0);
        }

        TextView txt_mng_pos = (TextView) this.findViewById(R.id.txt_mng_pos);
        if (txt_mng_pos != null) {
            txt_mng_pos.setText("");
        }

        SeekBar sb_mng_pos = (SeekBar) this.findViewById(R.id.sb_mng_pos);
        if (sb_mng_pos != null) {
            sb_mng_pos.setProgress(0);
        }

        EditText txt_designation = (EditText) this.findViewById(R.id.txt_designation);
        if (txt_designation != null) {
            txt_designation.setText("");
        }

        /*----- Comments -----*/
        ViewGroup pnl_grooming_grp = (ViewGroup) this.findViewById(R.id.pnl_grooming_grp);
        if (pnl_grooming_grp != null) {
            pnl_grooming_grp.removeAllViews();
        }

        ViewGroup pnl_dress_code_grp = (ViewGroup) this.findViewById(R.id.pnl_dress_code_grp);
        if (pnl_dress_code_grp != null) {
            pnl_dress_code_grp.removeAllViews();
        }

        Spinner sp_comments = (Spinner) this.findViewById(R.id.sp_comments);
        if (sp_comments != null) {
            sp_comments.setSelection(0);
        }

        EditText txt_extra_comments = (EditText) this.findViewById(R.id.txt_extra_comments);
        if (txt_extra_comments != null) {
            txt_extra_comments.setText("");
        }
    }

    private void loadScoreCards() {
        //loadScoreCardInfo(template);
        ArrayList<ExTemplate> items = new ArrayList<>();
        if (DeviceDataManager.DeviceData != null) {
            if (DeviceDataManager.DeviceData.ScoreCardCourseList != null && DeviceDataManager.DeviceData.ScoreCardCourseList.size() > 0) {
                if (DeviceDataManager.Data != null && DeviceDataManager.Data.SelectionProcessList != null && DeviceDataManager.Data.SelectionProcessList.size() > 0) {
                    for (ExSelectionProcess process : DeviceDataManager.Data.SelectionProcessList) {
                        if (process.IsSelected == true) {
                            for (ExScoreCardCourse scorecardcourse : DeviceDataManager.DeviceData.ScoreCardCourseList) {
                                if (scorecardcourse.CourseID == UISingleActivity.SelectedStudent.CourseID && scorecardcourse.ProcessID == process.ID) {
                                    if (DeviceDataManager.DeviceData.TemplateList != null) {
                                        for (ExTemplate template : DeviceDataManager.DeviceData.TemplateList) {
                                            if (template.ID == scorecardcourse.ScoreCardID) {
                                                template.SelectionProcess = process;
                                                template.TotalTimeTaken = 0;
                                                items.add(template);
                                                break;
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        UISingleActivity.ScoreCards = items;
    }

    private void resetAndSetEllapsedTime() {
        try {
            if (ScoreCard != null) {
                Date oldNow = this.Now;
                this.Now = new Date();
                ScoreCard.TotalTimeTaken += ((int) (Math.abs(this.Now.getTime() - oldNow.getTime()) / 1000));
            }
        }
        catch(Exception ex) { }
    }

    private void loadScoreCardInfo(ExTemplate template) {
        this.resetAndSetEllapsedTime();
        if (ScoreCard == null || ScoreCard.ID != template.ID) {
            this.Now = new Date();
            Saved = false;
            this.ManageState = false;
            ScoreCard = template;

            TextView lbl_title = (TextView) this.findViewById(R.id.lbl_title);
            if (lbl_title != null) {
                if (template.SelectionProcess != null) {
                    lbl_title.setText(template.SelectionProcess.Name);
                } else {
                    lbl_title.setText("SCORE SHEET");
                }
            }

            this.clearFields();
            this.loadTemplateParametersSection(template);
            this.loadTemplateEducationBackgroundSection(template);
            this.loadTemplateExperienceSection(template);
            this.loadTemplateCommentSection(template);

            /*----- Loading Previous State -----*/
            IPStateManager.loadInstance(this).loadState();
            this.ManageState = true;

            this.setTimerStatus(true);
        }
    }

    private void loadTemplateParametersSection(ExTemplate template) {
        //1: Score sheet parameters
        ViewGroup pnl_params = (ViewGroup) this.findViewById(R.id.pnl_params);
        ViewGroup pnl_total = (ViewGroup) this.findViewById(R.id.pnl_total);
        if (template.ParamList != null && template.ParamList.size() > 0) {
            pnl_total.setVisibility(View.VISIBLE);
            pnl_params.setVisibility(View.VISIBLE);
            pnl_params.removeAllViews();
            loadParams(pnl_params, template.ParamList);
        } else {
            pnl_params.setVisibility(View.GONE);
            pnl_total.setVisibility(View.GONE);
        }
    }

    private void loadTemplateEducationBackgroundSection(ExTemplate template) {
        View pnl_edu_bg_section = findViewById(R.id.pnl_edu_bg_section);
        if ((template.IsMarkPerReq == true && template.EduMarkList != null && template.EduMarkList.size() > 0) ||
                (template.IsEduBgReq == true && template.EduBgList != null && template.EduBgList.size() > 0) ||
                (template.IsBacklogReq == true) || (template.IsUgPgBreakReq == true) || (template.Is1012BreakReq == true) ||
                (template.IsBreakReasonReq == true) || (template.IsCommerceBgReq == true) ||
                (template.IsSpecReq == true && template.SpecList != null && template.SpecList.size() > 0)) {
            pnl_edu_bg_section.setVisibility(View.VISIBLE);
        } else {
            pnl_edu_bg_section.setVisibility(View.GONE);
        }

        /*----- 2: Percentage of marks -----*/
        ViewGroup pnl_mark_panel = (ViewGroup) this.findViewById(R.id.pnl_mark_panel);
        ViewGroup pnl_marks = (ViewGroup) this.findViewById(R.id.pnl_marks);
        if (template.IsMarkPerReq == true && template.EduMarkList != null && template.EduMarkList.size() > 0) {
            pnl_mark_panel.setVisibility(View.VISIBLE);
            pnl_marks.removeAllViews();
            loadQualifications(pnl_marks, template.EduMarkList);
        } else {
            pnl_mark_panel.setVisibility(View.GONE);
        }

        /*----- 3: Educational background -----*/
        ViewGroup pnl_edu_backgrounds = (ViewGroup) this.findViewById(R.id.pnl_edu_backgrounds);
        if (template.IsEduBgReq == true && template.EduBgList != null && template.EduBgList.size() > 0) {
            pnl_edu_backgrounds.setVisibility(View.VISIBLE);
            RadioGroup pnl_edu_backgrounds_grp = (RadioGroup) this.findViewById(R.id.pnl_edu_backgrounds_grp);
            pnl_edu_backgrounds_grp.removeAllViews();
            loadOptions(pnl_edu_backgrounds_grp, template.EduBgList);
        } else {
            pnl_edu_backgrounds.setVisibility(View.GONE);
        }

        /*----- 4: Backlog During Degree -----*/
        ViewGroup pnl_degree_backlog = (ViewGroup) this.findViewById(R.id.pnl_degree_backlog);
        if (template.IsBacklogReq == true) {
            pnl_degree_backlog.setVisibility(View.VISIBLE);
        } else {
            pnl_degree_backlog.setVisibility(View.GONE);
        }

        /*----- 5: Break in Studies UG to PG -----*/
        ViewGroup pnl_breakin_ug_pg = (ViewGroup) this.findViewById(R.id.pnl_breakin_ug_pg);
        if (template.IsUgPgBreakReq == true) {
            pnl_breakin_ug_pg.setVisibility(View.VISIBLE);
        } else {
            pnl_breakin_ug_pg.setVisibility(View.GONE);
        }

        /*----- 6: Break in Studies/Second Attempt during Class 10 to Class 12 -----*/
        ViewGroup pnl_breakin_10_12 = (ViewGroup) this.findViewById(R.id.pnl_breakin_10_12);
        if (template.Is1012BreakReq == true) {
            pnl_breakin_10_12.setVisibility(View.VISIBLE);
        } else {
            pnl_breakin_10_12.setVisibility(View.GONE);
        }

        /*----- 7: Reason for Break in Studies -----*/
        ViewGroup pnl_break_reason = (ViewGroup) this.findViewById(R.id.pnl_break_reason);
        if (template.IsBreakReasonReq == true) {
            pnl_break_reason.setVisibility(View.VISIBLE);
        } else {
            pnl_break_reason.setVisibility(View.GONE);
        }

        /*----- 8: Commerce Background -----*/
        ViewGroup pnl_commerce_bg = (ViewGroup) this.findViewById(R.id.pnl_commerce_bg);
        if (template.IsCommerceBgReq == true) {
            pnl_commerce_bg.setVisibility(View.VISIBLE);
        } else {
            pnl_commerce_bg.setVisibility(View.GONE);
        }

        /*----- 9: Specialization Preferred -----*/
        ViewGroup pnl_specialization = (ViewGroup) this.findViewById(R.id.pnl_specialization);
        if (template.IsSpecReq == true && template.SpecList != null && template.SpecList.size() > 0) {
            pnl_specialization.setVisibility(View.VISIBLE);
            RadioGroup pnl_specialization_grp = (RadioGroup) this.findViewById(R.id.pnl_specialization_grp);
            pnl_specialization_grp.removeAllViews();
            loadOptions(pnl_specialization_grp, template.SpecList);
        } else {
            pnl_specialization.setVisibility(View.GONE);
        }
    }

    private void loadTemplateExperienceSection(ExTemplate template) {
        View pnl_wrk_exp_section = findViewById(R.id.pnl_wrk_exp_section);
        if ((template.IsWorkExpReq == true) || (template.IsMngWorkExpReq == true) || (template.IsDesignationReq == true)) {
            pnl_wrk_exp_section.setVisibility(View.VISIBLE);
        } else {
            pnl_wrk_exp_section.setVisibility(View.GONE);
        }

        /*----- 10: Work Experience -----*/
        ViewGroup pnl_work_exp = (ViewGroup) this.findViewById(R.id.pnl_work_exp);
        if (template.IsWorkExpReq == true && template.WorkExpMaxValue > 0) {
            pnl_work_exp.setVisibility(View.VISIBLE);
            SeekBar sb_work_exp = (SeekBar) this.findViewById(R.id.sb_work_exp);
            if (template.WorkExpInterval > 0) {
                sb_work_exp.setMax((int) (template.WorkExpMaxValue / template.WorkExpInterval));
            } else {
                sb_work_exp.setMax(template.WorkExpMaxValue);
            }
            sb_work_exp.setTag(template);
        } else {
            pnl_work_exp.setVisibility(View.GONE);
        }

        /*----- 11: Managerial Position -----*/
        ViewGroup pnl_mng_pos = (ViewGroup) this.findViewById(R.id.pnl_mng_pos);
        if (template.IsMngWorkExpReq == true && template.MngWorkExpMaxValue > 0) {
            pnl_mng_pos.setVisibility(View.VISIBLE);
            SeekBar sb_mng_pos = (SeekBar) this.findViewById(R.id.sb_mng_pos);
            if (template.MngWorkExpInterval > 0) {
                sb_mng_pos.setMax((int) (template.MngWorkExpMaxValue / template.MngWorkExpInterval));
            } else {
                sb_mng_pos.setMax(template.MngWorkExpMaxValue);
            }
            sb_mng_pos.setTag(template);
        } else {
            pnl_mng_pos.setVisibility(View.GONE);
        }

        /*----- 12: Present Designation -----*/
        ViewGroup pnl_designation = (ViewGroup) this.findViewById(R.id.pnl_designation);
        if (template.IsDesignationReq == true) {
            pnl_designation.setVisibility(View.VISIBLE);
        } else {
            pnl_designation.setVisibility(View.GONE);
        }
    }

    private void loadTemplateCommentSection(ExTemplate template) {
        View pnl_comment_section = findViewById(R.id.pnl_comment_section);
        if ((template.IsGroomingReq == true && template.GroomingList != null && template.GroomingList.size() > 0) ||
                (template.IsDressCodeReq == true && template.DressCodeList != null && template.DressCodeList.size() > 0) ||
                (template.IsCommentReq == true) || (template.IsExCommentReq == true)) {
            pnl_comment_section.setVisibility(View.VISIBLE);
        } else {
            pnl_comment_section.setVisibility(View.GONE);
        }

        /*----- 13: Grooming -----*/
        ViewGroup pnl_grooming = (ViewGroup) this.findViewById(R.id.pnl_grooming);
        if (template.IsGroomingReq == true && template.GroomingList != null && template.GroomingList.size() > 0) {
            pnl_grooming.setVisibility(View.VISIBLE);
            RadioGroup pnl_grooming_grp = (RadioGroup) this.findViewById(R.id.pnl_grooming_grp);
            pnl_grooming_grp.removeAllViews();
            loadOptions(pnl_grooming_grp, template.GroomingList);
        } else {
            pnl_grooming.setVisibility(View.GONE);
        }

        /*----- 14: Dress Code -----*/
        ViewGroup pnl_dress_code = (ViewGroup) this.findViewById(R.id.pnl_dress_code);
        if (template.IsDressCodeReq == true && template.DressCodeList != null && template.DressCodeList.size() > 0) {
            pnl_dress_code.setVisibility(View.VISIBLE);
            RadioGroup pnl_dress_code_grp = (RadioGroup) this.findViewById(R.id.pnl_dress_code_grp);
            pnl_dress_code_grp.removeAllViews();
            loadOptions(pnl_dress_code_grp, template.DressCodeList);
        } else {
            pnl_dress_code.setVisibility(View.GONE);
        }

        /*----- 15: Panelist Comments -----*/
        ViewGroup pnl_comments = (ViewGroup) this.findViewById(R.id.pnl_comments);
        if (template.IsCommentReq == true && template.CommentList != null && template.CommentList.size() > 0) {
            pnl_comments.setVisibility(View.VISIBLE);

            Spinner sp_comments = (Spinner) this.findViewById(R.id.sp_comments);
            ArrayList<ExBaseObject> comments = new ArrayList<>();
            comments.add(new ExBaseObject(0, "-- Select --"));
            for (ExBaseObject comment : template.CommentList) {
                comments.add(comment);
            }
            sp_comments.setAdapter(new ArrayAdapter<ExBaseObject>(UISingleActivity.this, android.R.layout.simple_spinner_dropdown_item, comments));
        } else {
            pnl_comments.setVisibility(View.GONE);
        }

        /*----- 16: Additional Comments -----*/
        ViewGroup pnl_extra_comments = (ViewGroup) this.findViewById(R.id.pnl_extra_comments);
        if (template.IsExCommentReq == true) {
            pnl_extra_comments.setVisibility(View.VISIBLE);
        } else {
            pnl_extra_comments.setVisibility(View.GONE);
        }
    }

    private void loadOptions(final RadioGroup radioGroup, ArrayList<ExBaseObject> items) {
        for (ExBaseObject item : items) {
            /*----- Loading Button -----*/
            RadioButton button = new RadioButton(this);
            button.setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT));
            button.setButtonDrawable(Common.getDrawable(this, R.drawable.dr_radio_button));
            button.setTag(item);
            button.setText(item.Name);
            button.setTextColor(Color.parseColor("#555555"));
            button.setTextSize(15);
            button.setPadding(0, 5, 5, 5);
            button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked == true) {
                        radioGroup.setTag(buttonView);
                    }
                }
            });
            button.setMinimumWidth(110);
            radioGroup.addView(button);
        }
    }

    private void loadQualifications(ViewGroup controlPanel, ArrayList<ExBaseObject> items) {
        /*----- Loading Children -----*/
        for (ExBaseObject item : items) {
            LinearLayout childLayout = new LinearLayout(this);
            childLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            childLayout.setOrientation(LinearLayout.VERTICAL);
            controlPanel.addView(childLayout);

            /*----- Qualification Desc -----*/
            TextView qualificationDesc = new TextView(this);
            childLayout.addView(qualificationDesc);

            qualificationDesc.setLayoutParams(new LinearLayout.LayoutParams(120, LinearLayout.LayoutParams.WRAP_CONTENT));
            qualificationDesc.setTextColor(Color.parseColor("#555555"));
            qualificationDesc.setTextSize(15);
            qualificationDesc.setGravity(Gravity.CENTER);
            ((LinearLayout.LayoutParams) qualificationDesc.getLayoutParams()).setMargins(5, 5, 5, 5);
            qualificationDesc.setTag(item.ID);
            qualificationDesc.setText(item.Name);

            /*----- Qualification Desc -----*/
            EditText qualificationEdit = new EditText(this);
            childLayout.addView(qualificationEdit);

            qualificationEdit.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 40));
            qualificationEdit.setTextColor(Color.parseColor("#000000"));
            qualificationEdit.setTextSize(15);
            qualificationEdit.setPadding(10, 10, 10, 10);
            ((LinearLayout.LayoutParams) qualificationEdit.getLayoutParams()).setMargins(5, 5, 5, 5);
            qualificationEdit.setBackgroundResource(R.drawable.dr_edittext);
            qualificationEdit.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
            qualificationEdit.setTag(item);
            qualificationEdit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

            int maxLength = 6;
            InputFilter[] FilterArray = new InputFilter[1];
            FilterArray[0] = new InputFilter.LengthFilter(maxLength);
            qualificationEdit.setFilters(FilterArray);
        }
    }

    private void setParamsTotal() {
        TextView lbl_total = (TextView) this.findViewById(R.id.lbl_total);
        ViewGroup pnl_params = (ViewGroup) this.findViewById(R.id.pnl_params);
        double value = 0.00F;
        if (pnl_params != null) {
            ArrayList<View> views = Common.getChildren(pnl_params);
            if (views != null) {
                for (View view : views) {
                    if (view instanceof SeekBar) {
                        SeekBar seekbar = (SeekBar) view;
                        ExParam tagInfo = (ExParam) seekbar.getTag();
                        value += (seekbar.getProgress() * tagInfo.Interval);
                    }
                    /*----- NEW SECTION -----*/
                    else if(view instanceof EditText) {
                        EditText editText = (EditText) view;
                        String text = editText.getText().toString();
                        if(text != null && text.length() > 0) {
                            try {
                                double scoreValue = Double.parseDouble(text);
                                value += scoreValue;
                            }
                            catch(Exception ex) {
                            }
                        }
                    }
                }
            }
        }
        lbl_total.setText(Double.toString(value));
    }

    private void loadParams(ViewGroup controlPanel, ArrayList<ExParam> params) {
        for (ExParam param : params) {
            /*----- Score Card Parameters -----*/
            LinearLayout paramPanel = new LinearLayout(this);
            paramPanel.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            paramPanel.setOrientation(LinearLayout.VERTICAL);
            controlPanel.addView(paramPanel);

            RelativeLayout subLayout = new RelativeLayout(this);
            paramPanel.addView(subLayout);
            subLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            /*----- Param Name -----*/
            TextView txtDesc = new TextView(this);
            subLayout.addView(txtDesc);

            txtDesc.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            txtDesc.setCompoundDrawablesWithIntrinsicBounds(Common.getDrawable(this, R.drawable.img_bullet), null, null, null);
            txtDesc.setCompoundDrawablePadding(5);
            txtDesc.setPadding(0, 0, 50, 0);
            txtDesc.setTextColor(Color.parseColor("#555555"));
            txtDesc.setTextSize(15);
            ((RelativeLayout.LayoutParams) txtDesc.getLayoutParams()).setMargins(5, 5, 5, 5);
            txtDesc.setTag(param.ID);
            txtDesc.setText(param.Name);

            /*----- Param Value -----*/
            final TextView txtValue = new TextView(this);
            subLayout.addView(txtValue);

            txtValue.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            txtValue.setTextColor(Color.parseColor("#112233"));
            txtValue.setTextSize(24);
            txtValue.setTypeface(Common.getDigitalFontface(UISingleActivity.this)); //Typeface.DEFAULT_BOLD
            ((RelativeLayout.LayoutParams) txtValue.getLayoutParams()).setMargins(4, 4, 10, 4);
            ((RelativeLayout.LayoutParams) txtValue.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            ((RelativeLayout.LayoutParams) txtValue.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);
            //txtValue.setText("0.00");

            /*----- NEW SECTION -----*/
            int maxvalue = 0;
            if (param.Interval > 0) {
                maxvalue = (int) (param.MaxValue / param.Interval);
            } else {
                maxvalue = param.MaxValue;
            }

            if(maxvalue > 20) {
                /*----- NEW SECTION -----*/
                final EditText editText = new EditText(this);
                controlPanel.addView(editText);

                editText.setLayoutParams(new LinearLayout.LayoutParams(200, 40));
                editText.setHint("Maximum Value : " + Integer.toString(param.MaxValue));
                editText.setTextColor(Color.parseColor("#000000"));
                editText.setTextSize(15);
                editText.setPadding(10, 10, 10, 10);
                ((LinearLayout.LayoutParams) editText.getLayoutParams()).setMargins(5, 5, 5, 5);
                editText.setBackgroundResource(R.drawable.dr_edittext);
                editText.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
                editText.setTag(param);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        setParamsTotal();
                    }
                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
            }
            else {
                /*----- Slider Control -----*/
                final ExSeekBar sbValue = new ExSeekBar(this);
                controlPanel.addView(sbValue);

                sbValue.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                ((LinearLayout.LayoutParams) sbValue.getLayoutParams()).setMargins(5, 5, 5, 5);
                sbValue.setMax(maxvalue);
                sbValue.setTag(param);
                sbValue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ExParam tagInfo = (ExParam) sbValue.getTag();
                        txtValue.setText(Double.toString(sbValue.getProgress() * tagInfo.Interval));
                        tagInfo.Value = sbValue.getProgress();
                        setParamsTotal();
                    }
                });
                sbValue.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        ExParam tagInfo = (ExParam) sbValue.getTag();
                        txtValue.setText(Double.toString(sbValue.getProgress() * tagInfo.Interval));
                        tagInfo.Value = sbValue.getProgress();
                        setParamsTotal();
                        return false;
                    }
                });
                sbValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        ExParam tagInfo = (ExParam) seekBar.getTag();
                        txtValue.setText(Double.toString(progress * tagInfo.Interval));
                        setParamsTotal();
                        tagInfo.Value = sbValue.getProgress();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
            }
        }
    }
}
