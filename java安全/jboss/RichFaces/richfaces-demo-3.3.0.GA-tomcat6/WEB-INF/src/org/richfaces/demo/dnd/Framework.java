package org.richfaces.demo.dnd;

public class Framework {
	private String name;
	private String family;
	public String getFamily() {
		return family;
	}
	public void setFamily(String family) {
		this.family = family;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Framework(String name, String family) {
		super();
		this.name = name;
		this.family = family;
	}
	

}
