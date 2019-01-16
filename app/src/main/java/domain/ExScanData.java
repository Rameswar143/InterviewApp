package domain;

import java.io.Serializable;

public class ExScanData  implements Serializable {
    public ExBaseObject Course = new ExBaseObject();
    public ExBaseObject Program = new ExBaseObject();
    public int ApplicationNo;
    public int ApplicationID;
    public String FirstName;
    public String DOB;
    public int StudentID;
    public int BatchYear;
}

