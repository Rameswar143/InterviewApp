package domain;

import java.io.Serializable;
import java.util.ArrayList;

public class ExTemplate extends ExBaseObject implements Serializable {
    public boolean IsGroup;
    public boolean IsSelected;
    public boolean IsEduBgReq;
    public boolean IsMarkPerReq;
    public boolean IsBacklogReq;
    public boolean IsUgPgBreakReq;
    public boolean Is1012BreakReq;
    public boolean IsBreakReasonReq;
    public boolean IsCommerceBgReq;
    public boolean IsSpecReq;
    public int TotalTimeTaken;

    public boolean IsWorkExpReq;
    public int WorkExpMaxValue;
    public double WorkExpInterval;
    public boolean IsMngWorkExpReq;
    public int MngWorkExpMaxValue;
    public double MngWorkExpInterval;
    public boolean IsDesignationReq;

    public boolean IsGroomingReq;
    public boolean IsDressCodeReq;
    public boolean IsCommentReq;
    public boolean IsExCommentReq;

    public ArrayList<ExParam> ParamList = new ArrayList<>();
    public ArrayList<ExBaseObject> GroomingList = new ArrayList<>();
    public ArrayList<ExBaseObject> DressCodeList = new ArrayList<>();
    public ArrayList<ExBaseObject> CommentList = new ArrayList<>();
    public ArrayList<ExBaseObject> SpecList = new ArrayList<>();
    public ArrayList<ExBaseObject> EduMarkList = new ArrayList<>();
    public ArrayList<ExBaseObject> EduBgList = new ArrayList<>();

    public ExBaseObject SelectionProcess = new ExBaseObject();
}
