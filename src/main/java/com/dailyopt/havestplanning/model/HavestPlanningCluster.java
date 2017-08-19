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
	
	// additional methods
	public void concate(HavestPlanningCluster C){
		this.quantity = this.quantity + C.getQuantity();
		this.numberOfFields = this.numberOfFields + C.getNumberOfFields();
		HavestPlanningField[] newFields = new HavestPlanningField[fields.length + C.getFields().length];
		for(int i = 0; i < fields.length; i++)
			newFields[i] = fields[i];
		for(int i = 0; i < C.getFields().length; i++){
			newFields[i + fields.length] = C.getFields()[i];
		}
		fields = newFields;
	}
	
	
}
