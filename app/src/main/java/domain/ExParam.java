package domain;

import java.io.Serializable;

public class ExParam extends ExRefObject implements Serializable {
    public int MaxValue;
    public double Interval;
    public int Value = -1;
}
