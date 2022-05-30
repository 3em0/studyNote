/*
 * DemoTreeNodeImpl.java		Date created: 05.09.2008
 * Last modified by: $Author: ilya_shaikovsky $
 * $Revision: 10378 $	$Date: 2008-09-09 17:14:51 +0300 $
 */

package org.richfaces.demo.tree;

import org.richfaces.model.TreeNodeImpl;

/**
 * TODO Class description goes here.
 * @author Alexandr Levkovsky
 *
 */
public class DemoTreeNodeImpl<T> extends TreeNodeImpl<T>{

    private String icon;
    private String leafIcon;
    
	private String type;

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getLeafIcon() {
		return leafIcon;
	}

	public void setLeafIcon(String leafIcon) {
		this.leafIcon = leafIcon;
	}  
    
}
