package org.richfaces.demo.validation;

import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.ajax4jsf.event.AjaxEvent;
import org.ajax4jsf.event.AjaxListener;
import org.richfaces.component.UITab;
import org.richfaces.component.UITabPanel;

public class TabAjaxListener implements AjaxListener{

	public void processAjax(AjaxEvent event) {
		FacesContext context = FacesContext.getCurrentInstance();
		//Conversion/Validation Messages appears
		if (context.getMaximumSeverity() != null){
			//getting all the components with messages
			Iterator<String> ids =  context.getClientIdsWithMessages();
			if(ids.hasNext())  {
				// getting element id as string
				String id = ids.next();
				// getting component by it's id
				UIViewRoot view = context.getViewRoot();
				UIComponent cmp = (UIComponent)view.findComponent(id);                 
				UIComponent parentTab = this.findParentTabComponent(cmp);
				if (parentTab != null){
					UIComponent parentTabPanel = parentTab.getParent();
					((UITabPanel)parentTabPanel).setSelectedTab(((UITab)parentTab).getName());
				}
			}
		}
	}
	 public UIComponent findParentTabComponent(UIComponent component)
	 { // need to find and return parent org.richfaces.Tab component

	   // searching for parent org.richfaces.Tab component
	   UIComponent parent = component.getParent();
	   while(!"org.richfaces.Tab".equals(parent.getFamily()))
	   {
	     parent = parent.getParent();

	     if(parent == null || parent.equals("javax.faces.ViewRoot"))
	     { // if parent = javax.faces.ViewRoot or null(which is parent() of javax.faces.ViewRoot) - we should break while cycle
	       parent = null;
	       break;
	     }
	   }
	     return parent;
	 }
}
