package org.richfaces.demo.queue;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

public class QueueBean {
	public Long requestDelay = new Long(500);
	public boolean ignoreDupResponces = false;
	public boolean disabled = false;
	public int size = -1;
	public String sizeExceededBehavior = "dropNew";
	public String text="";
	public int requests=0;
	public int events=0;
	
	public List<SelectItem> strategies = new ArrayList<SelectItem>();
	
	public QueueBean() {
		SelectItem item = new SelectItem("dropNext", "Drop Next");
		strategies.add(item);
		item = new SelectItem("dropNew", "Drop New");
		strategies.add(item);
		item = new SelectItem("fireNext", "Fire Next");
		strategies.add(item);
		item = new SelectItem("fireNew", "Fire New");
		strategies.add(item);
	}
	
	public void resetText() {
		setText("");
	}
	
	public Long getRequestDelay() {
		return requestDelay;
	}

	public void setRequestDelay(Long requestDelay) {
		this.requestDelay = requestDelay;
	}

	public boolean isIgnoreDupResponces() {
		return ignoreDupResponces;
	}

	public void setIgnoreDupResponces(boolean ignoreDupResponces) {
		this.ignoreDupResponces = ignoreDupResponces;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getSizeExceededBehavior() {
		return sizeExceededBehavior;
	}

	public void setSizeExceededBehavior(String sizeExceedStrategy) {
		this.sizeExceededBehavior = sizeExceedStrategy;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<SelectItem> getStrategies() {
		return strategies;
	}

	public int getRequests() {
		return requests;
	}

	public void setRequests(int reuqests) {
		this.requests = reuqests;
	}

	public int getEvents() {
		return events;
	}

	public void setEvents(int events) {
		this.events = events;
	}

}
