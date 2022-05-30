/**
 * 
 */
package org.richfaces.datatablescroller;

import java.util.ArrayList;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.richfaces.model.Ordering;

/**
 * @author Ilya Shaikovsky
 *
 */
public class SortingBean {
	
	//Settings panel data bindings
	ArrayList<SelectItem> firstSortItems = new ArrayList<SelectItem>();
	ArrayList<SelectItem> secondSortItems = new ArrayList<SelectItem>();
	ArrayList<SelectItem> thirdSortItems = new ArrayList<SelectItem>();
	SelectItem[] sortDirectionItems = new SelectItem[3];
	private SortOption firstSortOption= new SortOption(0);
	private SortOption secondSortOption = new SortOption(0);
	private SortOption thirdSortOption = new SortOption(0);

	//Table Sort directions Binding
	ArrayList<String> prioritList = new ArrayList<String>();
	
	private String makeDirection = Ordering.UNSORTED.name();
	private String modelDirection = Ordering.UNSORTED.name();
	private String priceDirection = Ordering.UNSORTED.name();
	private String mileageDirection = Ordering.UNSORTED.name();
	
	public SortingBean() {
		firstSortItems.add(new SelectItem(0,"None"));
		firstSortItems.add(new SelectItem(1,"Model"));
		firstSortItems.add(new SelectItem(2,"Price"));
		firstSortItems.add(new SelectItem(3,"Make"));
		firstSortItems.add(new SelectItem(4,"Mileage"));
		
		sortDirectionItems[0] = new SelectItem(Ordering.UNSORTED, "Unsorted");
		sortDirectionItems[1] = new SelectItem(Ordering.ASCENDING, "Ascending");
		sortDirectionItems[2] = new SelectItem(Ordering.DESCENDING, "Descending");
	}
	
	public void firstSortOptionValueChanged(ValueChangeEvent valueChangeEvent) {
		if (!(valueChangeEvent.getNewValue().equals(new Integer(0)))){
			secondSortItems.clear();
			thirdSortItems.clear();
			for (SelectItem item : firstSortItems) {
				if (!(item.getValue().equals(valueChangeEvent.getNewValue()))){
					secondSortItems.add(item);
				}
			}
		}
		firstSortOption.setDirection(Ordering.UNSORTED);
		secondSortOption.setItem(0);
		secondSortOption.setDirection(Ordering.UNSORTED);
		thirdSortOption.setItem(0);
		thirdSortOption.setDirection(Ordering.UNSORTED);
	}
	
	public void secondSortOptionValueChanged(ValueChangeEvent valueChangeEvent) {
		if (!(valueChangeEvent.getNewValue().equals(new Integer(0)))){
			thirdSortItems.clear();
			for (SelectItem item : secondSortItems) {
				if (!(item.getValue().equals(valueChangeEvent.getNewValue()))){
					thirdSortItems.add(item);
				}
			}
		}
		secondSortOption.setDirection(Ordering.UNSORTED);
		thirdSortOption.setItem(0);
		thirdSortOption.setDirection(Ordering.UNSORTED);
	}

	public void thirdSortOptionValueChanged(ValueChangeEvent valueChangeEvent) {
		thirdSortOption.setDirection(Ordering.UNSORTED);
	}	
	
	public void checkSort(SortOption value){
		switch (value.getItem()) {
		case 1:
			prioritList.add("model");
			setModelDirection(value.getDirection().name());
			break;
		case 2:
			prioritList.add("price");
			setPriceDirection(value.getDirection().name());
			break;
		case 3:
			prioritList.add("make");
			setMakeDirection(value.getDirection().name());
			break;
		case 4:
			prioritList.add("mileage");
			setMileageDirection(value.getDirection().name());
			break;
			}
	}
	
	public String sortTable() {
		prioritList.clear();
		setModelDirection(Ordering.UNSORTED.name());
		setMakeDirection(Ordering.UNSORTED.name());
		setPriceDirection(Ordering.UNSORTED.name());
		setMileageDirection(Ordering.UNSORTED.name());
		if (firstSortOption.getItem()!=0){
			checkSort(firstSortOption);
			if (secondSortOption.getItem()!=0){
				checkSort(secondSortOption);
				if (thirdSortOption.getItem()!=0) {
					checkSort(thirdSortOption);
				}
			}
		}
		return null;
	}
	
	public SelectItem[] getSortDirectionItems() {
		return sortDirectionItems;
	}

	public ArrayList<String> getPrioritList() {
		return prioritList;
	}

	public SortOption getFirstSortOption() {
		return firstSortOption;
	}

	public void setFirstSortOption(SortOption firstSortOption) {
		this.firstSortOption = firstSortOption;
	}

	public SortOption getSecondSortOption() {
		return secondSortOption;
	}

	public void setSecondSortOption(SortOption secondSortOption) {
		this.secondSortOption = secondSortOption;
	}

	public SortOption getThirdSortOption() {
		return thirdSortOption;
	}

	public void setThirdSortOption(SortOption thirdSortOption) {
		this.thirdSortOption = thirdSortOption;
	}

	public ArrayList<SelectItem> getFirstSortItems() {
		return firstSortItems;
	}

	public ArrayList<SelectItem> getSecondSortItems() {
		return secondSortItems;
	}

	public ArrayList<SelectItem> getThirdSortItems() {
		return thirdSortItems;
	}

	public String getMakeDirection() {
		return makeDirection;
	}

	public void setMakeDirection(String makeDirection) {
		this.makeDirection = makeDirection;
	}

	public String getModelDirection() {
		return modelDirection;
	}

	public void setModelDirection(String modelDirection) {
		this.modelDirection = modelDirection;
	}

	public String getPriceDirection() {
		return priceDirection;
	}

	public void setPriceDirection(String priceDirection) {
		this.priceDirection = priceDirection;
	}

	public String getMileageDirection() {
		return mileageDirection;
	}

	public void setMileageDirection(String mileageDirection) {
		this.mileageDirection = mileageDirection;
	}

}
