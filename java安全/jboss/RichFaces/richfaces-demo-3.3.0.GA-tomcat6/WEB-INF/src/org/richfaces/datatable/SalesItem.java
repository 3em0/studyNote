package org.richfaces.datatable;

import java.util.ArrayList;

import javax.faces.model.SelectItem;

public class SalesItem {
	private int productCode;
	private double proposedPrice;
	private double ProposedGrossMargin;
	private double salesCost;
	private String reason;
	private ArrayList reasons; 
	public ArrayList getReasons() {
		reasons = new ArrayList();
		if (proposedPrice != 0.0) {
			if (proposedPrice <= salesCost) {
				reasons.add(new SelectItem("Nobody Needs it"));
				reasons.add(new SelectItem("Bad Quality"));
				reasons.add(new SelectItem("Partly Broken"));
			} else {
				reasons.add(new SelectItem("Just Good"));
				reasons.add(new SelectItem("Everybody Asks for it"));
			}
		} else {
			//reasons.add(new SelectItem("Enter the Price"));
		}
		return reasons;
	}
	public void setReasons(ArrayList reasons) {
		this.reasons = reasons;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	public SalesItem(int productCode, double salesCost) {
		super();
		this.productCode = productCode;
		this.salesCost = salesCost;
	}
	public int getProductCode() {
		return productCode;
	}
	public void setProductCode(int productCode) {
		this.productCode = productCode;
	}
	public double getProposedGrossMargin() {
		if (proposedPrice == 0)
			return 0;
		else {
			return (proposedPrice-salesCost)/proposedPrice ;
		}
	}
	public void setProposedGrossMargin(double proposedGrossMargin) {
		ProposedGrossMargin = proposedGrossMargin;
	}
	public double getProposedPrice() {
		return proposedPrice;
	}
	public void setProposedPrice(double proposedPrice) {
		this.proposedPrice = proposedPrice;
	}
	public double getSalesCost() {
		return salesCost;
	}
	public void setSalesCost(double salesCost) {
		this.salesCost = salesCost;
	}
}
