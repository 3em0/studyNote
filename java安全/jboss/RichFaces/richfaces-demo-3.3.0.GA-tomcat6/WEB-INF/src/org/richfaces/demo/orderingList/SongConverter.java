package org.richfaces.demo.orderingList;

import java.util.StringTokenizer;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.richfaces.demo.stateApi.Bean;
import org.richfaces.demo.tree.Library;
import org.richfaces.demo.tree.Song;

public class SongConverter implements Converter{

	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {
		long id = Long.parseLong(value); 
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExpressionFactory expressionFactory = facesContext.getApplication()
		.getExpressionFactory();

		ValueExpression beanExpression = expressionFactory
		.createValueExpression(facesContext.getELContext(),
				"#{library}", Library.class);		
		
		Library library =  (Library)beanExpression.getValue(facesContext.getELContext());
		for (Song song : library.getSongsList()) {
			if (song.getId() == id){
				return song;				
			}
			
		}
		
		return null;
	}

	public String getAsString(FacesContext context, UIComponent component,
			Object value) {
		Song song = (Song)value;
		return new Long(song.getId()).toString();
	}

}
