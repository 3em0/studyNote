package org.richfaces.demo.datafilterslider;

import java.util.List;

public interface DataFilterSliderDao {

    public List getCarsById(String id);

    public List getAllCarMakes();

    public int genRand();
}
