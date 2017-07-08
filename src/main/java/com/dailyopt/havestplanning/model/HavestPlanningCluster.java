package com.dailyopt.havestplanning.model;

import java.util.Date;

public class HavestPlanningCluster {
	private String date;
	private int quantity;
	private int numberOfFields;
	private HavestPlanningField[] fields;
	
	public String getDate() {
		return date;
	}
	public void setDates(String date) {
		this.date = date;
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
	public HavestPlanningCluster(String date, HavestPlanningField[] fields,
			int quantity, int numberOfFields) {
		super();
		this.date = date;
		this.fields = fields;
		this.quantity = quantity;
		this.numberOfFields = numberOfFields;
	}
	public HavestPlanningCluster() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
	
}
