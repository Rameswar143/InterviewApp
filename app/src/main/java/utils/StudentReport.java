package utils;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Html;
import android.view.View;
import android.view.ViewManager;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.vst.christapp.R;

import java.io.InputStream;
import java.net.URL;

import database.DBInfo;
import domain.ExStudentProfile;

public class StudentReport {
    private static View ContentView;

    public static void show(Activity activity, int appNo) {
        try {
            if (appNo > 0) {
                ExStudentProfile profile = DBInfo.createInstance(activity).getStudentProfile(appNo);
                if(profile != null && profile.Header != null && profile.Header.AppID != 0 &&
                        profile.Qualifications != null && profile.Qualifications.size() > 0) {
                    final Dialog dialog = new Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                    if(StudentReport.ContentView == null) {
                        StudentReport.ContentView = activity.getLayoutInflater().inflate(R.layout.view_student_report, null);
                    }
                    else {
                        try {
                            ViewParent parent = StudentReport.ContentView.getParent();
                            if (parent != null) {
                                ViewManager vm = (ViewManager) parent;
                                if (vm != null) {
                                    vm.removeView(StudentReport.ContentView);
                                }
                            }
                        }
                        catch(Exception ex) {
                        }
                    }
                    if(StudentReport.ContentView != null) {
                        StudentReport.registerEvents(dialog, StudentReport.ContentView);
                        StudentReport.loadData(StudentReport.ContentView, profile);
                    }
                    dialog.setContentView(StudentReport.ContentView);
                    dialog.show();
                }
            }
        }catch(Exception ex) {
        }
    }

    private static void registerEvents(final Dialog dialog, View view) {
        view.findViewById(R.id.img_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    private static void loadData(View view, ExStudentProfile profile) {
        StudentReport.loadStudentData(view, profile);
        StudentReport.loadEducationDetails(view, profile);
        StudentReport.loadParentDetails(view, profile);
    }
    private static void loadStudentData(final View view, final ExStudentProfile profile) {
        try {
            if(profile != null && profile.Header != null) {
                try {
                    final ImageView img_photo = (ImageView) view.findViewById(R.id.img_photo);
                    img_photo.setImageDrawable(Common.getDrawable(view.getContext(), R.drawable.img_user));
                    final Handler handler = new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            if (msg != null && msg.obj != null && msg.obj instanceof Bitmap) {
                                img_photo.setImageBitmap((Bitmap) msg.obj);
                            }
                            return false;
                        }
                    });
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Bitmap bmp = null;
                                try {
                                    URL newurl = new URL(Common.IMAGE_URL + Integer.toString(profile.Header.ID) + ".jpg");
                                    bmp = BitmapFactory.decodeStream((InputStream) newurl.getContent());
                                } catch (Exception e) {
                                }
                                if (bmp != null) {
                                    Message msg = new Message();
                                    msg.obj = bmp;
                                    handler.sendMessage(msg);
                                }
                            } catch (Exception ex) {
                            }
                        }
                    }).start();
                }
                catch(Exception ex) {
                }

                TextView lbl_app_no = (TextView) view.findViewById(R.id.lbl_app_no);
                lbl_app_no.setText(Integer.toString(profile.Header.AppNo));

                TextView lbl_name = (TextView) view.findViewById(R.id.lbl_name);
                lbl_name.setText(profile.Header.FirstName);

                TextView lbl_email = (TextView) view.findViewById(R.id.lbl_email);
                lbl_email.setText(profile.Header.Email);

                TextView lbl_category = (TextView) view.findViewById(R.id.lbl_category);
                lbl_category.setText(profile.Header.Category);

                TextView lbl_gender = (TextView) view.findViewById(R.id.lbl_gender);
                lbl_gender.setText(profile.Header.Gender);

                TextView lbl_dob = (TextView) view.findViewById(R.id.lbl_dob);
                lbl_dob.setText(profile.Header.DOB);

                TextView lbl_pob = (TextView) view.findViewById(R.id.lbl_pob);
                lbl_pob.setText(profile.Header.BirthPlace);

                TextView lbl_nationality = (TextView) view.findViewById(R.id.lbl_nationality);
                lbl_nationality.setText(profile.Header.Nationality);

                TextView lbl_religion = (TextView) view.findViewById(R.id.lbl_religion);
                lbl_religion.setText(profile.Header.Religion);

                TextView lbl_section = (TextView) view.findViewById(R.id.lbl_section);
                lbl_section.setText(profile.Header.Section);

                TextView lbl_pref_center = (TextView) view.findViewById(R.id.lbl_pref_center);
                lbl_pref_center.setText(Html.fromHtml("<b>Center Preference : </b>" + profile.Header.ExamCenter));

                TextView lbl_selection_process_date = (TextView) view.findViewById(R.id.lbl_selection_process_date);
                lbl_selection_process_date.setText(Html.fromHtml("<b>Selection Process Date : </b>" + profile.Header.SelectionDate));

                View pnl_pref = view.findViewById(R.id.pnl_pref);
                pnl_pref.setVisibility(View.GONE);
                if ((profile.Header.Pref1 != null && profile.Header.Pref1.trim().length() > 0) ||
                        (profile.Header.Pref2 != null && profile.Header.Pref2.trim().length() > 0) ||
                        (profile.Header.Pref3 != null && profile.Header.Pref3.trim().length() > 0)) {
                    View pnl_pref1 = view.findViewById(R.id.pnl_pref1);
                    View pnl_pref2 = view.findViewById(R.id.pnl_pref2);
                    View pnl_pref3 = view.findViewById(R.id.pnl_pref3);

                    pnl_pref1.setVisibility(View.GONE);
                    pnl_pref2.setVisibility(View.GONE);
                    pnl_pref3.setVisibility(View.GONE);

                    View pnl_pref_sep1 = view.findViewById(R.id.pnl_pref_sep1);
                    View pnl_pref_sep2 = view.findViewById(R.id.pnl_pref_sep2);
                    View pnl_pref_sep3 = view.findViewById(R.id.pnl_pref_sep3);

                    pnl_pref_sep1.setVisibility(View.GONE);
                    pnl_pref_sep2.setVisibility(View.GONE);
                    pnl_pref_sep3.setVisibility(View.GONE);

                    if(profile.Header.Pref1 != null && profile.Header.Pref1.trim().length() > 0) {
                        pnl_pref1.setVisibility(View.VISIBLE);
                        pnl_pref_sep1.setVisibility(View.VISIBLE);
                        TextView lbl_pref1 = (TextView) view.findViewById(R.id.lbl_pref1);
                        lbl_pref1.setText(profile.Header.Pref1);
                    }

                    if(profile.Header.Pref2 != null && profile.Header.Pref2.trim().length() > 0) {
                        pnl_pref2.setVisibility(View.VISIBLE);
                        pnl_pref_sep2.setVisibility(View.VISIBLE);
                        TextView lbl_pref2 = (TextView) view.findViewById(R.id.lbl_pref2);
                        lbl_pref2.setText(profile.Header.Pref2);
                    }

                    if(profile.Header.Pref3 != null && profile.Header.Pref3.trim().length() > 0) {
                        pnl_pref3.setVisibility(View.VISIBLE);
                        pnl_pref_sep3.setVisibility(View.VISIBLE);
                        TextView lbl_pref3 = (TextView) view.findViewById(R.id.lbl_pref3);
                        lbl_pref3.setText(profile.Header.Pref3);
                    }
                }
            }
        }
        catch(Exception ex) {
        }
    }
    private static void loadEducationDetails(View view, ExStudentProfile profile) {
        try {
            if(profile != null) {
                View pnl_edu_row1 = view.findViewById(R.id.pnl_edu_row1);
                View pnl_edu_row2 = view.findViewById(R.id.pnl_edu_row2);
                View pnl_edu_row3 = view.findViewById(R.id.pnl_edu_row3);
                View pnl_edu_row4 = view.findViewById(R.id.pnl_edu_row4);
                View pnl_edu_row5 = view.findViewById(R.id.pnl_edu_row5);

                pnl_edu_row1.setVisibility(View.GONE);
                pnl_edu_row2.setVisibility(View.GONE);
                pnl_edu_row3.setVisibility(View.GONE);
                pnl_edu_row4.setVisibility(View.GONE);
                pnl_edu_row5.setVisibility(View.GONE);

                if(profile.Qualifications != null && profile.Qualifications.size() > 0) {
                    pnl_edu_row1.setVisibility(View.VISIBLE);
                    ((TextView) view.findViewById(R.id.lbl_qualification1)).setText(profile.Qualifications.get(0).Qualification);
                    ((TextView) view.findViewById(R.id.lbl_university1)).setText(profile.Qualifications.get(0).University);
                    ((TextView) view.findViewById(R.id.lbl_institution1)).setText(profile.Qualifications.get(0).Institution);
                    ((TextView) view.findViewById(R.id.lbl_state1)).setText(profile.Qualifications.get(0).State);
                    ((TextView) view.findViewById(R.id.lbl_per1)).setText(String.format("%.2f", profile.Qualifications.get(0).MarkPer));
                    ((TextView) view.findViewById(R.id.lbl_ymofpass1)).setText(profile.Qualifications.get(0).YnMPass);
                    ((TextView) view.findViewById(R.id.lbl_attempt1)).setText(Integer.toString(profile.Qualifications.get(0).Attempt));

                    if(profile.Qualifications.size() > 1) {
                        pnl_edu_row2.setVisibility(View.VISIBLE);
                        ((TextView) view.findViewById(R.id.lbl_qualification2)).setText(profile.Qualifications.get(1).Qualification);
                        ((TextView) view.findViewById(R.id.lbl_university2)).setText(profile.Qualifications.get(1).University);
                        ((TextView) view.findViewById(R.id.lbl_institution2)).setText(profile.Qualifications.get(1).Institution);
                        ((TextView) view.findViewById(R.id.lbl_state2)).setText(profile.Qualifications.get(1).State);
                        ((TextView) view.findViewById(R.id.lbl_per2)).setText(String.format("%.2f", profile.Qualifications.get(1).MarkPer));
                        ((TextView) view.findViewById(R.id.lbl_ymofpass2)).setText(profile.Qualifications.get(1).YnMPass);
                        ((TextView) view.findViewById(R.id.lbl_attempt2)).setText(Integer.toString(profile.Qualifications.get(1).Attempt));
                    }
                    if(profile.Qualifications.size() > 2) {
                        pnl_edu_row3.setVisibility(View.VISIBLE);
                        ((TextView) view.findViewById(R.id.lbl_qualification3)).setText(profile.Qualifications.get(2).Qualification);
                        ((TextView) view.findViewById(R.id.lbl_university3)).setText(profile.Qualifications.get(2).University);
                        ((TextView) view.findViewById(R.id.lbl_institution3)).setText(profile.Qualifications.get(2).Institution);
                        ((TextView) view.findViewById(R.id.lbl_state3)).setText(profile.Qualifications.get(2).State);
                        ((TextView) view.findViewById(R.id.lbl_per3)).setText(String.format("%.2f", profile.Qualifications.get(2).MarkPer));
                        ((TextView) view.findViewById(R.id.lbl_ymofpass3)).setText(profile.Qualifications.get(2).YnMPass);
                        ((TextView) view.findViewById(R.id.lbl_attempt3)).setText(Integer.toString(profile.Qualifications.get(2).Attempt));
                    }
                    if(profile.Qualifications.size() > 3) {
                        pnl_edu_row4.setVisibility(View.VISIBLE);
                        ((TextView) view.findViewById(R.id.lbl_qualification4)).setText(profile.Qualifications.get(3).Qualification);
                        ((TextView) view.findViewById(R.id.lbl_university4)).setText(profile.Qualifications.get(3).University);
                        ((TextView) view.findViewById(R.id.lbl_institution4)).setText(profile.Qualifications.get(3).Institution);
                        ((TextView) view.findViewById(R.id.lbl_state4)).setText(profile.Qualifications.get(3).State);
                        ((TextView) view.findViewById(R.id.lbl_per4)).setText(String.format("%.2f", profile.Qualifications.get(3).MarkPer));
                        ((TextView) view.findViewById(R.id.lbl_ymofpass4)).setText(profile.Qualifications.get(3).YnMPass);
                        ((TextView) view.findViewById(R.id.lbl_attempt4)).setText(Integer.toString(profile.Qualifications.get(3).Attempt));
                    }
                    if(profile.Qualifications.size() > 4) {
                        pnl_edu_row4.setVisibility(View.VISIBLE);
                        ((TextView) view.findViewById(R.id.lbl_qualification5)).setText(profile.Qualifications.get(4).Qualification);
                        ((TextView) view.findViewById(R.id.lbl_university5)).setText(profile.Qualifications.get(4).University);
                        ((TextView) view.findViewById(R.id.lbl_institution5)).setText(profile.Qualifications.get(4).Institution);
                        ((TextView) view.findViewById(R.id.lbl_state5)).setText(profile.Qualifications.get(4).State);
                        ((TextView) view.findViewById(R.id.lbl_per5)).setText(String.format("%.2f", profile.Qualifications.get(4).MarkPer));
                        ((TextView) view.findViewById(R.id.lbl_ymofpass5)).setText(profile.Qualifications.get(4).YnMPass);
                        ((TextView) view.findViewById(R.id.lbl_attempt5)).setText(Integer.toString(profile.Qualifications.get(4).Attempt));
                    }
                }

                View pnl_prerequisite = view.findViewById(R.id.pnl_prerequisite);
                pnl_prerequisite.setVisibility(View.GONE);
                if(profile.Header != null && profile.Header.PreReqName != null && profile.Header.PreReqName.trim().length() > 0) {
                    pnl_prerequisite.setVisibility(View.VISIBLE);

                    ((TextView) view.findViewById(R.id.lbl_prereq_name)).setText(profile.Header.PreReqName + " : " + String.format("%.2f", profile.Header.PreReqMark));
                    ((TextView) view.findViewById(R.id.lbl_prereq_month)).setText(profile.Header.PreReqMonth);
                    ((TextView) view.findViewById(R.id.lbl_prereq_year)).setText(profile.Header.PreReqYear);
                    ((TextView) view.findViewById(R.id.lbl_prereq_rollno)).setText(profile.Header.PreReqRollNo);
                }

                TextView lbl_exp = (TextView) view.findViewById(R.id.lbl_exp);
                lbl_exp.setVisibility(View.GONE);
                if(profile.Header.WorkExp > 0) {
                    lbl_exp.setVisibility(View.VISIBLE);
                    lbl_exp.setText(Html.fromHtml("<b>Work Experience in No of Years : </b>" + String.format("%.1f", profile.Header.WorkExp)));
                }

                ((TextView) view.findViewById(R.id.lbl_backlogs)).setText(Html.fromHtml("<b>Backlogs : </b>" + (profile.Header.BackLogs == 0 ? "NO" : "YES")));
            }
        }
        catch(Exception ex) {
        }
    }
    private static void loadParentDetails(View view, ExStudentProfile profile) {
        try {
            ((TextView) view.findViewById(R.id.lbl_father_name)).setText(profile.Header.FatherName);
            ((TextView) view.findViewById(R.id.lbl_father_occupation)).setText(profile.Header.FatherJob);
            ((TextView) view.findViewById(R.id.lbl_father_income)).setText(profile.Header.FatherIncome);

            ((TextView) view.findViewById(R.id.lbl_mother_name)).setText(profile.Header.MotherName);
            ((TextView) view.findViewById(R.id.lbl_mother_occupation)).setText(profile.Header.MotherJob);
            ((TextView) view.findViewById(R.id.lbl_mother_income)).setText(profile.Header.MotherIncome);

            if(profile.Header.CAddress == null || profile.Header.CAddress.trim().length() == 0) {
                profile.Header.CAddress = "NA";
            }
            ((TextView) view.findViewById(R.id.lbl_caddress)).setText(profile.Header.CAddress);

            if(profile.Header.PAddress == null || profile.Header.PAddress.trim().length() == 0) {
                profile.Header.PAddress = "NA";
            }
            ((TextView) view.findViewById(R.id.lbl_paddress)).setText(profile.Header.PAddress);

            if(profile.Header.Phone == null || profile.Header.Phone.trim().length() == 0) {
                profile.Header.Phone = "NA";
            }
            ((TextView) view.findViewById(R.id.lbl_phone)).setText("Phone Number : " + profile.Header.Phone);

            if(profile.Header.Mobile == null || profile.Header.Mobile.trim().length() == 0) {
                profile.Header.Mobile = "NA";
            }
            ((TextView) view.findViewById(R.id.lbl_mobile)).setText("Mobile Number : " + profile.Header.Mobile);
        }
        catch(Exception ex) {
        }
    }
}
