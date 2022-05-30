/**
 * 
 */
package org.richfaces.demo.common;

import java.io.Serializable;

import javax.faces.context.FacesContext;


/**
 * @author sim
 *
 */
public class SkinBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2399884208294434812L;
	private String skin;
	private Object skinChooserState;

	public String getSkin() {
		String param = getSkinParam();
		if (param!=null) {
			setSkin(param);
		}
		return skin;
	}
	public void setSkin(String skin) {
		this.skin = skin;
	}
	
	private String getSkinParam(){
		FacesContext fc = FacesContext.getCurrentInstance();
		String param = (String) fc.getExternalContext().getRequestParameterMap().get("s");
		if (param!=null && param.trim().length()>0) {
			return param;
		} else {
			return null;
		}
	}
	
	
	public String changeSkin() {
		String param = getSkinParam();
		if (param!=null) {
			setSkin(param);
		}
		return null;
	}

	public Object getSkinChooserState() {
		return skinChooserState;
	}
	public void setSkinChooserState(Object skinChooserState) {
		this.skinChooserState = skinChooserState;
	}
}
