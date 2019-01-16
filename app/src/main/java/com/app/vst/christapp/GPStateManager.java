package com.app.vst.christapp;

import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;

import java.io.Serializable;
import java.util.ArrayList;

import domain.ExGroupStudent;
import domain.ExParam;
import utils.Common;
import utils.ScoreView;

public class GPStateManager {
    private static GPStateManager Instance = null;
    private UIGroupActivity ParentActivity = null;
    private ArrayList<GroupData> Groups = new ArrayList<GroupData>();
    private String FILE_NAME = "GP_STATE_DATA";

    private GPStateManager(UIGroupActivity parentActivity) {
        this.ParentActivity = parentActivity;
        this.Groups = (ArrayList<GroupData>) Common.readObjectFromDevice(this.ParentActivity, FILE_NAME);
        if (this.Groups == null) {
            this.Groups = new ArrayList<GroupData>();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (ParentActivity.ManageState == true) {
                        if (ParentActivity.SelectedGroup != null && ParentActivity.ScoreCard != null) {
                            if (ParentActivity.Saved == false) {
                                try {
                                    GroupData group = readState();
                                    removeState();
                                    Groups.add(group);
                                    Common.writeObjectToDevice(ParentActivity, FILE_NAME, Groups);
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
    public static GPStateManager loadInstance(UIGroupActivity parentActivity) {
        if(GPStateManager.Instance == null) { GPStateManager.Instance = new GPStateManager(parentActivity); }
        else { GPStateManager.Instance.ParentActivity = parentActivity; }
        return GPStateManager.Instance;
    }
    public void removeState() {
        try {
            GroupData existing = null;
            for (GroupData group : Groups) {
                if (group.ScoreCardID == ParentActivity.ScoreCard.ID && group.GroupID == ParentActivity.SelectedGroup.ID) {
                    existing = group;
                    break;
                }
            }
            if(existing != null) {
                Groups.remove(existing);
                Common.writeObjectToDevice(ParentActivity, FILE_NAME, Groups);
            }
        }
        catch(Exception ex) { }
    }
    public GroupData readState() {
        GroupData data = new GroupData();
        try {
            try {
                data.GroupID = ParentActivity.SelectedGroup.ID;
                data.ScoreCardID = ParentActivity.ScoreCard.ID;
                data.Students = new ArrayList<StudentData>();
            }
            catch(Exception ex) { }

            try {
                LinearLayout pnl_candidates = (LinearLayout) ParentActivity.findViewById(R.id.pnl_candidates);
                if(pnl_candidates != null) {
                    ArrayList<View> children = Common.getChildren(pnl_candidates);
                    if(children != null) {
                        for(View child : children) {
                            if(child instanceof LinearLayout) {
                                if (child.getTag() != null && child.getTag() instanceof ExGroupStudent) {
                                    ExGroupStudent student = (ExGroupStudent) child.getTag();
                                    if (student != null) {
                                        /*----- Adding Student Information -----*/
                                        StudentData row = new StudentData();
                                        row.AppNo = student.AppNo;
                                        row.Params = new ArrayList<ParamData>();
                                        data.Students.add(row);

                                        /*----- Loading All Params -----*/
                                        LinearLayout panel = (LinearLayout) child;
                                        ArrayList<View> scoreviews = Common.getChildren(panel);
                                        if (scoreviews != null) {
                                            for (View view : scoreviews) {
                                                if (view instanceof ScoreView) {
                                                    ScoreView scoreview = (ScoreView) view;
                                                    ExParam param = (ExParam) scoreview.getTag();
                                                    if (param != null) {
                                                        ParamData paramdata = new ParamData();
                                                        paramdata.ID = param.ID;
                                                        paramdata.Value = scoreview.Value;
                                                        paramdata.Text = scoreview.getText().toString();
                                                        row.Params.add(paramdata);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
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
            GroupData data = null;
            for (GroupData group : Groups) {
                if (group.ScoreCardID == ParentActivity.ScoreCard.ID && group.GroupID == ParentActivity.SelectedGroup.ID) {
                    data = group;
                    break;
                }
            }
            if(data != null) {
                /*----- Loading Params ----*/
                try {
                    LinearLayout pnl_candidates = (LinearLayout) ParentActivity.findViewById(R.id.pnl_candidates);
                    if(pnl_candidates != null) {
                        ArrayList<View> children = Common.getChildren(pnl_candidates);
                        if(children != null) {
                            for(View child : children) {
                                if(child instanceof LinearLayout) {
                                    if (child.getTag() != null && child.getTag() instanceof ExGroupStudent) {
                                        ExGroupStudent student = (ExGroupStudent) child.getTag();
                                        if (student != null) {
                                            StudentData row = null;
                                            if (data.Students != null) {
                                                for (StudentData item : data.Students) {
                                                    if (item.AppNo == student.AppNo) {
                                                        row = item;
                                                        break;
                                                    }
                                                }

                                                if (row != null && row.Params != null && row.Params.size() > 0) {
                                                    LinearLayout panel = (LinearLayout) child;
                                                    ArrayList<View> scoreviews = Common.getChildren(panel);
                                                    if (scoreviews != null) {
                                                        for (View view : scoreviews) {
                                                            if (view instanceof ScoreView) {
                                                                ScoreView scoreview = (ScoreView) view;
                                                                ExParam param = (ExParam) scoreview.getTag();
                                                                if (param != null) {
                                                                    ParamData paramdata = null;
                                                                    for (ParamData item : row.Params) {
                                                                        if (param.ID == item.ID) {
                                                                            scoreview.Value = item.Value;
                                                                            scoreview.setText(item.Text);
                                                                            if (scoreview.ValueChangeCallback != null) {
                                                                                Message msg = new Message();
                                                                                msg.obj = scoreview;
                                                                                scoreview.ValueChangeCallback.sendMessage(msg);
                                                                            }
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                catch(Exception ex) { }
            }
        }
        catch(Exception ex) { }
    }

    public class StudentData implements Serializable {
        public int AppNo;
        public ArrayList<ParamData> Params = new ArrayList<ParamData>();
    }
    public class ParamData implements Serializable {
        public int ID;
        public double Value;
        public String Text;
    }
    public class GroupData implements Serializable {
        public int ScoreCardID;
        public int GroupID;
        public ArrayList<StudentData> Students = new ArrayList<StudentData>();
    }
}
