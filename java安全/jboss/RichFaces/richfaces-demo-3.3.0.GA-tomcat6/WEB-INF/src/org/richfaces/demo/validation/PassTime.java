package org.richfaces.demo.validation;

import org.hibernate.validator.Length;
import org.hibernate.validator.Max;
import org.hibernate.validator.Min;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

public class PassTime {

	public PassTime(String title, Integer time) {
		setTitle(title);
		setTime(time);
	}
	
	@NotEmpty
	@Length(max=15, min=3)
	private String title;
	@NotNull
	@Min(0)
	@Max(12)
	private Integer time;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Integer getTime() {
		return time;
	}
	public void setTime(Integer time) {
		this.time = time;
	}
	
}
