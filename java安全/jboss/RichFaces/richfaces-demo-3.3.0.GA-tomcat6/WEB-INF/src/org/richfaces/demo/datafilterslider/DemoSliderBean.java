package org.richfaces.demo.datafilterslider;

import org.richfaces.event.DataFilterSliderEvent;


/**
 * @author $Autor$
 *
 */
public class DemoSliderBean {

    DemoInventoryList demoInventoryList;


    public void setDemoInventoryList(DemoInventoryList demoInventoryList) {
        this.demoInventoryList = demoInventoryList;
    }

    public void doSlide(DataFilterSliderEvent event) {

           Integer oldSliderVal = event.getOldSliderVal();
           Integer newSliderVal = event.getNewSliderVal();

          // System.out.println("Old Slider Value = " + oldSliderVal.toString() + "  " + "New Slider Value = " + newSliderVal.toString());

    }

}