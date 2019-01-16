package database;

import java.io.Serializable;
import java.util.ArrayList;

import domain.ExCourseRange;
import domain.ExGroup;
import domain.ExProgram;
import domain.ExScoreCardCourse;
import domain.ExSelectionProcess;
import domain.ExTemplate;

public class DeviceDataHolder implements Serializable {
    public ArrayList<ExSelectionProcess> SelectionProcessList = new ArrayList<>();
    public ArrayList<ExTemplate> TemplateList = new ArrayList<>();
    public ArrayList<ExProgram> ProgramList = new ArrayList<>();
    public ArrayList<ExCourseRange> CourseRangeList = new ArrayList<>();
    public ArrayList<ExScoreCardCourse> ScoreCardCourseList = new ArrayList<>();
    public ArrayList<ExGroup> GroupList = new ArrayList<>();
}
