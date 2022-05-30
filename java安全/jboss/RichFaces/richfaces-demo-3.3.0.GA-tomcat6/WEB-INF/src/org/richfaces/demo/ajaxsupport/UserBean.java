package org.richfaces.demo.ajaxsupport;

import java.util.Date;

public class UserBean {
	private String name=""; 
	private java.lang.Integer screenWidth;
	private java.lang.Integer screenHeight;
	private String job;
	private String address;
	private String city;
	private String zip;
	private Integer age;
	public UserBean() {
		super();
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public java.lang.Integer getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(java.lang.Integer screenWidth) {
		this.screenWidth = screenWidth;
	}

	public java.lang.Integer getScreenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(java.lang.Integer screenHeight) {
		this.screenHeight = screenHeight;
	}
	
	public String nameItJohn() {
		setName("Jonh");
		return null;
	}
	public String nameItMark() {
		setName("Mark");
		return null;
	}

	public String getJob() {
		return job;
	}

	public void setJob(String job) {
		this.job = job;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

}
