package org.richfaces.demo.sb;

import java.io.Serializable;

public class Data implements Serializable {
    private String text;

    private String label;

    public Data(String text, String label) {
        this.text = text;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

