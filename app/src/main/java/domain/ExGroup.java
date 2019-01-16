package domain;

import java.io.Serializable;
import java.util.ArrayList;

public class ExGroup extends ExBaseObject implements Serializable {
    public int ExamCenterID;
    public int ProgramID;
    public String Time;
    public ArrayList<ExGroupStudent> Students = new ArrayList<>();
}