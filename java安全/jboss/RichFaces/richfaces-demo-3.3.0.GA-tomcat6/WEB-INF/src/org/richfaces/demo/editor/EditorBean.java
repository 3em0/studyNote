package org.richfaces.demo.editor;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

public class EditorBean {

	private String currentConfiguration = CONFIGS_PACKAGE + "simple";
	private String viewMode = "visual";
	private String value;
	private boolean liveUpdatesEnabled=false;
	private boolean useSeamText=false;
	
	private static final String CONFIGS_PACKAGE = "/org/richfaces/demo/editor/";
	List<SelectItem> configurations = new ArrayList<SelectItem>();
	
	public EditorBean() {
		configurations.add(new SelectItem(CONFIGS_PACKAGE + "simple", "Simple"));
		configurations.add(new SelectItem(CONFIGS_PACKAGE + "advanced", "Advanced"));
	}
	
	public void resetValue() {
		value = "";
	}
	
	public String getCurrentConfiguration() {
		return currentConfiguration;
	}

	public void setCurrentConfiguration(String currentConfiguration) {
		this.currentConfiguration = currentConfiguration;
	}

	public List<SelectItem> getConfigurations() {
		return configurations;
	}

	public String getViewMode() {
		return viewMode;
	}

	public void setViewMode(String viewMode) {
		this.viewMode = viewMode;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isLiveUpdatesEnabled() {
		return liveUpdatesEnabled;
	}

	public void setLiveUpdatesEnabled(boolean liveUpdatesEnabled) {
		this.liveUpdatesEnabled = liveUpdatesEnabled;
	}

	public boolean isUseSeamText() {
		return useSeamText;
	}

	public void setUseSeamText(boolean useSeamText) {
		this.useSeamText = useSeamText;
	}
	
	
	
}
