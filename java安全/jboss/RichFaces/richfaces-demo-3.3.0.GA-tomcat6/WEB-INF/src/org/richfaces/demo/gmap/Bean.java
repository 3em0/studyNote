package org.richfaces.demo.gmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;


public class Bean {

	private List<Place> point;
	private String currentId;
	private int zoom;
	private String gmapkey;
	
	
	public String getCurrentId() {
		return currentId;
	}



	public void setCurrentId(String currentId) {
		this.currentId = currentId;
	}



	public List<Place> getPoint() {
		if (point == null)
			initData();
		return point;
	}



	public void setPoint(List<Place> point) {
		this.point = point;
	}


	public Place getCurrentPlace() {
		Iterator<Place> it = point.iterator();
		while (it.hasNext()) {
			 Place pl = it.next();
			 if (currentId.equals(pl.getId())) {
				 zoom = pl.getZoom(); //sync with zoom of new place
				 return pl;
			 }
		}
		return (Place)point.get(0);	
	}

	private void initData() {
		point = new ArrayList<Place>();
		point.add(new Place ("goldengate", "/org/richfaces/demo/gmap/images/gold.gif", "37.81765", "-122.477603" , 14,
				"Golden Gate  Bridge, San Francisco"));
		point.add(new Place ("eiffeltower", "/org/richfaces/demo/gmap/images//tower.gif", "48.858489", "2.295295" , 17,
				"Eiffel Tower, Paris"));
		point.add(new Place ("pyramids", "/org/richfaces/demo/gmap/images/pyramids.gif", "29.977785", "31.132915" , 15,
				"Pyramids of Egypt, Giza"));
		point.add(new Place ("exadel", "/org/richfaces/demo/gmap/images/exadel.gif", "37.971796", "-122.042334" , 18,
				"Headquarter of Exadel, Inc , Concord"));
		currentId = "eiffeltower";
	}



	public int getZoom() {
		return zoom;
	}



	public void setZoom(int zoom) {
		this.zoom = zoom;
	}



	public String getGmapkey() {
		if (gmapkey == null) {
			gmapkey = createKey();
		}
		return gmapkey;
	}

	private String createKey() {
		
		HashMap hosts = new HashMap();
		hosts.put("localhost", "ABQIAAAAxU6W9QEhFLMNdc3ATIu-VxT2yXp_ZAY8_ufC3CFXhHIE1NvwkxRkrpOGzxH8_ud3inE9pG1845-FCA");
		hosts.put("localhost:8080", "ABQIAAAAxU6W9QEhFLMNdc3ATIu-VxTwM0brOpm-All5BF6PoaKBxRWWERTHxF5cK19oAMu3MP89kWdchuCH6w");
		hosts.put("livedemo.exadel.com", "ABQIAAAAxU6W9QEhFLMNdc3ATIu-VxRl-RYVoXwacweAQq3rWvtlmS78MhRst9EH2cahrIp0_HHi_U1Zn7o1Fg");
		
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		String host = (String)ec.getRequestHeaderMap().get("host");
		String key = (String)hosts.get(host);
		if (key != null) 
			return key;
		else
			return "get the key for your domain at http://www.google.com/apis/maps/signup.html";
		
	}


	public void setGmapkey(String gmapkey) {
		this.gmapkey = gmapkey;
	}

}
