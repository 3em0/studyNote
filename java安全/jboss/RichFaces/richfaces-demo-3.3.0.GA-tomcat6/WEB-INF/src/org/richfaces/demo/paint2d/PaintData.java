package org.richfaces.demo.paint2d;

import java.io.Serializable;

public class PaintData implements Serializable{
	String text;
	long color;
	float scale;
	public long getColor() {
		return color;
	}
	public void setColor(long color) {
		this.color = color;
	}
	public float getScale() {
		return scale;
	}
	public void setScale(float scale) {
		this.scale = scale;
	}

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
