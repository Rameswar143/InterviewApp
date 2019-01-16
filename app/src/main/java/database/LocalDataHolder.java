package database;

import java.io.Serializable;
import java.util.ArrayList;

import domain.ExBaseObject;
import domain.ExSelectionProcess;
import domain.ExSelectionProcessTypes;
import domain.ExTemplate;
import domain.ExUser;

public class LocalDataHolder implements Serializable {
    public ExUser SelectedUser = new ExUser();
    public ExBaseObject SelectedProgram = new ExBaseObject();
    public ExBaseObject SelectedCenter = new ExBaseObject();
    public ExSelectionProcessTypes SelectionProcessType = ExSelectionProcessTypes.None;
    public ArrayList<ExSelectionProcess> SelectionProcessList = new ArrayList<>();
    public ArrayList<ExTemplate> SelectedGroupScoreCards = new ArrayList<>();
}
