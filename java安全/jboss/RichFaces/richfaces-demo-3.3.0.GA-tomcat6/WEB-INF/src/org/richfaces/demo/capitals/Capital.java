package org.richfaces.demo.capitals;

import java.io.Serializable;

public class Capital implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1042449580199397136L;
	private boolean checked=false;
	private String name;
	private String state;
	private String timeZone;
	private CapitalData data;
	
	public CapitalData getData() {
		return data;
	}
	public void setData(CapitalData data) {
		this.data = data;
	}
	private final static String FILE_EXT = ".gif";
	public Capital() {
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}

	private String stateNameToFileName() {
		return state.replaceAll("\\s", "").toLowerCase();
	}
	
	public String getStateFlag() {
		return "/images/capitals/" + stateNameToFileName() + FILE_EXT;
	}
	
	public String getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
}
