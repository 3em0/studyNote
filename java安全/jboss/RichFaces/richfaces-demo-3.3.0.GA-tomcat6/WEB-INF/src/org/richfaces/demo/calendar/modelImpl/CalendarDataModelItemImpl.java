/**
 * License Agreement.
 *
 * Rich Faces - Natural Ajax for Java Server Faces (JSF)
 *
 * Copyright (C) 2007 Exadel, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package org.richfaces.demo.calendar.modelImpl;

import org.richfaces.model.CalendarDataModelItem;

/**
 * @author Nick Belaevski - mailto:nbelaevski@exadel.com
 * created 04.07.2007
 *
 */
public class CalendarDataModelItemImpl implements CalendarDataModelItem {

	private Object data;
	private String styleClass;
	private Object toolTip;
	private int day;
	private boolean enabled = true;
	
			
	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	/* (non-Javadoc)
	 * @see org.richfaces.component.CalendarDataModelItem#getData()
	 */
	public Object getData() {
		return data;
	}

	/* (non-Javadoc)
	 * @see org.richfaces.component.CalendarDataModelItem#getStyleClass()
	 */
	public String getStyleClass() {
		return styleClass;
	}

	/* (non-Javadoc)
	 * @see org.richfaces.component.CalendarDataModelItem#getToolTip()
	 */
	public Object getToolTip() {
		return toolTip;
	}

	/* (non-Javadoc)
	 * @see org.richfaces.component.CalendarDataModelItem#hasToolTip()
	 */
	public boolean hasToolTip() {
		return getToolTip() != null;
	}

	/* (non-Javadoc)
	 * @see org.richfaces.component.CalendarDataModelItem#isEnabled()
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * @param styleClass the styleClass to set
	 */
	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

	/**
	 * @param toolTip the toolTip to set
	 */
	public void setToolTip(Object toolTip) {
		this.toolTip = toolTip;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
}
