package org.richfaces.datatable;

import java.util.ArrayList;
import java.util.List;

public class SalesReport {
	List items = null;

	public List getItems() {
		if (items == null)
			initData();
		return items;
	}

	public void setItems(List items) {
		this.items = items;
	}
	
	private void initData() {
		items = new ArrayList();
		items.add(new SalesItem(1, 20.00));
		items.add(new SalesItem(2, 10.00));
		items.add(new SalesItem(3, 20.00));
		items.add(new SalesItem(4, 20.00));
	}
}
