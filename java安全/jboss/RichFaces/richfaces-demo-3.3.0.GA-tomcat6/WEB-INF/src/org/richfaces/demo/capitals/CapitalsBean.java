package org.richfaces.demo.capitals;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.model.SelectItem;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.xml.sax.SAXException;

public class CapitalsBean {
	private ArrayList<Capital> capitals = new ArrayList<Capital>();
	private ArrayList<String> capitalsNames = new ArrayList<String>();
	private List<SelectItem> capitalsOptions = new ArrayList<SelectItem>();
    private String capital = ""; 
	
	public List<Capital> autocomplete(Object suggest) {
        String pref = (String)suggest;
        ArrayList<Capital> result = new ArrayList<Capital>();

        Iterator<Capital> iterator = getCapitals().iterator();
        while (iterator.hasNext()) {
            Capital elem = ((Capital) iterator.next());
            if ((elem.getName() != null && elem.getName().toLowerCase().indexOf(pref.toLowerCase()) == 0) || "".equals(pref))
            {
                result.add(elem);
            }
        }
        return result;
    }
    
	public CapitalsBean() {
		URL rulesUrl = getClass().getResource("capitals-rules.xml");
		Digester digester =	DigesterLoader.createDigester(rulesUrl);
		digester.push(this);
		try {
			digester.parse(getClass().getResourceAsStream("capitals.xml"));
		} catch (IOException e) {
			throw new FacesException(e);
		} catch (SAXException e) {
			throw new FacesException(e);
		}
		capitalsNames.clear();
		for (Capital cap : capitals) {
			capitalsNames.add(cap.getName());
		}
		capitalsOptions.clear();
		for (Capital cap : capitals) {
			capitalsOptions.add(new SelectItem(cap.getName(),cap.getState()));
		}
	}
	
	public String addCapital(Capital capital) {
		capitals.add(capital);
		return null;
	}
	
	public ArrayList<Capital> getCapitals() {
		return capitals;
	}

	public String getCapital() {
		return capital;
	}

	public void setCapital(String capital) {
		this.capital = capital;
	}

	public List<SelectItem> getCapitalsOptions() {
		return capitalsOptions;
	}

	public ArrayList<String> getCapitalsNames() {
		return capitalsNames;
	}

}
