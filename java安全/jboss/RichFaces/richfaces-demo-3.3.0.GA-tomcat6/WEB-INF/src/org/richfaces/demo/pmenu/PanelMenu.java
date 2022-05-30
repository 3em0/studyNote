package org.richfaces.demo.pmenu;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.richfaces.component.UIPanelMenuItem;

public class PanelMenu {
	private String current;
	private boolean singleMode;
	public boolean isSingleMode() {
		return singleMode;
	}

	public void setSingleMode(boolean singleMode) {
		this.singleMode = singleMode;
	}

	public PanelMenu() {
	}
	
	public String getCurrent() {
		return this.current;
	}
	
	public void setCurrent(String current) {
		this.current = current;
	}
	public String updateCurrent() {
		FacesContext context=FacesContext.getCurrentInstance();
		setCurrent((String)context.getExternalContext().getRequestParameterMap().get("current"));
		return null;
	}
}
