/**
 * 
 */
package org.richfaces.datatablescroller;

import org.richfaces.model.Ordering;

/**
 * @author Ilya Shaikovsky
 *
 */
public class SortOption {
	private int item;
	private Ordering direction;

	public SortOption(Integer item) {
		setItem(item);
		setDirection(Ordering.UNSORTED);
	}
	
	public Ordering getDirection() {
		return direction;
	}
	public void setDirection(Ordering direction) {
		this.direction = direction;
	}


	public int getItem() {
		return item;
	}


	public void setItem(int item) {
		this.item = item;
	} 
	
}
