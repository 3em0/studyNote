package org.richfaces.demo.listShuttle;

public class ToolBarItem {
private String icon;
private String label;
private String iconURI;
public ToolBarItem() {
	// TODO Auto-generated constructor stub
}

public ToolBarItem(String label, String icon) {
	setLabel(label);
	setIcon(icon);
}

public String getLabel() {
	return label;
}
public void setLabel(String label) {
	this.label = label;
}

public String getIcon() {
	return icon;
}

public void setIcon(String icon) {
	this.icon = icon;
}



public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((icon == null) ? 0 : icon.hashCode());
	result = prime * result + ((label == null) ? 0 : label.hashCode());
	return result;
}

public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (obj == null)
		return false;
	if (getClass() != obj.getClass())
		return false;
	final ToolBarItem other = (ToolBarItem) obj;
	if (icon == null) {
		if (other.icon != null)
			return false;
	} else if (!icon.equals(other.icon))
		return false;
	if (label == null) {
		if (other.label != null)
			return false;
	} else if (!label.equals(other.label))
		return false;
	return true;
}

public String getIconURI() {
	return "/images/icons/"+icon+".gif";
}


}
