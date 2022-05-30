/**
 * 
 */
package org.richfaces.datatablescroller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.richfaces.component.UIScrollableDataTable;
import org.richfaces.demo.datafilterslider.DemoInventoryItem;
import org.richfaces.model.SortField;
import org.richfaces.model.SortOrder;
import org.richfaces.model.selection.SimpleSelection;

/**
 * @author Nick Belaevski - nbelaevski@exadel.com
 * created 02.03.2007
 * 
 */
public class DataTableScrollerBean {
	
	private DemoInventoryItem currentItem = new DemoInventoryItem();	

	private Set<Integer> keys = new HashSet<Integer>();
	
	private int currentRow;	
	
	private SimpleSelection selection = new SimpleSelection();
	
	private UIScrollableDataTable table;
	
	private SortOrder order = new SortOrder();
	
	private int scrollerPage;
	
	private ArrayList<DemoInventoryItem> selectedCars = new ArrayList<DemoInventoryItem>();
	private ArrayList<Facet> columns = new ArrayList<Facet>(); 
	private static int DECIMALS = 1;
	private static int ROUNDING_MODE = BigDecimal.ROUND_HALF_UP;
	
	private List <DemoInventoryItem> allCars = null;

	public DataTableScrollerBean() {
		initColumnsHeaders();
		SortField[] fields = {new SortField("make", true)};
		order.setFields(fields);
	}
	
	public List <DemoInventoryItem> getAllCars() {
		synchronized (this) {
			if (allCars == null) {
				allCars = new ArrayList<DemoInventoryItem>();
				for (int k = 0; k <= 5; k++) {
					try{
						switch (k) {
						case 0:
							allCars.addAll(createCar("Chevrolet","Corvette", 5));
							allCars.addAll(createCar("Chevrolet","Malibu", 8));
							allCars.addAll(createCar("Chevrolet","S-10", 10));
							allCars.addAll(createCar("Chevrolet","Tahoe", 6));
							break;

						case 1:
							allCars.addAll(createCar("Ford","Taurus", 12));
							allCars.addAll(createCar("Ford","Explorer", 11));
							break;
						case 2:
							allCars.addAll(createCar("Nissan","Maxima", 9));
							break;
						case 3:
							allCars.addAll(createCar("Toyota","4-Runner", 7));
							allCars.addAll(createCar("Toyota","Camry", 15));
							allCars.addAll(createCar("Toyota","Avalon", 13));
							break;
						case 4:
							allCars.addAll(createCar("GMC","Sierra", 8));
							allCars.addAll(createCar("GMC","Yukon", 10));
							break;
						case 5:
							allCars.addAll(createCar("Infiniti","G35", 6));
							break;
						/*case 6:
							allCars.addAll(createCar("UAZ","469", 6));
							break;*/
						default:
							break;
						}
					}catch(Exception e){
						System.out.println("!!!!!!loadAllCars Error: " + e.getMessage());
						e.printStackTrace();
					}
				}
			}
		}
		return allCars;
	}

	public List<DemoInventoryItem> getTenRandomCars() {
		List<DemoInventoryItem> result = new ArrayList<DemoInventoryItem>();
		int size = getAllCars().size()-1; 
		for (int i = 0; i < 10; i++) {
			result.add(getAllCars().get(rand(1, size)));
		}
		return result;
	}
	
	public int genRand() {
		return rand(1,10000);
	}

	public List <DemoInventoryItem> createCar(String make, String model, int count){

		ArrayList <DemoInventoryItem> iiList = null;

		try{
			int arrayCount = count;

			DemoInventoryItem[] demoInventoryItemArrays = new DemoInventoryItem[arrayCount];

			for (int j = 0; j < demoInventoryItemArrays.length; j++){
				DemoInventoryItem ii = new DemoInventoryItem();

				ii.setMake(make);
				ii.setModel(model);
				ii.setStock(randomstring(6,7));
				ii.setVin(randomstring(14,15));
				ii.setMileage(new BigDecimal(rand(5000,80000)).setScale(DECIMALS, ROUNDING_MODE));
				ii.setMileageMarket(new BigDecimal(rand(25000,45000)).setScale(DECIMALS, ROUNDING_MODE));
				ii.setPrice(new Integer(rand(15000,55000)));
				ii.setPriceMarket(new BigDecimal(rand(15000,55000)).setScale(DECIMALS, ROUNDING_MODE));
				ii.setDaysLive(rand(1,90));
				ii.setChangeSearches(new BigDecimal(rand(0,5)).setScale(DECIMALS, ROUNDING_MODE));
				ii.setChangePrice(new BigDecimal(rand(0,5)).setScale(DECIMALS, ROUNDING_MODE));
				ii.setExposure(new BigDecimal(rand(0,5)).setScale(DECIMALS, ROUNDING_MODE));
				ii.setActivity(new BigDecimal(rand(0,5)).setScale(DECIMALS, ROUNDING_MODE));
				ii.setPrinted(new BigDecimal(rand(0,5)).setScale(DECIMALS, ROUNDING_MODE));
				ii.setInquiries(new BigDecimal(rand(0,5)).setScale(DECIMALS, ROUNDING_MODE));
				demoInventoryItemArrays[j] = ii;

			}

			iiList = new ArrayList<DemoInventoryItem>(Arrays.asList(demoInventoryItemArrays));

		}catch(Exception e){
			System.out.println("!!!!!!createCategory Error: " + e.getMessage());
			e.printStackTrace();
		}
		return iiList;
	}

	public static int rand(int lo, int hi)
	{
		Random rn2 = new Random();
		//System.out.println("**" + lo);
		//System.out.println("**" + hi);
		int n = hi - lo + 1;
		int i = rn2.nextInt() % n;
		if (i < 0)
			i = -i;
		return lo + i;
	}

	public static String randomstring(int lo, int hi)
	{
		int n = rand(lo, hi);
		byte b[] = new byte[n];
		for (int i = 0; i < n; i++)
			b[i] = (byte)rand('A', 'Z');
		return new String(b);
	}

	public SimpleSelection getSelection() {
		return selection;
	}

	public void setSelection(SimpleSelection selection) {
		this.selection = selection;
	}
	
	public String takeSelection() {
		getSelectedCars().clear();
		if (getSelection().isSelectAll()){
			getSelectedCars().addAll(allCars);
		}else{
			Iterator<Object> iterator = getSelection().getKeys();
			while (iterator.hasNext()){
				Object key = iterator.next();
				table.setRowKey(key);
				if (table.isRowAvailable()) {
					getSelectedCars().add((DemoInventoryItem) table.getRowData());
				}
			}
		}
		return null;
	}

	public ArrayList<DemoInventoryItem> getSelectedCars() {
		return selectedCars;
	}

	public void setSelectedCars(ArrayList<DemoInventoryItem> selectedCars) {
		this.selectedCars = selectedCars;
	}

	public UIScrollableDataTable getTable() {
		return table;
	}

	public void setTable(UIScrollableDataTable table) {
		this.table = table;
	}
	
	public void initColumnsHeaders(){
		columns.clear();
		String header;
		String footer="";
		header = "Chevrolet";
		Facet facet = new Facet(header ,footer);
		columns.add(facet);
		header = "Ford";
		facet = new Facet(header ,footer);
		columns.add(facet);
		header = "Nissan";
		facet = new Facet(header ,footer);
		columns.add(facet);
		header = "Toyota";
		facet = new Facet(header ,footer);
		columns.add(facet);
		header = "GMC";
		facet = new Facet(header ,footer);
		columns.add(facet);
		header = "Infiniti";
		facet = new Facet(header ,footer);
		columns.add(facet);
	}
	
	public ArrayList<DemoInventoryItem[]> getModel() {

		ArrayList<DemoInventoryItem[]> model = new ArrayList<DemoInventoryItem[]>();
		for (int i = 0; i < 9; i++) {
			DemoInventoryItem[] items = new DemoInventoryItem[6];
			items[0]=createCar("Chevrolet","Corvette", 1).get(0);
			items[1]=createCar("Ford","Explorer", 1).get(0);
			items[2]=createCar("Nissan","Maxima", 1).get(0);
			items[3]=createCar("Toyota","Camry", 1).get(0);
			items[4]=createCar("GMC","Yukon", 1).get(0);
			items[5]=createCar("Infiniti","G35", 1).get(0);
			model.add(items);
		}
		return model;
	}

	public ArrayList<Facet> getColumns() {
		return columns;
	}

	public int getScrollerPage() {
		return scrollerPage;
	}

	public void setScrollerPage(int scrollerPage) {
		this.scrollerPage = scrollerPage;
	}

	public SortOrder getOrder() {
		return order;
	}

	public void setOrder(SortOrder order) {
		this.order = order;
	}

	public DemoInventoryItem getCurrentItem() {
		return currentItem;
	}

	public void setCurrentItem(DemoInventoryItem currentItem) {
		this.currentItem = currentItem;
	}

	public int getCurrentRow() {
		return currentRow;
	}

	public void setCurrentRow(int currentRow) {
		this.currentRow = currentRow;
	}

	public void store() {
		allCars.set(currentRow, currentItem);
		keys.clear();
		keys.add(currentRow);
	}

	public void delete() {
		allCars.remove(currentRow);
	}

	public Set<Integer> getKeys() {
		return keys;
	}

	public void setKeys(Set<Integer> keys) {
		this.keys = keys;
	}
	
}
