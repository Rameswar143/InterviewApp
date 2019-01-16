package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;

import java.util.ArrayList;

import domain.ExScore;
import domain.ExScoreInfo;
import domain.ExStudentDataArgs;
import domain.ExStudentDtlInfo;
import domain.ExStudentDtlInfoList;
import domain.ExStudentEduInfo;
import domain.ExStudentEduInfoList;
import domain.ExStudentProfile;

public class DBInfo extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Christ.db";
    private static SQLiteDatabase mWritableDb;
    private static SQLiteDatabase mReadableDb;
    private static final String STUDENT_TBL = "christ_student_info";
    private static final String QUALIFICATION_TBL = "christ_qualification_info";
    private static final String SCORECARD_TBL = "christ_scorecard_info";
    private static DBInfo Instance = null;

    private DBInfo(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, 3);
    }

    public static DBInfo createInstance(Context context) {
        if(DBInfo.Instance == null) {
            DBInfo.Instance = new DBInfo(context);
        }
        return DBInfo.Instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + STUDENT_TBL + " (" +
                "ID INTEGER, " +
                "AppID INTEGER, " +
                "AppNo INTEGER, " +
                "FirstName VARCHAR(100), " +
                "Gender VARCHAR(10), " +
                "Email VARCHAR(100), " +
                "DOB VARCHAR(20), " +
                "Nationality VARCHAR(50), " +
                "BirthPlace VARCHAR(100), " +
                "Religion VARCHAR(100), " +
                "Section VARCHAR(100), " +
                "Category VARCHAR(100), " +
                "PreReqMark REAL, " +
                "PreReqMonth VARCHAR(20), " +
                "PreReqYear VARCHAR(10), " +
                "PreReqRollNo VARCHAR(50), " +
                "PreReqName VARCHAR(100), " +
                "Pref1 VARCHAR(100), " +
                "Pref2 VARCHAR(100), " +
                "Pref3 VARCHAR(100), " +
                "JournalNo VARCHAR(50), " +
                "ExamCenter VARCHAR(100), " +
                "SelectionDate VARCHAR(20), " +
                "Phone VARCHAR(50), " +
                "Mobile VARCHAR(50), " +
                "PAddress TEXT, " +
                "CAddress TEXT, " +
                "FatherName VARCHAR(100), " +
                "MotherName VARCHAR(100), " +
                "FatherIncome VARCHAR(100), " +
                "MotherIncome VARCHAR(100), " +
                "MotherJob VARCHAR(100), " +
                "FatherJob VARCHAR(100), " +
                "WorkExp REAL, " +
                "BackLogs INTEGER)");
        db.execSQL("CREATE TABLE " + QUALIFICATION_TBL + " (" +
                "AppID INTEGER, " +
                "AppNo INTEGER, " +
                "ID INTEGER, " +
                "Qualification VARCHAR(100), " +
                "University VARCHAR(100), " +
                "Institution VARCHAR(100), " +
                "State VARCHAR(100), " +
                "MarkPer REAL, " +
                "YnMPass VARCHAR(30), " +
                "Attempt INTEGER)");
        db.execSQL("CREATE TABLE " + SCORECARD_TBL + " (" +
                "AppID INTEGER, " +
                "AppNo INTEGER, " +
                "ScoreCardID INTEGER, " +
                "GroupDetailID INTEGER, " +
                "Updated CHARACTER(1)," +
                "Data TEXT)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + STUDENT_TBL);
        db.execSQL("DROP TABLE IF EXISTS " + QUALIFICATION_TBL);
        db.execSQL("DROP TABLE IF EXISTS " + SCORECARD_TBL);
        this.onCreate(db);
    }
    @Override
    public void close() {
        super.close();
        if (DBInfo.mWritableDb != null) {
            DBInfo.mWritableDb.close();
            DBInfo.mWritableDb = null;
        }
        if (DBInfo.mReadableDb != null) {
            DBInfo.mReadableDb.close();
            DBInfo.mReadableDb = null;
        }
    }

    private SQLiteDatabase getChristWritableDatabase() {
        if ((DBInfo.mWritableDb == null) || (!DBInfo.mWritableDb.isOpen())) {
            DBInfo.mWritableDb = this.getWritableDatabase();
        }
        return DBInfo.mWritableDb;
    }
    private SQLiteDatabase getChristReadableDatabase() {
        if ((DBInfo.mReadableDb == null) || (!DBInfo.mReadableDb.isOpen())) {
            DBInfo.mReadableDb = this.getReadableDatabase();
        }
        return DBInfo.mReadableDb;
    }

    public int getUnsavedDataCount() {
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = this.getChristReadableDatabase().rawQuery(
                    "SELECT COUNT(*) AS Unsaved FROM " + SCORECARD_TBL + " WHERE Updated=?",
                    new String[]{"N"});
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                count = cursor.getInt(cursor.getColumnIndex("Unsaved"));
                break;
            }
        }
        catch (Exception ex) {
        }
        finally {
            if(cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return count;
    }
    public boolean isScoreCardExist(int appNo, int appID, int scorecardID) {
        Cursor cursor = null;
        try {
            cursor = this.getChristReadableDatabase().rawQuery("SELECT AppID FROM " + SCORECARD_TBL + " WHERE AppID=? AND AppNo=? AND ScoreCardID=? AND GroupDetailID=0",
                    new String[]{Integer.toString(appID), Integer.toString(appNo), Integer.toString(scorecardID)});
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                return true;
            }
        }
        catch(Exception ex) {
        }
        finally {
            if(cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return false;
    }
    public boolean isGroupScoreCardExist(int scorecardID, int groupDetailID) {
        Cursor cursor = null;
        try {
            cursor = this.getChristReadableDatabase().rawQuery("SELECT AppID FROM " + SCORECARD_TBL + " WHERE ScoreCardID=? AND GroupDetailID=?",
                    new String[]{Integer.toString(scorecardID), Integer.toString(groupDetailID)});
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                return true;
            }
        }
        catch(Exception ex) {
        }
        finally {
            if(cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return false;
    }
    public void setUpdateStatus(ExScore score, boolean updated) {
        try {
            ContentValues values = new ContentValues();
            values.put("Updated", (updated ? "Y" : "N"));
            this.getChristWritableDatabase().update(SCORECARD_TBL, values, "AppID=? AND AppNo=? AND ScoreCardID=? AND GroupDetailID=?",
                    new String[]{Integer.toString(score.AppID),
                            Integer.toString(score.AppNo),
                            Integer.toString(score.ScoreCardID),
                            Integer.toString(score.GroupDetailID)});
        }
        catch(Exception ex) {
        }
    }
    public void saveScoreCard(ExScore score) {
        try {
            this.getChristWritableDatabase().delete(SCORECARD_TBL, "AppID=? AND AppNo=? AND ScoreCardID=? AND GroupDetailID=?",
                    new String[]{Integer.toString(score.AppID),
                            Integer.toString(score.AppNo),
                            Integer.toString(score.ScoreCardID),
                            Integer.toString(score.GroupDetailID)});

            ContentValues values = new ContentValues();
            values.put("AppID", score.AppID);
            values.put("AppNo", score.AppNo);
            values.put("ScoreCardID", score.ScoreCardID);
            values.put("GroupDetailID", score.GroupDetailID);
            values.put("Updated", score.Updated);
            values.put("Data", new Gson().toJson(score));
            this.getChristWritableDatabase().insert(SCORECARD_TBL, null, values);
        }
        catch(Exception ex) {
        }
    }
    public void saveScoreCards(ArrayList<ExScore> scores) {
        try {
            for (ExScore score : scores) {
                saveScoreCard(score);
            }
        }
        catch(Exception ex) {
        }
    }
    public ArrayList<ExScore> readScoreCards() {
        ArrayList<ExScore> scorecards = new ArrayList<ExScore>();
        Cursor cursor = null;
        try {
            cursor = this.getChristReadableDatabase().rawQuery("SELECT Updated, Data FROM " + SCORECARD_TBL, null);
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                ExScore scorecard = null;
                try {
                    String updated = cursor.getString(cursor.getColumnIndex("Updated"));
                    String json = cursor.getString(cursor.getColumnIndex("Data"));
                    if (json != null && json.trim().length() > 0) {
                        scorecard = new Gson().fromJson(json, ExScore.class);
                    }
                    scorecard.Updated = (updated != null && updated.trim().compareToIgnoreCase("Y") == 0);
                }catch(Exception ex) {
                }
                if(scorecard != null) {
                    scorecards.add(scorecard);
                }
                cursor.moveToNext();
            }
        }
        catch (Exception ex) {
        }
        finally {
            if(cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return scorecards;
    }
    public boolean isProfileExist(int appNo) {
        Cursor cursor = null;
        try {
            cursor = this.getChristReadableDatabase().rawQuery("SELECT ID FROM " +
                    STUDENT_TBL + " WHERE AppNo=?", new String[]{Integer.toString(appNo)});
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                return true;
            }
        } catch (Exception ex) {
        }
        finally {
            if(cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return false;
    }
    public ExStudentProfile getStudentProfile(int appNo) {
        ExStudentProfile profile = new ExStudentProfile();
        profile.Header = new ExStudentDtlInfo();
        profile.Qualifications = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = this.getChristReadableDatabase().rawQuery("SELECT " +
                    "ID, AppID, AppNo, FirstName, Gender, Email, " +
                    "DOB, Nationality, BirthPlace, Religion, Section, " +
                    "Category, PreReqMark, PreReqMonth, PreReqYear, PreReqRollNo, " +
                    "PreReqName, Pref1, Pref2, Pref3, JournalNo, " +
                    "ExamCenter, SelectionDate, Phone, Mobile, PAddress, " +
                    "CAddress, FatherName, MotherName, FatherIncome, MotherIncome, " +
                    "MotherJob, FatherJob, WorkExp, BackLogs FROM " + STUDENT_TBL + " WHERE AppNo=?", new String[]{Integer.toString(appNo)});
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                profile.Header.AppID = cursor.getInt(cursor.getColumnIndex("AppID"));
                profile.Header.AppNo = cursor.getInt(cursor.getColumnIndex("AppNo"));
                profile.Header.ID = cursor.getInt(cursor.getColumnIndex("ID"));
                profile.Header.FirstName = cursor.getString(cursor.getColumnIndex("FirstName"));
                profile.Header.Gender = cursor.getString(cursor.getColumnIndex("Gender"));
                profile.Header.Email = cursor.getString(cursor.getColumnIndex("Email"));
                profile.Header.DOB = cursor.getString(cursor.getColumnIndex("DOB"));
                profile.Header.Nationality = cursor.getString(cursor.getColumnIndex("Nationality"));
                profile.Header.BirthPlace = cursor.getString(cursor.getColumnIndex("BirthPlace"));
                profile.Header.Religion = cursor.getString(cursor.getColumnIndex("Religion"));
                profile.Header.Section = cursor.getString(cursor.getColumnIndex("Section"));
                profile.Header.Category = cursor.getString(cursor.getColumnIndex("Category"));
                profile.Header.PreReqMark = cursor.getFloat(cursor.getColumnIndex("PreReqMark"));
                profile.Header.PreReqMonth = cursor.getString(cursor.getColumnIndex("PreReqMonth"));
                profile.Header.PreReqYear = cursor.getString(cursor.getColumnIndex("PreReqYear"));
                profile.Header.PreReqRollNo = cursor.getString(cursor.getColumnIndex("PreReqRollNo"));
                profile.Header.PreReqName = cursor.getString(cursor.getColumnIndex("PreReqName"));
                profile.Header.Pref1 = cursor.getString(cursor.getColumnIndex("Pref1"));
                profile.Header.Pref2 = cursor.getString(cursor.getColumnIndex("Pref2"));
                profile.Header.Pref3 = cursor.getString(cursor.getColumnIndex("Pref3"));
                profile.Header.JournalNo = cursor.getString(cursor.getColumnIndex("JournalNo"));
                profile.Header.ExamCenter = cursor.getString(cursor.getColumnIndex("ExamCenter"));
                profile.Header.SelectionDate = cursor.getString(cursor.getColumnIndex("SelectionDate"));
                profile.Header.Phone = cursor.getString(cursor.getColumnIndex("Phone"));
                profile.Header.Mobile = cursor.getString(cursor.getColumnIndex("Mobile"));
                profile.Header.PAddress = cursor.getString(cursor.getColumnIndex("PAddress"));
                profile.Header.CAddress = cursor.getString(cursor.getColumnIndex("CAddress"));
                profile.Header.FatherName = cursor.getString(cursor.getColumnIndex("FatherName"));
                profile.Header.MotherName = cursor.getString(cursor.getColumnIndex("MotherName"));
                profile.Header.FatherIncome = cursor.getString(cursor.getColumnIndex("FatherIncome"));
                profile.Header.MotherIncome = cursor.getString(cursor.getColumnIndex("MotherIncome"));
                profile.Header.MotherJob = cursor.getString(cursor.getColumnIndex("MotherJob"));
                profile.Header.FatherJob = cursor.getString(cursor.getColumnIndex("FatherJob"));
                profile.Header.WorkExp = cursor.getFloat(cursor.getColumnIndex("WorkExp"));
                profile.Header.BackLogs = cursor.getInt(cursor.getColumnIndex("BackLogs"));
                break;
            }

            cursor = this.getChristReadableDatabase().rawQuery("SELECT " +
                    "AppID, AppNo, ID, Qualification, University, " +
                    "Institution, State, MarkPer, YnMPass, Attempt FROM " + QUALIFICATION_TBL + " WHERE AppNo=?", new String[]{Integer.toString(appNo)});
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                ExStudentEduInfo eduInfo = new ExStudentEduInfo();
                try {
                    eduInfo.AppID = cursor.getInt(cursor.getColumnIndex("AppID"));
                    eduInfo.AppNo = cursor.getInt(cursor.getColumnIndex("AppNo"));
                    eduInfo.ID = cursor.getInt(cursor.getColumnIndex("ID"));
                    eduInfo.Qualification = cursor.getString(cursor.getColumnIndex("Qualification"));
                    eduInfo.University = cursor.getString(cursor.getColumnIndex("University"));
                    eduInfo.Institution = cursor.getString(cursor.getColumnIndex("Institution"));
                    eduInfo.State = cursor.getString(cursor.getColumnIndex("State"));
                    eduInfo.MarkPer = cursor.getFloat(cursor.getColumnIndex("MarkPer"));
                    eduInfo.YnMPass = cursor.getString(cursor.getColumnIndex("YnMPass"));
                    eduInfo.Attempt = cursor.getInt(cursor.getColumnIndex("Attempt"));
                }catch(Exception ex) {
                }
                profile.Qualifications.add(eduInfo);
                cursor.moveToNext();
            }
        }
        catch (Exception ex) {
        }
        finally {
            if(cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return profile;
    }

    public void saveStudents(ExStudentDtlInfoList result) {
        if(result != null && result.Data != null && result.Data.size() > 0) {
            for(ExStudentDtlInfo student : result.Data) {
                this.saveStudent(student);
            }
        }
    }
    public void saveStudent(ExStudentDtlInfo student) {
        try {
            this.getChristWritableDatabase().delete(
                    STUDENT_TBL,
                    "AppID=? AND AppNo=?",
                    new String[]{Integer.toString(student.AppID), Integer.toString(student.AppNo)});

            ContentValues values = new ContentValues();
            values.put("AppID", student.AppID);
            values.put("AppNo", student.AppNo);
            values.put("ID", student.ID);
            values.put("FirstName", student.FirstName);
            values.put("Gender", student.Gender);
            values.put("Email", student.Email);
            values.put("DOB", student.DOB);
            values.put("Nationality", student.Nationality);
            values.put("BirthPlace", student.BirthPlace);
            values.put("Religion", student.Religion);
            values.put("Section", student.Section);
            values.put("Category", student.Category);
            values.put("PreReqMark", student.PreReqMark);
            values.put("PreReqMonth", student.PreReqMonth);
            values.put("PreReqYear", student.PreReqYear);
            values.put("PreReqRollNo", student.PreReqRollNo);
            values.put("PreReqName", student.PreReqName);
            values.put("Pref1", student.Pref1);
            values.put("Pref2", student.Pref2);
            values.put("Pref3", student.Pref3);
            values.put("JournalNo", student.JournalNo);
            values.put("ExamCenter", student.ExamCenter);
            values.put("SelectionDate", student.SelectionDate);
            values.put("Phone", student.Phone);
            values.put("Mobile", student.Mobile);
            values.put("PAddress", student.PAddress);
            values.put("CAddress", student.CAddress);
            values.put("FatherName", student.FatherName);
            values.put("MotherName", student.MotherName);
            values.put("FatherIncome", student.FatherIncome);
            values.put("MotherIncome", student.MotherIncome);
            values.put("MotherJob", student.MotherJob);
            values.put("FatherJob", student.FatherJob);
            values.put("WorkExp", student.WorkExp);
            values.put("BackLogs", student.BackLogs);
            this.getChristWritableDatabase().insert(STUDENT_TBL, null, values);
        }
        catch(Exception ex) {
        }
    }
    public void saveQualifications(ExStudentEduInfoList result) {
        if(result != null && result.Data != null && result.Data.size() > 0) {
            for(ExStudentEduInfo edu : result.Data) {
                this.saveQualification(edu);
            }
        }
    }
    public void saveQualification(ExStudentEduInfo edu) {
        try {
            this.getChristWritableDatabase().delete(
                    QUALIFICATION_TBL,
                    "AppID=? AND AppNo=? AND ID=?",
                    new String[]{Integer.toString(edu.AppID), Integer.toString(edu.AppNo), Integer.toString(edu.ID)});

            ContentValues values = new ContentValues();
            values.put("AppID", edu.AppID);
            values.put("AppNo", edu.AppNo);
            values.put("ID", edu.ID);
            values.put("Qualification", edu.Qualification);
            values.put("University", edu.University);
            values.put("Institution", edu.Institution);
            values.put("State", edu.State);
            values.put("MarkPer", edu.MarkPer);
            values.put("YnMPass", edu.YnMPass);
            values.put("Attempt", edu.Attempt);
            this.getChristWritableDatabase().insert(QUALIFICATION_TBL, null, values);
        }
        catch(Exception ex) {
        }
    }
}
