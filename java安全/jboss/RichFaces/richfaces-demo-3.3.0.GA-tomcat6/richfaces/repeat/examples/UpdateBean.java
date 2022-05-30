package org.richfaces.datatable;

import javax.faces.component.html.HtmlInputText;
import javax.faces.context.FacesContext;
import org.ajax4jsf.component.UIRepeat;
import java.util.HashSet;
import java.util.Set;

public class UpdateBean {

	HtmlInputText priceRef;
	private UIRepeat repeater;
	private Set<Integer> keys = null;
	
	/**
	 * @return the keys
	 */
	public Set getKeys() {
		return keys;
	}

	/**
	 * @param keys the keys to set
	 */
	public void setKeys(Set keys) {
		this.keys = keys;
	}

	public void setRepeater(UIRepeat repeater) {
		this.repeater = repeater;
	}

	public UIRepeat getRepeater() {
		return repeater;
	}

	public HtmlInputText getPriceRef() {
		return priceRef;
	}

	public void setPriceRef(HtmlInputText priceRef) {
		this.priceRef = priceRef;
	}
	
	public String change(){
		
		HashSet keys = new HashSet<Integer>();
		int rowKey = getRepeater().getRowIndex();
		keys.add(rowKey);
		setKeys(keys);
		priceRef.processValidators(FacesContext.getCurrentInstance());
		priceRef.processUpdates(FacesContext.getCurrentInstance());
		return null;
	}
}
