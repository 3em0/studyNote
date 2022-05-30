package org.richfaces.demo.capitals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CapitalData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3456988151970108426L;
	private List<String> items = new ArrayList<String>();
	
	public void addItem(String item) {
		items.add(item);
	}
	
	public List<String> getItems() {
		return items;
	}
	
}
