package org.richfaces.demo.dnd;

import java.util.ArrayList;

public class DndBean {
	private ArrayList containerPHP;
	private ArrayList containerCF;
	private ArrayList containerDNET;
	private ArrayList frameworks;
	
	public ArrayList getContainerCF() {
		return containerCF;
	}
	public void setContainerCF(ArrayList containerCF) {
		this.containerCF = containerCF;
	}
	public ArrayList getContainerDNET() {
		return containerDNET;
	}
	public void setContainerDNET(ArrayList containerDNET) {
		this.containerDNET = containerDNET;
	}
	public ArrayList getContainerPHP() {
		return containerPHP;
	}
	public void setContainerPHP(ArrayList containerPHP) {
		this.containerPHP = containerPHP;
	}
	public ArrayList getFrameworks() {
		if (frameworks == null)
			initList();
		return frameworks;
	}
	public void setFrameworks(ArrayList frameworks) {
		this.frameworks = frameworks;
	}
	
	public void moveFramework(Object fm, Object family) {
		ArrayList target = null; 
		if ("PHP".equals(family)) target =  containerPHP;
		else if ("DNET".equals(family)) target =  containerDNET;
		else  if ("CF".equals(family)) target =  containerCF;
		
		if (target != null) {
			int ind = frameworks.indexOf(fm);
			if (ind > -1) {
					target.add(frameworks.get(ind));
					frameworks.remove(ind);
			}
			
		}
	}
	
	public String reset() {
		
		initList();
		return null;
		
	}
	
	private void initList() {
		frameworks = new ArrayList();
		frameworks.add(new Framework("Flexible Ajax", "PHP"));
		frameworks.add(new Framework("ajaxCFC", "CF"));
		frameworks.add(new Framework("AJAXEngine", "DNET"));
		frameworks.add(new Framework("AjaxAC", "PHP"));
		frameworks.add(new Framework("MonoRail", "DNET"));
		frameworks.add(new Framework("wddxAjax", "CF"));
		frameworks.add(new Framework("AJAX AGENT", "PHP"));
		frameworks.add(new Framework("FastPage", "DNET"));
		frameworks.add(new Framework("JSMX", "CF"));
		frameworks.add(new Framework("PAJAJ", "PHP"));
		frameworks.add(new Framework("Symfony", "PHP"));
		frameworks.add(new Framework("PowerWEB", "DNET"));
		
		containerPHP = new ArrayList();
		containerCF = new ArrayList();
		containerDNET = new ArrayList();
	}

}
