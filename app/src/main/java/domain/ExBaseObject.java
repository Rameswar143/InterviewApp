package domain;

import java.io.Serializable;

public class ExBaseObject implements Serializable {
    public int ID;
    public String Name;
    public ExBaseObject() {

    }
    public ExBaseObject(int id, String name) {
        this.ID = id;
        this.Name = name;
    }

    @Override
    public String toString() {
        return this.Name.toString();
    }
}
