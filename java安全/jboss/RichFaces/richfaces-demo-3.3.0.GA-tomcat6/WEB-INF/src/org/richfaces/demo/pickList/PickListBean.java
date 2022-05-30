/**
 * 
 */
package org.richfaces.demo.pickList;

import java.util.List;

/**
 * @author Ilya Shaikovsky
 *
 */
public class PickListBean {
	
	private List<String> result;

	public PickListBean() {
	}

	public List<String> getResult() {
		return result;
	}

	public void setResult(List<String> result) {
		this.result = result;
	}

	public Integer getItems() {
		if (result == null){
			return 0;
		}
		return result.size();
	}
}
