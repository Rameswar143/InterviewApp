package com.app.vst.christapp;

import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import domain.ExBaseObject;
import domain.ExParam;
import domain.ExScore;
import domain.ExTemplate;
import utils.Common;

public class IPStateManager {
    private static IPStateManager Instance = null;
    private UISingleActivity ParentActivity = null;
    private ArrayList<ExScore> ScoreCards = new ArrayList<ExScore>();
    private String FILE_NAME = "IP_STATE_DATA";

    private IPStateManager(UISingleActivity parentActivity) {
        this.ParentActivity = parentActivity;
        this.ScoreCards = (ArrayList<ExScore>) Common.readObjectFromDevice(this.ParentActivity, FILE_NAME);
        if (this.ScoreCards == null) {
            this.ScoreCards = new ArrayList<ExScore>();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (ParentActivity.ManageState == true) {
                        if (ParentActivity.SelectedStudent != null && ParentActivity.ScoreCard != null) {
                            if (ParentActivity.Saved == false) {
                                try {
                                    ExScore score = readState();
                                    removeState();
                                    ScoreCards.add(score);
                                    Common.writeObjectToDevice(ParentActivity, FILE_NAME, ScoreCards);
                                } catch (Exception ex) {
                                }
                            }
                        }
                        try {
                            Thread.currentThread().sleep(500);
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }).start();
    }
    public static IPStateManager loadInstance(UISingleActivity parentActivity) {
        if(IPStateManager.Instance == null) { IPStateManager.Instance = new IPStateManager(parentActivity); }
        else { IPStateManager.Instance.ParentActivity = parentActivity; }
        return IPStateManager.Instance;
    }
    public void removeState() {
        try {
            ExScore existing = null;
            for (ExScore scorecard : ScoreCards) {
                if (scorecard.ScoreCardID == ParentActivity.ScoreCard.ID && scorecard.AppNo == ParentActivity.SelectedStudent.AppNo) {
                    existing = scorecard;
                    break;
                }
            }
            if(existing != null) {
                ScoreCards.remove(existing);
                Common.writeObjectToDevice(ParentActivity, FILE_NAME, ScoreCards);
            }
        }
        catch(Exception ex) { }
    }
    public ExScore readState() {
        ExScore data = new ExScore();
        try {
            try {
                data.AppID = ParentActivity.SelectedStudent.AppID;
                data.AppNo = ParentActivity.SelectedStudent.AppNo;
                data.ScoreCardID = ParentActivity.ScoreCard.ID;
                data.GroupDetailID = 0;
                data.TotalTimeTaken = ParentActivity.ScoreCard.TotalTimeTaken;
            }
            catch(Exception ex) { }

            try {
                /*----- Loading Params ----*/
                ViewGroup pnl_params = (ViewGroup) ParentActivity.findViewById(R.id.pnl_params);
                if (pnl_params != null) {
                    data.Params = new ArrayList<ExBaseObject>();
                    ArrayList<View> views = Common.getChildren(pnl_params);
                    if (views != null) {
                        for (View view : views) {
                            if (view instanceof SeekBar) {
                                final SeekBar seekbar = (SeekBar) view;
                                ExParam tagInfo = (ExParam) seekbar.getTag();
                                try {
                                    if(tagInfo.Value > -1) {
                                        data.Params.add(new ExBaseObject(tagInfo.ID, Double.toString(seekbar.getProgress())));
                                    }
                                }
                                catch(Exception ex) { }
                            }
                            else if (view instanceof EditText) {
                                final EditText editText = (EditText) view;
                                ExParam tagInfo = (ExParam) editText.getTag();
                                String text = editText.getText().toString();
                                if(text != null && text.length() > 0) {
                                    try {
                                        double scoreValue = Double.parseDouble(text);
                                        data.Params.add(new ExBaseObject(tagInfo.ID, Double.toString(scoreValue)));
                                    }
                                    catch(Exception ex) {
                                    }
                                }
                            }
                        }
                    }
                }

                /*----- Loading Marks -----*/
                if (ParentActivity.ScoreCard.IsMarkPerReq == true && ParentActivity.ScoreCard.EduMarkList != null && ParentActivity.ScoreCard.EduMarkList.size() > 0) {
                    ViewGroup pnl_marks = (ViewGroup) ParentActivity.findViewById(R.id.pnl_marks);
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

                                    if (invalid == false) {
                                        ExBaseObject tagInfo = (ExBaseObject) edittext.getTag();
                                        data.Marks.add(new ExBaseObject(tagInfo.ID, edittext.getText().toString()));
                                    }
                                }
                            }
                        }
                    }
                }

                /*----- Education Background -----*/
                if (ParentActivity.ScoreCard.IsEduBgReq == true && ParentActivity.ScoreCard.EduBgList != null && ParentActivity.ScoreCard.EduBgList.size() > 0) {
                    RadioGroup pnl_edu_backgrounds_grp = (RadioGroup) ParentActivity.findViewById(R.id.pnl_edu_backgrounds_grp);
                    if (pnl_edu_backgrounds_grp != null) {
                        Object tag = pnl_edu_backgrounds_grp.getTag();
                        if(tag != null && tag instanceof RadioButton) {
                            RadioButton button = (RadioButton) tag;
                            if(button.isChecked()) {
                                ExBaseObject tagInfo = (ExBaseObject) button.getTag();
                                data.EduBgID = tagInfo.ID;
                            }
                        }
                    }
                }

                /*----- Commerce Background -----*/
                if (ParentActivity.ScoreCard.IsCommerceBgReq == true) {
                    ViewGroup pnl_commerce_bg = (ViewGroup) ParentActivity.findViewById(R.id.pnl_commerce_bg);
                    if (pnl_commerce_bg.getVisibility() == View.VISIBLE) {
                        CheckBox chk_commerce_bg = (CheckBox) ParentActivity.findViewById(R.id.chk_commerce_bg);
                        if (chk_commerce_bg != null) {
                            data.IsCommerceBgExist = chk_commerce_bg.isChecked() ? "1" : "0";
                        }
                    }
                }

                /*----- Backlog During Degree -----*/
                if (ParentActivity.ScoreCard.IsBacklogReq == true) {
                    ViewGroup pnl_degree_backlog = (ViewGroup) ParentActivity.findViewById(R.id.pnl_degree_backlog);
                    if (pnl_degree_backlog.getVisibility() == View.VISIBLE) {
                        CheckBox chk_degree_backlog = (CheckBox) ParentActivity.findViewById(R.id.chk_degree_backlog);
                        if (chk_degree_backlog != null) {
                            data.IsBacklogExist = chk_degree_backlog.isChecked() ? "1" : "0";
                        }
                    }
                }

                /*----- Break in Studies UG to PG -----*/
                if (ParentActivity.ScoreCard.IsUgPgBreakReq == true) {
                    ViewGroup pnl_breakin_ug_pg = (ViewGroup) ParentActivity.findViewById(R.id.pnl_breakin_ug_pg);
                    if (pnl_breakin_ug_pg.getVisibility() == View.VISIBLE) {
                        CheckBox chk_breakin_ug_pg = (CheckBox) ParentActivity.findViewById(R.id.chk_breakin_ug_pg);
                        if (chk_breakin_ug_pg != null) {
                            data.IsUgPgBreakExist = chk_breakin_ug_pg.isChecked() ? "1" : "0";
                        }
                    }
                }

                /*----- Break in Studies/Second Attempt during Class 10 to Class 12 -----*/
                if (ParentActivity.ScoreCard.Is1012BreakReq == true) {
                    ViewGroup pnl_breakin_10_12 = (ViewGroup) ParentActivity.findViewById(R.id.pnl_breakin_10_12);
                    if (pnl_breakin_10_12.getVisibility() == View.VISIBLE) {
                        CheckBox chk_breakin_10_12 = (CheckBox) ParentActivity.findViewById(R.id.chk_breakin_10_12);
                        if (chk_breakin_10_12 != null) {
                            data.Is1012BreakExist = chk_breakin_10_12.isChecked() ? "1" : "0";
                        }
                    }
                }

                /*----- Reason for Break in Studies -----*/
                if (ParentActivity.ScoreCard.IsBreakReasonReq == true) {
                    final EditText txt_break_reason = (EditText) ParentActivity.findViewById(R.id.txt_break_reason);
                    ViewGroup pnl_break_reason = (ViewGroup) ParentActivity.findViewById(R.id.pnl_break_reason);
                    if (pnl_break_reason.getVisibility() == View.VISIBLE) {
                        if (txt_break_reason != null) {
                            data.BreakInStudiesReason = txt_break_reason.getText().toString();
                        }
                    }
                }

                /*----- Specialization Preferred -----*/
                if (ParentActivity.ScoreCard.IsSpecReq == true && ParentActivity.ScoreCard.SpecList != null && ParentActivity.ScoreCard.SpecList.size() > 0) {
                    RadioGroup pnl_specialization_grp = (RadioGroup) ParentActivity.findViewById(R.id.pnl_specialization_grp);
                    if (pnl_specialization_grp != null) {
                        Object tag = pnl_specialization_grp.getTag();
                        if(tag != null && tag instanceof RadioButton) {
                            RadioButton button = (RadioButton) tag;
                            if(button.isChecked()) {
                                ExBaseObject tagInfo = (ExBaseObject) button.getTag();
                                data.SpecID = tagInfo.ID;
                            }
                        }
                    }
                }

                /*----- Work Experience -----*/
                if (ParentActivity.ScoreCard.IsWorkExpReq == true) {
                    ViewGroup pnl_work_exp = (ViewGroup) ParentActivity.findViewById(R.id.pnl_work_exp);
                    if (pnl_work_exp.getVisibility() == View.VISIBLE) {
                        TextView txt_work_exp = (TextView) ParentActivity.findViewById(R.id.txt_work_exp);
                        if(txt_work_exp != null && txt_work_exp.getText().toString().trim().length() == 0) {
                            data.WorkExp = -1;
                        }
                        else {
                            SeekBar sb_work_exp = (SeekBar) ParentActivity.findViewById(R.id.sb_work_exp);
                            if (sb_work_exp != null) {
                                ExTemplate tagInfo = (ExTemplate) sb_work_exp.getTag();
                                data.WorkExp = sb_work_exp.getProgress();
                            }
                        }
                    }
                }

                /*----- Managerial Position -----*/
                if (ParentActivity.ScoreCard.IsMngWorkExpReq == true) {
                    ViewGroup pnl_mng_pos = (ViewGroup) ParentActivity.findViewById(R.id.pnl_mng_pos);
                    if (pnl_mng_pos.getVisibility() == View.VISIBLE) {
                        TextView txt_mng_pos = (TextView) ParentActivity.findViewById(R.id.txt_mng_pos);
                        if(txt_mng_pos != null && txt_mng_pos.getText().toString().trim().length() == 0) {
                            data.MngWorkExp = -1;
                        }
                        else {
                            SeekBar sb_mng_pos = (SeekBar) ParentActivity.findViewById(R.id.sb_mng_pos);
                            if (sb_mng_pos != null) {
                                ExTemplate tagInfo = (ExTemplate) sb_mng_pos.getTag();
                                data.MngWorkExp = sb_mng_pos.getProgress();
                            }
                        }
                    }
                }

                /*----- Present Designation -----*/
                if (ParentActivity.ScoreCard.IsDesignationReq == true) {
                    ViewGroup pnl_designation = (ViewGroup) ParentActivity.findViewById(R.id.pnl_designation);
                    final EditText txt_designation = (EditText) ParentActivity.findViewById(R.id.txt_designation);
                    if (pnl_designation.getVisibility() == View.VISIBLE) {
                        if (txt_designation != null) {
                            data.Designation = txt_designation.getText().toString();
                        }
                    }
                }

                /*----- Grooming -----*/
                if (ParentActivity.ScoreCard.IsGroomingReq == true && ParentActivity.ScoreCard.GroomingList != null && ParentActivity.ScoreCard.GroomingList.size() > 0) {
                    RadioGroup pnl_grooming_grp = (RadioGroup) ParentActivity.findViewById(R.id.pnl_grooming_grp);
                    if (pnl_grooming_grp != null) {
                        Object tag = pnl_grooming_grp.getTag();
                        if(tag != null && tag instanceof RadioButton) {
                            RadioButton button = (RadioButton) tag;
                            if(button.isChecked()) {
                                ExBaseObject tagInfo = (ExBaseObject) button.getTag();
                                data.GroomingID = tagInfo.ID;
                            }
                        }
                    }
                }

                /*----- Dress Code -----*/
                if (ParentActivity.ScoreCard.IsDressCodeReq == true && ParentActivity.ScoreCard.DressCodeList != null && ParentActivity.ScoreCard.DressCodeList.size() > 0) {
                    RadioGroup pnl_dress_code_grp = (RadioGroup) ParentActivity.findViewById(R.id.pnl_dress_code_grp);
                    if (pnl_dress_code_grp != null) {
                        Object tag = pnl_dress_code_grp.getTag();
                        if(tag != null && tag instanceof RadioButton) {
                            RadioButton button = (RadioButton) tag;
                            if(button.isChecked()) {
                                ExBaseObject tagInfo = (ExBaseObject) button.getTag();
                                data.DressCodeID = tagInfo.ID;
                            }
                        }
                    }
                }

                /*----- Panelist Comments -----*/
                if (ParentActivity.ScoreCard.IsCommentReq == true) {
                    Spinner sp_comments = (Spinner) ParentActivity.findViewById(R.id.sp_comments);
                    if(sp_comments != null) {
                        Object selectedItem = sp_comments.getSelectedItem();
                        if(selectedItem != null && selectedItem instanceof ExBaseObject) {
                            data.CommentID = ((ExBaseObject) selectedItem).ID;
                        }
                    }
                }

                /*----- Additional Comments -----*/
                if (ParentActivity.ScoreCard.IsExCommentReq == true) {
                    ViewGroup pnl_extra_comments = (ViewGroup) ParentActivity.findViewById(R.id.pnl_extra_comments);
                    EditText txt_extra_comments = (EditText) ParentActivity.findViewById(R.id.txt_extra_comments);
                    if (pnl_extra_comments.getVisibility() == View.VISIBLE) {
                        if (txt_extra_comments != null) {
                            data.ExComment = txt_extra_comments.getText().toString();
                        }
                    }
                }
            }
            catch(Exception ex) { }
        }
        catch(Exception ex) { }
        return data;
    }
    public void loadState() {
        try {
            ExScore data = null;
            for (ExScore scorecard : ScoreCards) {
                if (scorecard.ScoreCardID == ParentActivity.ScoreCard.ID && scorecard.AppNo == ParentActivity.SelectedStudent.AppNo) {
                    data = scorecard;
                    ParentActivity.ScoreCard.TotalTimeTaken = scorecard.TotalTimeTaken;
                    break;
                }
            }
            if(data != null) {
                /*----- Loading Params ----*/
                ViewGroup pnl_params = (ViewGroup) ParentActivity.findViewById(R.id.pnl_params);
                if (pnl_params != null) {
                    ArrayList<View> views = Common.getChildren(pnl_params);
                    if (views != null) {
                        for (View view : views) {
                            if (view instanceof SeekBar) {
                                final SeekBar seekbar = (SeekBar) view;
                                ExParam tagInfo = (ExParam) seekbar.getTag();
                                try {
                                    for(ExBaseObject param : data.Params) {
                                        if(param.ID == tagInfo.ID) {
                                            if(param.Name != null && param.Name.trim().length() > 0) {
                                                seekbar.setProgress((int) Double.parseDouble(param.Name));
                                            }
                                            break;
                                        }
                                    }
                                }
                                catch(Exception ex) { }
                            }
                            else if(view instanceof EditText) {
                                final EditText editText = (EditText) view;
                                ExParam tagInfo = (ExParam) editText.getTag();
                                try {
                                    for(ExBaseObject param : data.Params) {
                                        if(param.ID == tagInfo.ID) {
                                            if(param.Name != null && param.Name.trim().length() > 0) {
                                                editText.setText(param.Name);
                                            }
                                            break;
                                        }
                                    }
                                }
                                catch(Exception ex) { }
                            }
                        }
                    }
                }

                /*----- Loading Marks -----*/
                if (ParentActivity.ScoreCard.IsMarkPerReq == true && ParentActivity.ScoreCard.EduMarkList != null && ParentActivity.ScoreCard.EduMarkList.size() > 0) {
                    ViewGroup pnl_marks = (ViewGroup) ParentActivity.findViewById(R.id.pnl_marks);
                    if (pnl_marks != null) {
                        ArrayList<View> views = Common.getChildren(pnl_marks);
                        if (views != null) {
                            for (View view : views) {
                                if (view instanceof EditText) {
                                    final EditText edittext = (EditText) view;
                                    ExBaseObject tagInfo = (ExBaseObject) edittext.getTag();
                                    try {
                                        for(ExBaseObject mark : data.Marks) {
                                            if(tagInfo.ID == mark.ID) {
                                                edittext.setText(mark.Name);
                                                break;
                                            }
                                        }
                                    } catch(Exception ex) { }
                                }
                            }
                        }
                    }
                }

                /*----- Education Background -----*/
                if (ParentActivity.ScoreCard.IsEduBgReq == true && ParentActivity.ScoreCard.EduBgList != null && ParentActivity.ScoreCard.EduBgList.size() > 0) {
                    RadioGroup pnl_edu_backgrounds_grp = (RadioGroup) ParentActivity.findViewById(R.id.pnl_edu_backgrounds_grp);
                    if (pnl_edu_backgrounds_grp != null) {
                        ArrayList<View> children = Common.getChildren(pnl_edu_backgrounds_grp);
                        if(children != null) {
                            for(View child : children) {
                                if(child instanceof RadioButton) {
                                    try {
                                        RadioButton button = (RadioButton) child;
                                        ExBaseObject tag = (ExBaseObject) button.getTag();
                                        if(tag.ID == data.EduBgID) {
                                            button.setChecked(true);
                                        }
                                    }
                                    catch(Exception ex) { }
                                }
                            }
                        }
                    }
                }

                /*----- Commerce Background -----*/
                if (ParentActivity.ScoreCard.IsCommerceBgReq == true) {
                    ViewGroup pnl_commerce_bg = (ViewGroup) ParentActivity.findViewById(R.id.pnl_commerce_bg);
                    if (pnl_commerce_bg.getVisibility() == View.VISIBLE) {
                        CheckBox chk_commerce_bg = (CheckBox) ParentActivity.findViewById(R.id.chk_commerce_bg);
                        if (chk_commerce_bg != null) {
                            chk_commerce_bg.setChecked(data.IsCommerceBgExist.compareTo("1") == 0);
                        }
                    }
                }

                /*----- Backlog During Degree -----*/
                if (ParentActivity.ScoreCard.IsBacklogReq == true) {
                    ViewGroup pnl_degree_backlog = (ViewGroup) ParentActivity.findViewById(R.id.pnl_degree_backlog);
                    if (pnl_degree_backlog.getVisibility() == View.VISIBLE) {
                        CheckBox chk_degree_backlog = (CheckBox) ParentActivity.findViewById(R.id.chk_degree_backlog);
                        if (chk_degree_backlog != null) {
                            chk_degree_backlog.setChecked(data.IsBacklogExist.compareTo("1") == 0);
                        }
                    }
                }

                /*----- Break in Studies UG to PG -----*/
                if (ParentActivity.ScoreCard.IsUgPgBreakReq == true) {
                    ViewGroup pnl_breakin_ug_pg = (ViewGroup) ParentActivity.findViewById(R.id.pnl_breakin_ug_pg);
                    if (pnl_breakin_ug_pg.getVisibility() == View.VISIBLE) {
                        CheckBox chk_breakin_ug_pg = (CheckBox) ParentActivity.findViewById(R.id.chk_breakin_ug_pg);
                        if (chk_breakin_ug_pg != null) {
                            chk_breakin_ug_pg.setChecked(data.IsUgPgBreakExist.compareTo("1") == 0);
                        }
                    }
                }

                /*----- Break in Studies/Second Attempt during Class 10 to Class 12 -----*/
                if (ParentActivity.ScoreCard.Is1012BreakReq == true) {
                    ViewGroup pnl_breakin_10_12 = (ViewGroup) ParentActivity.findViewById(R.id.pnl_breakin_10_12);
                    if (pnl_breakin_10_12.getVisibility() == View.VISIBLE) {
                        CheckBox chk_breakin_10_12 = (CheckBox) ParentActivity.findViewById(R.id.chk_breakin_10_12);
                        if (chk_breakin_10_12 != null) {
                            chk_breakin_10_12.setChecked(data.Is1012BreakExist.compareTo("1") == 0);
                        }
                    }
                }

                /*----- Reason for Break in Studies -----*/
                if (ParentActivity.ScoreCard.IsBreakReasonReq == true) {
                    final EditText txt_break_reason = (EditText) ParentActivity.findViewById(R.id.txt_break_reason);
                    ViewGroup pnl_break_reason = (ViewGroup) ParentActivity.findViewById(R.id.pnl_break_reason);
                    if (pnl_break_reason.getVisibility() == View.VISIBLE) {
                        if (txt_break_reason != null) {
                            txt_break_reason.setText(data.BreakInStudiesReason);
                        }
                    }
                }

                /*----- Specialization Preferred -----*/
                if (ParentActivity.ScoreCard.IsSpecReq == true && ParentActivity.ScoreCard.SpecList != null && ParentActivity.ScoreCard.SpecList.size() > 0) {
                    RadioGroup pnl_specialization_grp = (RadioGroup) ParentActivity.findViewById(R.id.pnl_specialization_grp);
                    if (pnl_specialization_grp != null) {
                        ArrayList<View> children = Common.getChildren(pnl_specialization_grp);
                        if(children != null) {
                            for(View child : children) {
                                if(child instanceof RadioButton) {
                                    try {
                                        RadioButton button = (RadioButton) child;
                                        ExBaseObject tag = (ExBaseObject) button.getTag();
                                        if(tag.ID == data.SpecID) {
                                            button.setChecked(true);
                                        }
                                    }
                                    catch(Exception ex) { }
                                }
                            }
                        }
                    }
                }

                /*----- Work Experience -----*/
                if (ParentActivity.ScoreCard.IsWorkExpReq == true) {
                    ViewGroup pnl_work_exp = (ViewGroup) ParentActivity.findViewById(R.id.pnl_work_exp);
                    if (pnl_work_exp.getVisibility() == View.VISIBLE) {
                        if(data.WorkExp > -1) {
                            SeekBar sb_work_exp = (SeekBar) ParentActivity.findViewById(R.id.sb_work_exp);
                            if (sb_work_exp != null) {
                                sb_work_exp.setProgress((int)data.WorkExp);
                                sb_work_exp.performClick();
                            }
                        }
                    }
                }

                /*----- Managerial Position -----*/
                if (ParentActivity.ScoreCard.IsMngWorkExpReq == true) {
                    ViewGroup pnl_mng_pos = (ViewGroup) ParentActivity.findViewById(R.id.pnl_mng_pos);
                    if (pnl_mng_pos.getVisibility() == View.VISIBLE) {
                        if(data.MngWorkExp > -1) {
                            SeekBar sb_mng_pos = (SeekBar) ParentActivity.findViewById(R.id.sb_mng_pos);
                            if (sb_mng_pos != null) {
                                sb_mng_pos.setProgress((int)data.MngWorkExp);
                                sb_mng_pos.performClick();
                            }
                        }
                    }
                }

                /*----- Present Designation -----*/
                if (ParentActivity.ScoreCard.IsDesignationReq == true) {
                    ViewGroup pnl_designation = (ViewGroup) ParentActivity.findViewById(R.id.pnl_designation);
                    final EditText txt_designation = (EditText) ParentActivity.findViewById(R.id.txt_designation);
                    if (pnl_designation.getVisibility() == View.VISIBLE) {
                        if (txt_designation != null) {
                            txt_designation.setText(data.Designation);
                        }
                    }
                }

                /*----- Grooming -----*/
                if (ParentActivity.ScoreCard.IsGroomingReq == true && ParentActivity.ScoreCard.GroomingList != null && ParentActivity.ScoreCard.GroomingList.size() > 0) {
                    RadioGroup pnl_grooming_grp = (RadioGroup) ParentActivity.findViewById(R.id.pnl_grooming_grp);
                    if (pnl_grooming_grp != null) {
                        ArrayList<View> children = Common.getChildren(pnl_grooming_grp);
                        if(children != null) {
                            for(View child : children) {
                                if(child instanceof RadioButton) {
                                    try {
                                        RadioButton button = (RadioButton) child;
                                        ExBaseObject tag = (ExBaseObject) button.getTag();
                                        if(tag.ID == data.GroomingID) {
                                            button.setChecked(true);
                                        }
                                    }
                                    catch(Exception ex) { }
                                }
                            }
                        }
                    }
                }

                /*----- Dress Code -----*/
                if (ParentActivity.ScoreCard.IsDressCodeReq == true && ParentActivity.ScoreCard.DressCodeList != null && ParentActivity.ScoreCard.DressCodeList.size() > 0) {
                    RadioGroup pnl_dress_code_grp = (RadioGroup) ParentActivity.findViewById(R.id.pnl_dress_code_grp);
                    if (pnl_dress_code_grp != null) {
                        ArrayList<View> children = Common.getChildren(pnl_dress_code_grp);
                        if(children != null) {
                            for(View child : children) {
                                if(child instanceof RadioButton) {
                                    try {
                                        RadioButton button = (RadioButton) child;
                                        ExBaseObject tag = (ExBaseObject) button.getTag();
                                        if(tag.ID == data.DressCodeID) {
                                            button.setChecked(true);
                                        }
                                    }
                                    catch(Exception ex) { }
                                }
                            }
                        }
                    }
                }

                /*----- Panelist Comments -----*/
                if (ParentActivity.ScoreCard.IsCommentReq == true) {
                    Spinner sp_comments = (Spinner) ParentActivity.findViewById(R.id.sp_comments);
                    if(sp_comments != null) {
                        ExBaseObject selectedItem = null;
                        for(int i = 0; i < sp_comments.getAdapter().getCount(); i++) {
                            ExBaseObject item = (ExBaseObject) sp_comments.getAdapter().getItem(i);
                            if(item.ID == data.CommentID) {
                                sp_comments.setSelection(i);
                                break;
                            }
                        }
                    }
                }

                /*----- Additional Comments -----*/
                if (ParentActivity.ScoreCard.IsExCommentReq == true) {
                    ViewGroup pnl_extra_comments = (ViewGroup) ParentActivity.findViewById(R.id.pnl_extra_comments);
                    EditText txt_extra_comments = (EditText) ParentActivity.findViewById(R.id.txt_extra_comments);
                    if (pnl_extra_comments.getVisibility() == View.VISIBLE) {
                        if (txt_extra_comments != null) {
                            txt_extra_comments.setText(data.ExComment);
                        }
                    }
                }
            }
        }
        catch(Exception ex) { }
    }
}
