/**
 * 
 */
package org.richfaces.demo.stateApi;

import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

import org.richfaces.ui.model.States;

/**
 * @author asmirnov
 * 
 */
public class Config {

	/**
	 * @return States
	 */
	public States getStates() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		States states = new States();

		// Registering new User State definition
		states.setCurrentState("register"); // Name of the new state

		// Text labels, properties and Labels for controls in "register" state
		states.put("showConfirm", Boolean.TRUE); // confirm field rendering
		states.put("link", "(To login)"); // Switch State link label
		states.put("okBtn", "Register"); // Login/Register button label
		states.put("stateTitle", "Register New User"); // Panel title

		ExpressionFactory expressionFactory = facesContext.getApplication()
				.getExpressionFactory();

		// Define "registerbean" available under "bean" EL binding on the page
		ValueExpression beanExpression = expressionFactory
				.createValueExpression(facesContext.getELContext(),
						"#{registerbean}", Bean.class);
		states.put("bean", beanExpression);

		// Define "registeraction" available under "action" EL binding on the
		// page
		beanExpression = expressionFactory.createValueExpression(facesContext
				.getELContext(), "#{registeraction}", RegisterAction.class);
		states.put("action", beanExpression);

		// Define method expression inside registeraction binding for this state
		MethodExpression methodExpression = expressionFactory.createMethodExpression(
				facesContext.getELContext(), "#{registeraction.ok}",
				String.class, new Class[] {});
		states.put("ok", methodExpression);

		// Outcome for switching to login state definition
		states.setNavigation("switch", "login");

		// Login Existent User State analogous definition
		states.setCurrentState("login");
		states.put("showConfirm", Boolean.FALSE);
		states.put("link", "(To register)");
		states.put("okBtn", "Login");
		states.put("stateTitle", "Login Existent User");

		beanExpression = expressionFactory.createValueExpression(facesContext
				.getELContext(), "#{loginbean}", Bean.class);
		states.put("bean", beanExpression);

		beanExpression = expressionFactory.createValueExpression(facesContext
				.getELContext(), "#{loginaction}", LoginAction.class);
		states.put("action", beanExpression);

		methodExpression = expressionFactory.createMethodExpression(
				facesContext.getELContext(), "#{loginaction.ok}",
				String.class, new Class[] {});
		states.put("ok", methodExpression);

		states.setNavigation("switch", "register");

		return states;
	}
}