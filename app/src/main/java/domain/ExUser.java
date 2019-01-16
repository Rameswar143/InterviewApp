package domain;

import java.io.Serializable;

public class ExUser implements Serializable {
    public String ID;
    public String Name;
    public String LoginID;
    public String Password;
    public Boolean IsAdmin;
    public Boolean IsPannelist;
}