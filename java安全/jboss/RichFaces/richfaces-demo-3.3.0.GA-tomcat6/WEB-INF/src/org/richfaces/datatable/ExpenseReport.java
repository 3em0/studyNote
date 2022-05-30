package org.richfaces.datatable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExpenseReport {
	private List records = null;

	public List getRecords() {
		if (records==null) {
			initRecords();
		}
		return records;
	}

	public void setRecords(List records) {
		this.records = records;
	} 
	
	public double getTotalMeals() {
		double ret = 0.0;
		Iterator it = getRecords().iterator();
		while (it.hasNext()) {
			ExpenseReportRecord record = (ExpenseReportRecord) it.next();
			ret+=record.getTotalMeals();
		}
		return ret;
	}
	public double getTotalHotels() {
		double ret = 0.0;
		Iterator it = getRecords().iterator();
		while (it.hasNext()) {
			ExpenseReportRecord record = (ExpenseReportRecord) it.next();
			ret+=record.getTotalHotels();
		}
		return ret;
	}
	public double getTotalTransport() {
		double ret = 0.0;
		Iterator it = getRecords().iterator();
		while (it.hasNext()) {
			ExpenseReportRecord record = (ExpenseReportRecord) it.next();
			ret+=record.getTotalTransport();
		}
		return ret;
	}
	public double getGrandTotal() {
		return getTotalMeals()+getTotalHotels()+getTotalTransport();
	}
	public int getRecordsCount() {
		return getRecords().size();
	}
	
	private void initRecords() {
		records = new ArrayList();
		ExpenseReportRecord rec;
		rec = new ExpenseReportRecord();
		rec.setCity("San Jose");
		rec.getItems().add(new ExpenseReportRecordItem("25-Aug-97",37.74,112.0,45.0));
		rec.getItems().add(new ExpenseReportRecordItem("26-Aug-97",27.28,112.0,45.0));
		records.add(rec);
		rec = new ExpenseReportRecord();
		rec.setCity("Seattle");
		rec.getItems().add(new ExpenseReportRecordItem("27-Aug-97",96.25,109.0,36.00));
		rec.getItems().add(new ExpenseReportRecordItem("28-Aug-97",35.0,109.0,36.0));
		records.add(rec);
	}
	
}
