/**
 * 
 */
package org.richfaces.demo.extendedDataTable;

import java.util.ArrayList;
import java.util.List;

import org.richfaces.demo.capitals.Capital;
import org.richfaces.model.DataProvider;
import org.richfaces.model.ExtendedTableDataModel;

/**
 * @author Ilya Shaikovsky
 *
 */
public class ExtendedTableBean {
	private String sortMode="single";
	private String selectionMode="multi";
	
	private ExtendedTableDataModel<Capital> dataModel;
	private List<Capital> capitals = new ArrayList<Capital>();
	
	public String getSortMode() {
		return sortMode;
	}

	public void setSortMode(String sortMode) {
		this.sortMode = sortMode;
	}

	public String getSelectionMode() {
		return selectionMode;
	}

	public void setSelectionMode(String selectionMode) {
		this.selectionMode = selectionMode;
	}

	public ExtendedTableBean() {
	}
	
	public ExtendedTableDataModel<Capital> getCapitalsDataModel() {
		if (dataModel == null) {
			dataModel = new ExtendedTableDataModel<Capital>(new DataProvider<Capital>(){

				private static final long serialVersionUID = 5054087821033164847L;

				public Capital getItemByKey(Object key) {
					for(Capital c : capitals){
						if (key.equals(getKey(c))){
							return c;
						}
					}
					return null;
				}

				public List<Capital> getItemsByRange(int firstRow, int endRow) {
					return capitals.subList(firstRow, endRow);
				}

				public Object getKey(Capital item) {
					return item.getName();
				}

				public int getRowCount() {
					return capitals.size();
				}
				
			});
		}
		return dataModel;
	}

	public void setCapitals(List<Capital> capitals) {
		this.capitals = capitals;
	}

}
