package org.richfaces.datatable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExpenseReportRecord {
	private String city;
	private List items = new ArrayList();
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public List getItems() {
		return this.items;
	}
	public void setItems(List items) {
		this.items = items;
	}
	public double getTotalMeals() {
		double ret = 0.0;
		Iterator it = items.iterator();
		while (it.hasNext()) {
			ExpenseReportRecordItem item = (ExpenseReportRecordItem) it.next();
			ret+=item.getMeals();
		}
		return ret;
	}
	public double getTotalHotels() {
		double ret = 0.0;
		Iterator it = items.iterator();
		while (it.hasNext()) {
			ExpenseReportRecordItem item = (ExpenseReportRecordItem) it.next();
			ret+=item.getHotels();
		}
		return ret;
	}
	public double getTotalTransport() {
		double ret = 0.0;
		Iterator it = items.iterator();
		while (it.hasNext()) {
			ExpenseReportRecordItem item = (ExpenseReportRecordItem) it.next();
			ret+=item.getTransport();
		}
		return ret;
	}
	public double getTotal() {
		return getTotalMeals() +getTotalHotels()+getTotalTransport(); 
	}
	
	public int getItemsCount() {
		return getItems().size();
	}
}
