package com.dailyopt.havestplanning.model;

import java.util.Date;

public class HavestPlanningCluster {
	private String dates;
	private HavestPlanningField[] fields;
	private int quantity;
	private int numberOfFields;
	public String getDates() {
		return dates;
	}
	public void setDates(String dates) {
		this.dates = dates;
	}
	public HavestPlanningField[] getFields() {
		return fields;
	}
	public void setFields(HavestPlanningField[] fields) {
		this.fields = fields;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public int getNumberOfFields() {
		return numberOfFields;
	}
	public void setNumberOfFields(int numberOfFields) {
		this.numberOfFields = numberOfFields;
	}
	public HavestPlanningCluster(String dates, HavestPlanningField[] fields,
			int quantity, int numberOfFields) {
		super();
		this.dates = dates;
		this.fields = fields;
		this.quantity = quantity;
		this.numberOfFields = numberOfFields;
	}
	public HavestPlanningCluster() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
	
}
