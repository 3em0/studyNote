package org.richfaces.demo.common;

import java.util.HashMap;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;

public class Environment {
	private Map params = new HashMap();
	private String version; 
	public Map getParams() {
		return params;
	}

	public void setParams(Map params) {
		this.params = params;
	}
	
	public String getVersion() {
		String shortVersion = FacesContext.getCurrentInstance().getApplication().getExpressionFactory().createValueExpression(FacesContext.getCurrentInstance().getELContext(), "#{a4j.version}", String.class).getValue(FacesContext.getCurrentInstance().getELContext()).toString();
		return shortVersion.substring(0, shortVersion.indexOf("$Date"));
	}
}
