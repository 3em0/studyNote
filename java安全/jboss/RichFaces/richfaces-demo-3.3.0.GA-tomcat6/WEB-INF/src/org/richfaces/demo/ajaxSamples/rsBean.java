package org.richfaces.demo.ajaxSamples;

public class rsBean {
	private Integer addent1;
	private Integer addent2;
	private Integer sum;
	private String text1;
	private String text2;
	
	
	public Integer getSum() {
		return sum;
	}
	public void setSum(Integer sum) {
		this.sum = sum;
	}
	public Integer getAddent1() {
		return addent1;
	}
	public void setAddent1(Integer addent1) {
		this.addent1 = addent1;
	}
	public Integer getAddent2() {
		return addent2;
	}
	public void setAddent2(Integer addent2) {
		this.addent2 = addent2;
	}
	public String doSum() {
		sum = new Integer((addent1 != null ? addent1.intValue() : 0) + (addent2 != null ? addent2.intValue() : 0));
		return null;
	}
	public String getText1() {
		return text1;
	}
	public void setText1(String text1) {
		this.text1 = text1;
	}
	public String getText2() {
		return text2;
	}
	public void setText2(String text2) {
		this.text2 = text2;
	}
	
}
