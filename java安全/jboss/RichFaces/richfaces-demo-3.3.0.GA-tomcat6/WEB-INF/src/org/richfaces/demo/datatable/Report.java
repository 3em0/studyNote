package org.richfaces.demo.datatable;

import org.richfaces.datatable.ExpenseReport;

public class Report {
	ExpenseReport expReport;

	public ExpenseReport getExpReport() {
		if (expReport == null)
			expReport = new ExpenseReport();
		return expReport;
	}

	public void setExpReport(ExpenseReport expReport) {
		this.expReport = expReport;
	}
}
