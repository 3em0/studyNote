/**
 * 
 */
package org.richfaces.demo.stateApi;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

/**
 * @author Ilya Shaikovsky
 * 
 */

public class RegisterAction {
	private Bean bean;

	public void listener(ActionEvent event) {
		//Check if the password fields are equals
		if (bean.getConfirmPassword().equals(bean.getPassword())) {
			FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, "registered");
		} else {
			FacesContext.getCurrentInstance().addMessage(
					event.getComponent().getClientId(
							FacesContext.getCurrentInstance()),
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Different passwords entered",
							"Different passwords entered"));
		}
	}
	
	public String ok() {
		if (FacesContext.getCurrentInstance().getMaximumSeverity()==null){
			return "registered";
		}else{
			return null;
		}
	}
	
	/**
	 * @return the bean
	 */
	public Bean getBean() {
		return bean;
	}

	/**
	 * @param bean
	 *            the bean to set
	 */
	public void setBean(Bean bean) {
		this.bean = bean;
	}

}