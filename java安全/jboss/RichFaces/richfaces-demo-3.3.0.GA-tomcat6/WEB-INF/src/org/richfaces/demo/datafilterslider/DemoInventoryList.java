package org.richfaces.demo.datafilterslider;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIData;
import javax.faces.event.ActionEvent;

import org.richfaces.component.UIDataFltrSlider;

public class DemoInventoryList {

    DataFilterSliderDao dataFilterSliderDao = DataFilterSliderDaoImpl.getInstance();


    public DemoInventoryList() {
       loadCarMakeIndex();
    }

    private List headers;
    private List members;

    // dataTableColumn Names
    private static final String mileageColumnName = "Mileage";
    private static final String mileageMktAvgColumnName = "+/- Mkt Avg";
    private static final String priceColumnName = "Price";
    private static final String priceMktAvgColumnName = "+/- Mkt Avg";
    private static final String daysLiveColumnName = "Days Live";
    private static final String changeSearchesColumnName = "% Change Searches";
    private static final String changePriceColumnName = "% Change Price";
    private static final String exposureColumnName = "Exposure";
    private static final String activityColumnName = "Activity";
    private static final String printedColumnName = "Printed";
    private static final String inquiriesColumnName = "Inquiries";

    private void populateHeaderList(){
        headers = new ArrayList();
        //headers.add("");
        headers.add(mileageColumnName);
        headers.add(mileageMktAvgColumnName);
        headers.add(priceColumnName);
        headers.add(priceMktAvgColumnName);
        headers.add(daysLiveColumnName);
        headers.add(changeSearchesColumnName);
        headers.add(changePriceColumnName);
        headers.add(exposureColumnName);
        headers.add(activityColumnName);
        headers.add(printedColumnName);
        headers.add(inquiriesColumnName);


    }

    private void populateMemberList(){
        members = new ArrayList();
        members.add("avgMileage");
        members.add("avgMileageMarket");
        members.add("avgPrice");
        members.add("avgPriceMarket");
        members.add("avgDaysLive");
        members.add("avgChangeSearches");
        members.add("avgChangePrice");
        members.add("avgExposure");
        members.add("avgActivity");
        members.add("avgPrinted");
        members.add("avgInquiries");
    }



    public String getMileageColumnName() {
        return mileageColumnName;
    }


    public String getMileageMktAvgColumnName() {
        return mileageMktAvgColumnName;
    }

    public String getPriceMktAvgColumnName() {
        return priceMktAvgColumnName;
    }

    public String getPriceColumnName() {
        return priceColumnName;
    }

    public String getDaysLiveColumnName() {
        return daysLiveColumnName;
    }

    public String getChangeSearchesColumnName() {
        return changeSearchesColumnName;
    }

    public String getChangePriceColumnName() {
        return changePriceColumnName;
    }

    public String getExposureColumnName() {
        return exposureColumnName;
    }

    public String getActivityColumnName() {
        return activityColumnName;
    }

    public String getPrintedColumnName() {
        return printedColumnName;
    }

    public String getInquiriesColumnName() {
        return inquiriesColumnName;
    }

    private List carMakeIndex;


    public List getCarMakeIndex() {
        return carMakeIndex;
    }

    public void setCarMakeIndex(List carMakeIndex) {
        this.carMakeIndex = carMakeIndex;
    }

    private UIData carMakeIndexUIData;


    public UIData getCarMakeIndexUIData() {
        return carMakeIndexUIData;
    }

    public void setCarMakeIndexUIData(UIData carMakeIndexUIData) {
        this.carMakeIndexUIData = carMakeIndexUIData;
    }

    public Object getCarInventory() {

        if (uiData == null){
            loadCarTable("1");
        }

        return getUiData().getValue();

    }

    protected void loadCarMakeIndex() {

        carMakeIndex = dataFilterSliderDao.getAllCarMakes();
    }

    UIData uiData;


    public UIData getUiData() {
        if(uiData == null){
            uiData = new UIData();
        }
        return uiData;
    }

    public void setUiData(UIData uiData) {
        this.uiData = uiData;
    }

    public void loadCarTable(String id) {
       try{

            getUiData().setValue(dataFilterSliderDao.getCarsById(id));

        }catch(Exception e){
            e.printStackTrace();
        }
    }


    private static final String carMileageColumnName = "Mileage";
    private static final String carMileageMktAvgColumnName = "MktAvg";
    private static final String carPriceColumnName = "Price";


    public String getCarMileageColumnName() {
        return carMileageColumnName;
    }

    public String getCarMileageMktAvgColumnName() {
        return carMileageMktAvgColumnName;
    }

    public String getCarPriceColumnName() {
        return carPriceColumnName;
    }

    private String filterValue;
    private String filterRule;


    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }


    public String getFilterRule() {
        return filterRule;
    }

    public void setFilterRule(String filterRule) {
        this.filterRule = filterRule;


    }//Other Functions---------------------------------------------------------------------------------------
    public void populateTableFromMake(){
         loadCarTable(filterValue);
    }

    UIDataFltrSlider dataFilterSlider;


    public UIDataFltrSlider getDataFilterSlider() {
        return dataFilterSlider;
    }

    public void setDataFilterSlider(UIDataFltrSlider dataFilterSlider) {
        this.dataFilterSlider = dataFilterSlider;
    }

    public void filterCarList(ActionEvent event) {

    	UIDataFltrSlider slider = getDataFilterSlider();
    	slider.resetDataTable();

        try{
            filterValue = String.valueOf(carMakeIndexUIData.getRowIndex());
            filterRule = getAttribute(event, "filterRule");

            if (filterRule.equals("showTable")){
                loadCarTable(filterValue);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        slider.filterDataTable(slider.getHandleValue());
    }

    private static String getAttribute(ActionEvent event, String name) {
        return (String) event.getComponent().getAttributes().get(name);
    }

    public int genRandom;


    public int getGenRandom() {
        return dataFilterSliderDao.genRand();
    }

    public void setGenRandom(int genRandom) {
        this.genRandom = genRandom;
    }
}
