package org.richfaces.demo.extendeddatamodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import org.ajax4jsf.model.SerializableDataModel;
/**
 * 
 * @author ias
 * This is example class that intended to demonstrate use of ExtendedDataModel and SerializableDataModel.
 * This implementation intended to be used as a request scope bean. However, it actually provides serialized
 * state, so on a post-back we do not load data from the data provider. Instead we use data that was used 
 * during rendering.
 * This data model must be used together with Data Provider, which is responsible for actual data load 
 * from the database using specific filtering and sorting. Normally Data Provider must be in either session, or conversation
 * scope.
 */
public class AuctionDataModel extends SerializableDataModel {
	
	private AuctionDataProvider dataProvider;
	private Integer currentPk;
	private Map<Integer,AuctionItem> wrappedData = new HashMap<Integer,AuctionItem>();
	private List<Integer> wrappedKeys = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = -1956179896877538628L;

	/**
	 * This method never called from framework.
	 * (non-Javadoc)
	 * @see org.ajax4jsf.model.ExtendedDataModel#getRowKey()
	 */
	@Override
	public Object getRowKey() {
		return currentPk;
	}
	/**
	 * This method normally called by Visitor before request Data Row.
	 */
	@Override
	public void setRowKey(Object key) {
		this.currentPk = (Integer) key;
		
	}
	/**
	 * This is main part of Visitor pattern. Method called by framework many times during request processing. 
	 */
	@Override
	public void walk(FacesContext context, DataVisitor visitor, Range range, Object argument) throws IOException {
		int firstRow = ((SequenceRange)range).getFirstRow();
		int numberOfRows = ((SequenceRange)range).getRows();
		wrappedKeys = new ArrayList<Integer>();
		for (AuctionItem item:dataProvider.getItemsByrange(new Integer(firstRow), numberOfRows, null, true)) {
			wrappedKeys.add(item.getPk());
			wrappedData.put(item.getPk(), item);
			visitor.process(context, item.getPk(), argument);
		}
	}
	/**
	 * This method must return actual data rows count from the Data Provider. It is used by pagination control
	 * to determine total number of data items.
	 */
	private Integer rowCount; // better to buffer row count locally
	@Override
	public int getRowCount() {
		if (rowCount==null) {
			rowCount = new Integer(getDataProvider().getRowCount());
			return rowCount.intValue();
		} else {
			return rowCount.intValue();
		}
	}
	/**
	 * This is main way to obtain data row. It is intensively used by framework. 
	 * We strongly recommend use of local cache in that method. 
	 */
	@Override
	public Object getRowData() {
		if (currentPk==null) {
			return null;
		} else {
			AuctionItem ret = wrappedData.get(currentPk);
			if (ret==null) {
				ret = getDataProvider().getAuctionItemByPk(currentPk);
				wrappedData.put(currentPk, ret);
				return ret;
			} else {
				return ret;
			}
		}
	}

	/**
	 * Unused rudiment from old JSF staff.
	 */
	@Override
	public int getRowIndex() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Unused rudiment from old JSF staff.
	 */
	@Override
	public Object getWrappedData() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Never called by framework.
	 */
	@Override
	public boolean isRowAvailable() {
		if (currentPk==null) {
			return false;
		} else {
			return getDataProvider().hasAuctionItemByPk(currentPk);
		}
	}

	/**
	 * Unused rudiment from old JSF staff.
	 */
	@Override
	public void setRowIndex(int rowIndex) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Unused rudiment from old JSF staff.
	 */
	@Override
	public void setWrappedData(Object data) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This method suppose to produce SerializableDataModel that will be serialized into View State and used on a post-back.
	 * In current implementation we just mark current model as serialized. In more complicated cases we may need to 
	 * transform data to actually serialized form.
	 */
	public  SerializableDataModel getSerializableModel(Range range) {
		if (wrappedKeys!=null) {
			return this; 
		} else {
			return null;
		}
	}
	/**
	 * This is helper method that is called by framework after model update. In must delegate actual database update to 
	 * Data Provider.
	 */
	@Override
	public void update() {
		getDataProvider().update();
	}

	public AuctionDataProvider getDataProvider() {
		return dataProvider;
	}

	public void setDataProvider(AuctionDataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}

}
