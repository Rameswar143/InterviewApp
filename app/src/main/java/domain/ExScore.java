package domain;

import java.io.Serializable;
import java.util.ArrayList;

public class ExScore implements Serializable {
    public boolean Updated;
    public int AppID;
    public int AppNo;
    public int ScoreCardID;
    public int GroupDetailID;
    public int TotalTimeTaken = 0;
    public String IsBacklogExist = "";
    public String IsUgPgBreakExist = "";
    public String Is1012BreakExist = "";
    public String BreakInStudiesReason;
    public String IsCommerceBgExist = "";
    public int SpecID;
    public double WorkExp;
    public double MngWorkExp;
    public String Designation;
    public int GroomingID;
    public int DressCodeID;
    public int CommentID;
    public String ExComment;
    public int UserID;
    public int EduBgID;
    public boolean IsAbsent;
    public ArrayList<ExBaseObject> Marks = new ArrayList<>();
    public ArrayList<ExBaseObject> Params = new ArrayList<>();
}
