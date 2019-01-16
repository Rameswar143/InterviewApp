package com.app.vst.christapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import database.AttendanceManager;
import database.DBInfo;
import database.DeviceDataManager;
import domain.ExBaseObject;
import domain.ExGroup;
import domain.ExGroupStudent;
import domain.ExParam;
import domain.ExScore;
import domain.ExTemplate;
import utils.Common;
import utils.ScoreView;

public class UIGroupActivity extends UIBaseActivity {
    public static UIGroupActivity Instance = null;
    public static ArrayList<ExTemplate> ScoreCards = new ArrayList<>();
    public static ExTemplate ScoreCard;
    public static ExGroup SelectedGroup = new ExGroup();
    public static boolean Saved = false;
    public boolean ManageState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(Instance != null) { Instance.finish(); }
        Instance = this;
        ScoreCard = null;
        ScoreCards = new ArrayList<>();
        AttendanceManager.init(this);

        /*----- Sorting Student List -----*/
        Collections.sort(SelectedGroup.Students, new Comparator<ExGroupStudent>() {
            @Override
            public int compare(ExGroupStudent lhs, ExGroupStudent rhs) {
                return lhs.AppNo < rhs.AppNo ? -1 : 1;
            }
        });

        super.onCreate(savedInstanceState);
        try { Common.loadActionBarWithTimer(this); }catch(Exception e) { }

        this.loadScoreCards();
        if(this.ScoreCards != null && this.ScoreCards.size() > 0) {
            setContentView(R.layout.ui_group_activity);
            if(defaultScoreCard()) {
                this.init();
                /*----- State Management -----*/
                GPStateManager.loadInstance(this);
            }
        }
        else {
            Common.alert(UIGroupActivity.this, "Selection process score card is not defined for this group!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UIGroupActivity.this.finish();
                }
            });
        }
    }

    @Override
    protected void onStop() {
        this.ManageState = false;
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        Common.confirm(this, "Are you sure you want leave this page?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == Dialog.BUTTON_POSITIVE) {
                    onSuperBackPressed();
                }
            }
        });
    }

    private void onSuperBackPressed() {
        super.onBackPressed();
    }

    private boolean defaultScoreCard() {
        if(this.ScoreCards != null && this.ScoreCards.size() > 0) {
            for(ExTemplate template : this.ScoreCards) {
                if(isSubmitted(template) == false) {
                    loadScoreCardInfo(template);
                    return true;
                }
            }

            Common.alert(UIGroupActivity.this, "Sorry! Score card is already submitted; You cannot continue.", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UIGroupActivity.this.finish();
                }
            });
        }
        return false;
    }

    private boolean isSubmitted(ExTemplate template) {
        boolean exist = false;
        try {
            exist = DBInfo.createInstance(UIGroupActivity.this).isGroupScoreCardExist(
                    template.ID, SelectedGroup.Students.get(0).GroupDetailID);
        }
        catch (Exception ex) {
        }
        return exist;
    }

    private void loadScoreCards() {
        if(DeviceDataManager.Data.SelectedGroupScoreCards != null) {
            for(ExTemplate scorecard : DeviceDataManager.Data.SelectedGroupScoreCards) {
                if(scorecard.IsSelected == true) {
                    ScoreCards.add(scorecard);
                }
            }
        }
    }

    private void init() {
        View btn_back = this.findViewById(R.id.btn_back);
        if(btn_back != null) {
            btn_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UIGroupActivity.this.startActivity(new Intent(UIGroupActivity.this, UIHomeActivity.class));
                    UIGroupActivity.this.finish();
                }
            });
        }

        TextView lbl_group = (TextView) this.findViewById(R.id.lbl_group);
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

        pnl_popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return;
            }
        });
        pnl_popup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        if(SelectedGroup != null) {
            lbl_group.setText(SelectedGroup.Name);
        }

        if(lvw_popup_items != null && ScoreCards.size() > 1) {
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
                        view = UIGroupActivity.this.getLayoutInflater().inflate(R.layout.view_scorecard_item, null);
                    }

                    view.setTag(item);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final ExTemplate template = (ExTemplate) v.getTag();
                            if (template != null) {
                                if(isSubmitted(template)) {
                                    Common.alert(UIGroupActivity.this, "Sorry! Score card is already submitted; You cannot continue.");
                                    return;
                                }
                                else {
                                    if(pnl_popup != null) { pnl_popup.setVisibility(View.GONE); }
                                    loadScoreCardInfo(template);
                                }
                            }
                            if(pnl_popup != null) { pnl_popup.setVisibility(View.GONE); }
                        }
                    });
                    TextView lbl_text = (TextView) view.findViewById(R.id.lbl_text);
                    if (lbl_text != null) {
                        lbl_text.setText(item.Name);
                    }

                    final View img_done = view.findViewById(R.id.img_done);
                    if(img_done != null) {
                        final View parentView = view;
                        img_done.setVisibility(View.GONE);

                        final Handler handler = new Handler(new Handler.Callback() {
                            @Override
                            public boolean handleMessage(Message msg) {
                                if(msg.obj instanceof View) {
                                    View parentView = (View) msg.obj;
                                    View img_done = parentView.findViewById(R.id.img_done);
                                    ExTemplate template = (ExTemplate) parentView.getTag();
                                    img_done.setVisibility(isSubmitted(template) ? View.VISIBLE : View.GONE);
                                }
                                return false;
                            }
                        });
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Message msg = new Message();
                                msg.obj = parentView;
                                handler.sendMessage(msg);
                            }
                        }).start();
                    }

                    return view;
                }
            });

            if(pnl_scoresheets != null) {
                pnl_scoresheets.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ScoreCards == null || ScoreCards.size() == 0) {
                            Common.alert(UIGroupActivity.this, "No selection process selected, Please contact your administrator!");
                        } else {
                            if (pnl_popup != null) {
                                pnl_popup.setVisibility(View.VISIBLE);
                                BaseAdapter adpater = (BaseAdapter) lvw_popup_items.getAdapter();
                                if(adpater != null) {
                                    adpater.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    private void showScoreCardPopup() {
        if(this.ScoreCards != null && this.ScoreCards.size() > 0) {
            for(ExTemplate template : this.ScoreCards) {
                if(isSubmitted(template) == false) {
                    View pnl_scoresheets = findViewById(R.id.pnl_scoresheets);
                    pnl_scoresheets.performClick();
                    return;
                }
            }
        }

        UIGroupActivity.this.finish();
    }

    private void submitScoreCard() {
        final ProgressDialog wait = ProgressDialog.show(UIGroupActivity.this, "", "Wait while loading...", true, false);
        try {
            final ArrayList<ExScore> scores = new ArrayList<>();
            LinearLayout pnl_candidates = (LinearLayout) this.findViewById(R.id.pnl_candidates);
            if (pnl_candidates != null) {
                int maxChild = pnl_candidates.getChildCount();
                for (int i = 0; i < maxChild; i++) {
                    View row = pnl_candidates.getChildAt(i);
                    if (row instanceof LinearLayout && row.getTag() != null && row.getTag() instanceof ExGroupStudent) {
                        ExGroupStudent student = (ExGroupStudent) row.getTag();
                        if(student != null) {
                            final ExScore data = new ExScore();
                            try {
                                data.AppID = student.AppID;
                                data.AppNo = student.AppNo;
                                try {
                                    data.UserID = Integer.parseInt(DeviceDataManager.Data.SelectedUser.ID);
                                } catch (Exception e) {
                                }
                                data.ScoreCardID = ScoreCard.ID;
                                data.GroupDetailID = student.GroupDetailID;
                            } catch (Exception ex) {
                            }
                            if (i == 0) {
                                if (DBInfo.createInstance(UIGroupActivity.this).isGroupScoreCardExist(data.ScoreCardID, data.GroupDetailID) == true) {
                                    wait.dismiss();
                                    Common.alert(UIGroupActivity.this, "Sorry! Score card is already submitted; You cannot continue.");
                                    return;
                                }
                            }
                            data.Params = new ArrayList<>();
                            data.IsAbsent = student.Absent;
                            if (student.Absent == false) {
                                int cells = ((LinearLayout) row).getChildCount();
                                for (int c = 0; c < cells; c++) {
                                    View cell = ((LinearLayout) row).getChildAt(c);
                                    if (cell instanceof ScoreView) {
                                        ScoreView scoreview = (ScoreView) cell;
                                        String text = scoreview.getText().toString();
                                        //if (scoreview.Value == 0) {
                                        if (text.trim().length() == 0) {
                                            wait.dismiss();
                                            Common.alert(UIGroupActivity.this, "Please enter valid score: (Application No: " + Integer.toString(student.AppNo) + ")");
                                            return;
                                        }
                                        ExParam tagInfo = (ExParam) scoreview.getTag();
                                        data.Params.add(new ExBaseObject(tagInfo.ID, Double.toString(scoreview.Value)));
                                    }
                                }
                            }

                            scores.add(data);
                        }
                    }
                }

                /*----- Saving Data to Device Memory -----*/
                try {
                    Common.confirm(UIGroupActivity.this, "Do you want to submit the score card?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Dialog.BUTTON_POSITIVE == which) {
                                if (DBInfo.createInstance(UIGroupActivity.this).isGroupScoreCardExist(scores.get(0).ScoreCardID, scores.get(0).GroupDetailID)) {
                                    wait.dismiss();
                                    Saved = true;
                                    Common.alert(UIGroupActivity.this, "Sorry! Score card is already submitted; You cannot continue.");
                                } else {
                                    DBInfo.createInstance(UIGroupActivity.this).saveScoreCards(scores);
                                    wait.dismiss();
                                    Saved = true;
                                        /*----- Remove The State Information -----*/
                                    GPStateManager.loadInstance(UIGroupActivity.this).removeState();
                                    Common.alert(UIGroupActivity.this, "Scorecard successfully submitted.", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (ScoreCards != null && ScoreCards.size() == 1) {
                                                UIGroupActivity.this.finish();
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

                Saved = true;
            }
        }
        catch(Exception ex) { wait.dismiss(); }
    }

    private void loadScoreCardInfo(ExTemplate template) {
        if(ScoreCard == null || ScoreCard.ID != template.ID) {
            Saved = false;
            ScoreCard = template;
            this.ManageState = false;

            TextView lbl_title = (TextView) this.findViewById(R.id.lbl_title);
            lbl_title.setText(ScoreCard.Name);

            this.loadHeaders(template);
            this.loadRows(template);

            /*----- Loading Previous State -----*/
            GPStateManager.loadInstance(this).loadState();
            this.ManageState = true;
        }
    }

    private void loadHeaders(ExTemplate template) {
        LinearLayout pnl_header = (LinearLayout) this.findViewById(R.id.pnl_header);
        if(pnl_header != null && template.ParamList != null) {
            pnl_header.removeAllViews();
            pnl_header.setWeightSum(((template.ParamList.size() + 1) * 3) + 3);
            loadHeaderCell(pnl_header, 0, "Srl.", false, 1.0F);
            loadHeaderCell(pnl_header, 0, "Candidate", false, 3.0F);
            for(ExParam param : template.ParamList) {
                loadHeaderCell(pnl_header, param.ID, param.Name, false, 3.0F);
            }
            loadHeaderCell(pnl_header, 0, "TOTAL", true, 2.0F);
        }
    }

    private void loadHeaderCell(LinearLayout panel, int id, String text, boolean total, float weight) {
        /*----- Generate View -----*/
        TextView view = new TextView(UIGroupActivity.this);
        view.setBackgroundDrawable(Common.getDrawable(UIGroupActivity.this, R.drawable.dr_sheet_header));
        view.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weight));
        view.setTag(total ? "TOTAL" : "");
        view.setPadding(5, 5, 5, 5);
        view.setTextSize((text == null || text.trim().length() == 0) && total ? 24 : 14);
        view.setMinHeight(100);
        view.setText(text);
        view.setSingleLine(false);
        view.setGravity(Gravity.CENTER);
        view.setTextColor(Color.parseColor((text == null || text.trim().length() == 0) && total ? "#112233" : "#555555"));
        view.setTypeface((text == null || text.trim().length() == 0) && total ? Common.getDigitalFontface(UIGroupActivity.this) : Typeface.DEFAULT_BOLD);
        /*----- Inserting View -----*/
        panel.addView(view);
    }

    private void setAbsent(LinearLayout panel, boolean absent) {
        for(int i=0; i<panel.getChildCount(); i++) {
            View child = panel.getChildAt(i);
            if(child instanceof ScoreView) {
                ScoreView childscoreview = (ScoreView) child;
                if(childscoreview != null) {
                    childscoreview.Absent = absent;
                    if(absent) {
                        childscoreview.setText("- -");
                        childscoreview.Value = 0.00F;
                    }
                    else {
                        if(childscoreview.getText().toString().compareTo("- -")==0) {
                            childscoreview.setText("");
                        }
                    }
                }
            }
            else if(absent && child.getTag() == "TOTAL" && child instanceof TextView){
                ((TextView) child).setText("");
            }
        }
    }

    final Handler scoreChangedHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg != null && msg.obj != null && msg.obj instanceof ScoreView) {
                try {
                    ScoreView scoreview = (ScoreView) msg.obj;
                    if (scoreview != null) {
                        ViewGroup parent = (ViewGroup) scoreview.getParent();
                        if(parent != null) {
                            double score = 0.00F;
                            for(int i=0; i<parent.getChildCount(); i++) {
                                View child = parent.getChildAt(i);
                                if(child instanceof ScoreView) {
                                    ScoreView childscoreview = (ScoreView) child;
                                    if(childscoreview != null) {
                                        score += childscoreview.Value;
                                    }
                                }
                            }

                            View totalview = parent.findViewWithTag("TOTAL");
                            if(totalview != null && totalview instanceof TextView) {
                                TextView textview = (TextView) totalview;
                                if(textview != null) {
                                    textview.setText(score > 0 ? Double.toString(score) : "");
                                }
                            }
                        }
                    }
                }
                catch(Exception ex) { }
            }
            return true;
        }
    });

    private void loadRows(ExTemplate template) {
        LinearLayout pnl_candidates = (LinearLayout) this.findViewById(R.id.pnl_candidates);
        if(pnl_candidates != null) {
            pnl_candidates.removeAllViews();
            if (SelectedGroup != null && SelectedGroup.Students != null && SelectedGroup.Students.size() > 0) {
                for (ExGroupStudent student : SelectedGroup.Students) {
                    final LinearLayout panel = new LinearLayout(UIGroupActivity.this);
                    panel.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));
                    panel.setWeightSum(((template.ParamList.size() + 1) * 3) + 3);
                    panel.setTag(student);
                    pnl_candidates.addView(panel);

                    final TextView srlView = new TextView(UIGroupActivity.this);
                    srlView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0F));
                    srlView.setGravity(Gravity.CENTER);
                    srlView.setTypeface(Typeface.DEFAULT_BOLD);
                    srlView.setText(Integer.toString(SelectedGroup.Students.indexOf(student) + 1));
                    srlView.setBackgroundDrawable(Common.getDrawable(UIGroupActivity.this, R.drawable.dr_sheet_header));
                    panel.addView(srlView);

                    final RelativeLayout imagePanel = new RelativeLayout(UIGroupActivity.this);
                    imagePanel.setBackgroundDrawable(Common.getDrawable(UIGroupActivity.this, R.drawable.dr_sheet_header));
                    imagePanel.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 3.0F));
                    panel.addView(imagePanel);

                    final ImageView imageview = new ImageView(UIGroupActivity.this);
                    imageview.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    imageview.setImageDrawable(Common.getDrawable(UIGroupActivity.this, R.drawable.img_person));
                    ((RelativeLayout.LayoutParams) imageview.getLayoutParams()).addRule(RelativeLayout.CENTER_IN_PARENT);
                    imageview.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    imageview.setPadding(0, 0, 0, 15);
                    imagePanel.addView(imageview);

                    final TextView absentView = new TextView(UIGroupActivity.this);
                    absentView.setText("ABSENT");
                    absentView.setTag(student);
                    absentView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    absentView.setGravity(Gravity.CENTER);
                    ((RelativeLayout.LayoutParams) absentView.getLayoutParams()).setMargins(1, 1, 1, 1);
                    absentView.setTextSize(18);
                    absentView.setTextColor(Color.parseColor("#B06569"));
                    absentView.setBackgroundColor(Color.parseColor("#B0FFFFFF"));
                    absentView.setVisibility(View.GONE);
                    imagePanel.addView(absentView);

                    final ExGroupStudent groupStudent = student;
                    imagePanel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialog.Builder(UIGroupActivity.this)
                                .setTitle("Please select student status")
                                //.setMessage("Please select student status")
                                .setCancelable(false)
                                .setNegativeButton("ABSENT", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        absentView.setVisibility(View.VISIBLE);
                                        groupStudent.Absent = true;
                                        AttendanceManager.setAbsent((long) groupStudent.AppNo);
                                        setAbsent(panel, true);
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton("PRESENT", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        groupStudent.Absent = false;
                                        absentView.setVisibility(View.GONE);
                                        AttendanceManager.setPresent((long) groupStudent.AppNo);
                                        setAbsent(panel, false);
                                        dialog.dismiss();
                                    }
                                }).create().show();
                        }
                    });

                    final TextView appNoView = new TextView(UIGroupActivity.this);
                    appNoView.setText(Integer.toString(student.AppNo));
                    appNoView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    ((RelativeLayout.LayoutParams) appNoView.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    appNoView.setGravity(Gravity.CENTER);
                    appNoView.setPadding(5, 1, 5, 1);
                    ((RelativeLayout.LayoutParams) appNoView.getLayoutParams()).setMargins(1,1,1,1);
                    appNoView.setTextSize(12);
                    appNoView.setTextColor(Color.parseColor("#111111"));
                    appNoView.setBackgroundColor(Color.parseColor("#BBFFFFFF"));
                    imagePanel.addView(appNoView);

                    final Handler handler = new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            if(msg != null && msg.obj != null && msg.obj instanceof Bitmap) {
                                imageview.setImageBitmap((Bitmap) msg.obj);
                            }
                            return false;
                        }
                    });
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap bmp = null;
                            try {
                                URL newurl = new URL(Common.IMAGE_URL + Integer.toString(groupStudent.ID) + ".jpg");
                                bmp = BitmapFactory.decodeStream((InputStream)newurl.getContent());
                            } catch (Exception e) { }
                            if(bmp != null) {
                                Message msg = new Message();
                                msg.obj = bmp;
                                handler.sendMessage(msg);
                            }
                        }
                    }).start();

                    if(template.ParamList != null && template.ParamList.size() > 0) {
                        for (ExParam param : template.ParamList) {
                            ScoreView childview = new ScoreView(UIGroupActivity.this);
                            childview.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 3.0F));
                            childview.setBackgroundDrawable(Common.getDrawable(UIGroupActivity.this, R.drawable.dr_sheet_cell));
                            childview.setTag(param);
                            childview.MaximumValue = param.MaxValue;
                            childview.Interval = param.Interval;
                            childview.ValueChangeCallback = scoreChangedHandler;
                            panel.addView(childview);
                        }
                    }

                    loadHeaderCell(panel, 0, "", true, 2.0F);

                    if(AttendanceManager.isAbsent((long) groupStudent.AppNo)) {
                        absentView.setVisibility(View.VISIBLE);
                        groupStudent.Absent = true;
                        setAbsent(panel, true);
                    }
                }
            }
        }
    }
}
