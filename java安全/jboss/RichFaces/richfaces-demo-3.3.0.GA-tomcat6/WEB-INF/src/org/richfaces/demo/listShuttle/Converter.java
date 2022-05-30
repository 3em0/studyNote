package org.richfaces.demo.listShuttle;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

public class Converter implements javax.faces.convert.Converter{

	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {

		int index = value.indexOf(':');
		
		return new ToolBarItem(value.substring(0, index), value.substring(index + 1));
	}

	public String getAsString(FacesContext context, UIComponent component,
			Object value) {

		ToolBarItem optionItem = (ToolBarItem) value;
		return optionItem.getLabel() + ":" + optionItem.getIcon();
	}

}