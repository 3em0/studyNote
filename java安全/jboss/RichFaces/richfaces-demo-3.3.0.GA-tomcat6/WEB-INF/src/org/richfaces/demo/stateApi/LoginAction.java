/**
 * 
 */
package org.richfaces.demo.stateApi;

import javax.faces.event.ActionEvent;

/**
 * @author Ilya Shaikovsky
 *
 */
public class LoginAction
{
	
	private Bean bean; 
	
	public void listener(ActionEvent event) {
		//fetching some data on login
	}
	
	public String ok() {
		return "loggedIn";
	}

	public Bean getBean() {
		return bean;
	}

	public void setBean(Bean bean) {
		this.bean = bean;
	}
	
}	