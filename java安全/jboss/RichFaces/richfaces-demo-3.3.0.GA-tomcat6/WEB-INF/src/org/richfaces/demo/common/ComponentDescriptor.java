package org.richfaces.demo.common;

import javax.faces.context.FacesContext;

public class ComponentDescriptor {

    private String id;

    private String name;

    private String group;

    private String captionImage;

    private String iconImage;

    private String devGuideLocation;

    private String tldDocLocation;

    private String javaDocLocation;

    private String demoLocation;

    private boolean current;

    private String activeTab;
    
    private boolean newComponent;
    
    public boolean isNewComponent() {
		return newComponent;
	}

	public void setNewComponent(boolean newComponent) {
		this.newComponent = newComponent;
	}

	public ComponentDescriptor() {
        this.id = "";
        this.name = "";
        this.captionImage = "";
        this.iconImage = "";
        this.devGuideLocation = "";
        this.tldDocLocation = "";
        this.javaDocLocation = "";
        this.current = false;
        this.activeTab = "usage";
        this.newComponent=false;
    }

    public String getCaptionImage() {
        return captionImage;
    }

    public void setCaptionImage(String captionImage) {
        this.captionImage = captionImage;
    }

    public String getDevGuideLocation() {
        return devGuideLocation;
    }

    public void setDevGuideLocation(String devGuideLocation) {
        this.devGuideLocation = devGuideLocation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJavaDocLocation() {
        return javaDocLocation;
    }

    public void setJavaDocLocation(String javaDocLocation) {
        this.javaDocLocation = javaDocLocation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTldDocLocation() {
        return tldDocLocation;
    }

    public void setTldDocLocation(String tldDocLocation) {
        this.tldDocLocation = tldDocLocation;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }

    public String getIconImage() {
        return iconImage;
    }

    public void setIconImage(String iconImage) {
        this.iconImage = iconImage;
    }

    public String getDemoLocation() {
        return demoLocation;
    }

    public void setDemoLocation(String demoLocation) {
        this.demoLocation = demoLocation;
    }

    public String getContextRelativeDemoLocation() {
        FacesContext fc = FacesContext.getCurrentInstance();
        return fc.getExternalContext().getRequestContextPath() + getDemoLocation();
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getTagInfoLocation() {
        int pos = tldDocLocation.indexOf("tlddoc");
        if (pos > 0) {
            return tldDocLocation.substring(pos);
        }
        return tldDocLocation;
    }

    public String getTagUsageLocation() {
        return demoLocation.replaceAll("\\.jsf[\\s]*$", "");
    }

    /**
     * Gets value of activeTab field.
     * @return value of activeTab field
     */
    public String getActiveTab() {
        return activeTab;
    }

    /**
     * Set a new value for activeTab field.
     * @param activeTab a new value for activeTab field
     */
    public void setActiveTab(String activeTab) {
        this.activeTab = activeTab;
    }

}
