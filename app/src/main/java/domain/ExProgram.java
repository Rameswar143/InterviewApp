package domain;

import java.io.Serializable;
import java.util.ArrayList;

public class ExProgram extends ExBaseObject implements Serializable {
    public ArrayList<ExBaseObject> Centers = new ArrayList<>();
}
